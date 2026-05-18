package pl.siarko.jetlytra.client.input;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import pl.siarko.jetlytra.JetlytraSlotHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import pl.siarko.jetlytra.client.ClientJetpackState;
import pl.siarko.jetlytra.client.input.integration.ControlifyBridge;
import pl.siarko.jetlytra.config.JetlytraClientConfig;
import pl.siarko.jetlytra.client.particle.JetpackParticleHandler;
import pl.siarko.jetlytra.client.particle.ParticleSpawnType;
import pl.siarko.jetlytra.flight.FlightState;
import pl.siarko.jetlytra.flight.FuelData;
import pl.siarko.jetlytra.flight.FuelTypeDefinition;
import pl.siarko.jetlytra.item.JetlytraItems;
import pl.siarko.jetlytra.network.*;

public class JetpackInputHandler {

    // TODO move all these values to server config
    private static final double THRUST_ACCEL = 0.10;
    private static final double MAX_THRUST_VEL = 0.6;
    private static final double THRUST_ACCEL_DOWN = 0.23;
    private static final double HOVER_THRUST_ACCEL = 0.05;
    private static final double HOVER_THRUST_MAX = 0.3;
    private static final double SPRINT_BOOST_ACCEL = 0.05;
    private static final double SPRINT_BOOST_MAX = 0.8;
    private static final double ELYTRA_BOOST_ACCEL = 0.1;
    private static final double ELYTRA_BOOST_MAX = 1.5;
    private static final double SWIM_BOOST_MAX = 0.6;
    private static final long DOUBLE_TAP_WINDOW_MS = 300;

    private long lastSprintPressTime = 0;
    private boolean hoverToggled = false;
    private boolean previousThrustSent = false;
    private boolean wasFalling = false;

    private final KeyStateTracker keyStateTracker;

    public JetpackInputHandler(KeyStateTracker keyStateTracker) {
        this.keyStateTracker = keyStateTracker;
    }

    public void onClientTick(ClientTickEvent.Pre event) {
        var mc = Minecraft.getInstance();
        var player = mc.player;
        if (player == null) return;
        keyStateTracker.update(mc);

        ItemStack jetlytraStack = JetlytraSlotHelper.getWornJetlytra(player);
        boolean jetpackAvailable = JetlytraItems.isJetpackAvailable(jetlytraStack);

        FlightState state = ClientJetpackState.getState();

        handleToggleActions(player, state, jetpackAvailable);

        sendStateChangePackets(player, state, jetpackAvailable);
        if (jetpackAvailable) {
            applyPhysics(player, state, jetlytraStack);
        }
    }

    private void handleToggleActions(Player player, FlightState state, boolean jetpackAvailable) {
        while (JetpackKeyMappings.TOGGLE_JETPACK.consumeClick()) {
            PacketDistributor.sendToServer(new C2SToggleJetpackPacket());
        }

        while (JetpackKeyMappings.TOGGLE_HOVER.consumeClick()) {
            this.hoverToggled = !hoverToggled;
            PacketDistributor.sendToServer(new C2SHoverPacket(this.hoverToggled));
        }

        boolean airborne = !player.onGround() && !player.isInWater();
        while (JetpackKeyMappings.TOGGLE_ELYTRA.consumeClick()) {
            if(airborne) {
                PacketDistributor.sendToServer(new C2SToggleElytraPacket(ToggleType.TOGGLE));
            }
        }
        if (ControlifyBridge.isElytraToggleJustPressed() && airborne) {
            PacketDistributor.sendToServer(new C2SToggleElytraPacket(ToggleType.TOGGLE));
        }

        KeyState jump = keyStateTracker.getJump();
        if (jump.isJustPressed() && !jetpackAvailable && state != FlightState.ELYTRA) {
            boolean notRisingFast = player.getDeltaMovement().y <= 0.1;
            if (airborne && notRisingFast) {
                PacketDistributor.sendToServer(new C2SToggleElytraPacket(ToggleType.ENABLE));
            }
        }
    }

    private void sendStateChangePackets(Player player, FlightState state, boolean jetpackAvailable) {
        boolean thrustNow = thrustKeyActive(player, state) && jetpackAvailable;
        boolean airborne = !player.onGround() && !player.isInWater();

        if (thrustNow != previousThrustSent) {
            previousThrustSent = thrustNow;
            PacketDistributor.sendToServer(new C2SThrustPacket(thrustNow));
        }

        if (keyStateTracker.getCrouch().changed() && !hoverToggled) {
            PacketDistributor.sendToServer(new C2SHoverPacket(keyStateTracker.getCrouch().isActive()));
        }

        KeyState sprint = keyStateTracker.getSprint();
        if (sprint.changed() && sprint.isActive()) {
            long now = System.currentTimeMillis();
            if (
                    now - lastSprintPressTime < DOUBLE_TAP_WINDOW_MS
                            && jetpackAvailable && airborne
                            && (state != FlightState.ELYTRA)
            ) {
                PacketDistributor.sendToServer(new C2SToggleElytraPacket(ToggleType.ENABLE));
            }
            lastSprintPressTime = now;
        }
    }

    // Applies client-side movement physics for all active flight states.
    private void applyPhysics(Player player, FlightState state, ItemStack jetlytraStack) {
        boolean thrustKey = thrustKeyActive(player, state);
        boolean thrusting = state == FlightState.JETPACK && thrustKey;
        boolean hovering = state == FlightState.HOVERING;
        boolean elytraBoost = state == FlightState.ELYTRA && thrustKey;
        boolean swimBoost = player.isSwimming() && thrustKey;

        FuelData fuelData = jetlytraStack.get(JetlytraItems.FUEL_DATA);
        float accelFactor = fuelData != null
                ? fuelData.getDefinition().map(FuelTypeDefinition::accelerationMultiplier).orElse(1.0f)
                : 1.0f;

        ParticleSpawnType particleSpawnType = null;
        if (elytraBoost) {
            particleSpawnType = applyElytraBoost(player, accelFactor);
        } else if (hovering) {
            particleSpawnType = applyHover(player, thrustKey);
        } else if (swimBoost) {
            particleSpawnType = applySwimBoost(player, accelFactor);
        } else if (thrusting) {
            particleSpawnType = applyThrust(player, accelFactor);
        }

        if (keyStateTracker.getSprint().isActive() && thrusting) {
            applyHorizontalBoost(player);
        }

        if (particleSpawnType != null) {
            JetpackParticleHandler.spawnExhaustParticles(particleSpawnType, player);
        }
    }

    private boolean thrustKeyActive(Player player, FlightState state) {
        // We don't want to thrust right away then jump is pressed on the ground.
        // Quick click of jump key should just allow you to jump one block without triggering jetpack
        // .2 is a magical player falling speed, just on the edge where it makes sense to start thrusting
        if(player.getDeltaMovement().y < 0.20) {
            this.wasFalling = true;
        }
        if(player.onGround() && !player.isInWater()) {
            this.wasFalling = false;
        }
        boolean jumpBeforeThrust = JetlytraClientConfig.JUMP_BEFORE_THRUST.get();
        boolean normalThrust = keyStateTracker.getJump().isActive() &&
                (state != FlightState.JETPACK || !jumpBeforeThrust || this.wasFalling);
        boolean airborne = !player.onGround() && !player.isInWater();

        boolean triggerThrust = (state != FlightState.HOVERING) &&
                (airborne || player.isSwimming()) &&
                ControlifyBridge.isTriggerThrusting();
        return normalThrust || triggerThrust;
    }

    private ParticleSpawnType applySwimBoost(Player player, float accelFactor) {
        player.setDeltaMovement(player.getLookAngle().scale(SWIM_BOOST_MAX * accelFactor));
        return ParticleSpawnType.BOOSTING;
    }

    private ParticleSpawnType applyHover(Player player, boolean thrustKey) {
        Vec3 deltaMovement = player.getDeltaMovement();
        double targetY = thrustKey ? Math.min(deltaMovement.y + HOVER_THRUST_ACCEL, HOVER_THRUST_MAX) : 0;
        player.setDeltaMovement(deltaMovement.x, targetY, deltaMovement.z);
        player.resetFallDistance();
        return thrustKey ? ParticleSpawnType.THRUSTING : ParticleSpawnType.HOVERING;
    }

    private ParticleSpawnType applyThrust(Player player, float accelFactor) {
        Vec3 deltaMovement = player.getDeltaMovement();
        double verticalAcceleration;
        if (player.getDeltaMovement().y < 0) {
            verticalAcceleration = deltaMovement.y + THRUST_ACCEL_DOWN;
        } else {
            verticalAcceleration = Math.min(deltaMovement.y + THRUST_ACCEL * accelFactor, MAX_THRUST_VEL * accelFactor);
        }
        player.setDeltaMovement(deltaMovement.x, verticalAcceleration, deltaMovement.z);
        player.resetFallDistance();
        return ParticleSpawnType.THRUSTING;
    }

    private ParticleSpawnType applyElytraBoost(Player player, float accelFactor) {
        Vec3 look = player.getLookAngle();
        Vec3 ev = player.getDeltaMovement();
        double boostMax = ELYTRA_BOOST_MAX * accelFactor;
        player.setDeltaMovement(
                ev.x + look.x * ELYTRA_BOOST_ACCEL * accelFactor + (look.x * boostMax - ev.x) * 0.5,
                ev.y + look.y * ELYTRA_BOOST_ACCEL * accelFactor + (look.y * boostMax - ev.y) * 0.5,
                ev.z + look.z * ELYTRA_BOOST_ACCEL * accelFactor + (look.z * boostMax - ev.z) * 0.5
        );
        return ParticleSpawnType.BOOSTING;
    }

    private void applyHorizontalBoost(Player player) {
        Vec3 look = player.getLookAngle();
        Vec3 lookH = new Vec3(look.x, 0, look.z).normalize();
        Vec3 current = player.getDeltaMovement();
        double newX = current.x + lookH.x * SPRINT_BOOST_ACCEL;
        double newZ = current.z + lookH.z * SPRINT_BOOST_ACCEL;
        double currentSpeed = Math.sqrt(newX * newX + newZ * newZ);
        if (currentSpeed < SPRINT_BOOST_MAX) {
            player.setDeltaMovement(newX, current.y, newZ);
        }
    }
}
