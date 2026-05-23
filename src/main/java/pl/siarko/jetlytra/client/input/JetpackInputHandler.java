package pl.siarko.jetlytra.client.input;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import pl.siarko.jetlytra.JetlytraSlotHelper;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import pl.siarko.jetlytra.client.ClientJetpackState;
import pl.siarko.jetlytra.client.input.integration.ControlifyBridge;
import pl.siarko.jetlytra.config.JetlytraClientConfig;
import pl.siarko.jetlytra.client.particle.JetpackParticleHandler;
import pl.siarko.jetlytra.client.particle.ParticleSpawnType;
import pl.siarko.jetlytra.config.ServerPhysicsConfig;
import pl.siarko.jetlytra.flight.FlightState;
import pl.siarko.jetlytra.flight.FuelData;
import pl.siarko.jetlytra.flight.FuelTypeDefinition;
import pl.siarko.jetlytra.flight.JetpackPhysics;
import pl.siarko.jetlytra.item.JetlytraItems;
import pl.siarko.jetlytra.network.*;

public class JetpackInputHandler {

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

        updateFallState(player);
        boolean thrustKey = isThrustKeyActive(player, state);

        handleToggleActions(player, state, jetpackAvailable);
        sendStateChangePackets(player, state, jetpackAvailable, thrustKey);
        if (jetpackAvailable) {
            applyPhysics(player, state, jetlytraStack, thrustKey);
        }
    }

    private void handleToggleActions(Player player, FlightState state, boolean jetpackAvailable) {
        while (JetpackKeyMappings.TOGGLE_JETPACK.consumeClick()) {
            PacketDistributor.sendToServer(new C2SToggleJetpackPacket());
        }

        // Keep hoverToggled in sync with authoritative server state
        if (state != FlightState.HOVERING) hoverToggled = false;
        while (JetpackKeyMappings.TOGGLE_HOVER.consumeClick()) {
            this.hoverToggled = !hoverToggled;
            PacketDistributor.sendToServer(new C2SHoverPacket(this.hoverToggled));
        }

        boolean airborne = !player.onGround() && !player.isInWater();
        while (JetpackKeyMappings.TOGGLE_ELYTRA.consumeClick()) {
            if (airborne) {
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

    private void sendStateChangePackets(Player player, FlightState state, boolean jetpackAvailable, boolean thrustKey) {
        boolean thrustNow = thrustKey && jetpackAvailable;
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

    private void applyPhysics(Player player, FlightState state, ItemStack jetlytraStack, boolean thrustKey) {
        boolean thrusting = state == FlightState.JETPACK && thrustKey;
        boolean hovering = state == FlightState.HOVERING;
        boolean elytraBoost = state == FlightState.ELYTRA && thrustKey;
        boolean swimBoost = player.isSwimming() && thrustKey;

        FuelData fuelData = jetlytraStack.get(JetlytraItems.FUEL_DATA);
        float accelFactor = fuelData != null
                ? fuelData.getDefinition().map(FuelTypeDefinition::accelerationMultiplier).orElse(1.0f)
                : 1.0f;

        JetpackPhysics.applyPhysics(
                player,
                state,
                thrustKey,
                keyStateTracker.getSprint().isActive(),
                accelFactor,
                ServerPhysicsConfig.physics
        );

        ParticleSpawnType particleType = null;
        if (elytraBoost || swimBoost) {
            particleType = ParticleSpawnType.BOOSTING;
        } else if (hovering) {
            particleType = thrustKey ? ParticleSpawnType.THRUSTING : ParticleSpawnType.HOVERING;
        } else if (thrusting) {
            particleType = ParticleSpawnType.THRUSTING;
        }

        if (particleType != null) {
            JetpackParticleHandler.spawnExhaustParticles(particleType, player);
        }
    }

    private void updateFallState(Player player) {
        // Tracks whether the player was falling before pressing jump, so a quick
        // ground jump doesn't immediately trigger jetpack thrust.
        if (player.getDeltaMovement().y < 0.20) {
            wasFalling = true;
        }
        if (player.onGround() && !player.isInWater()) {
            wasFalling = false;
        }
    }

    private boolean isThrustKeyActive(Player player, FlightState state) {
        boolean jumpBeforeThrust = JetlytraClientConfig.JUMP_BEFORE_THRUST.get();
        boolean normalThrust = keyStateTracker.getJump().isActive() &&
                (state != FlightState.JETPACK || !jumpBeforeThrust || wasFalling);
        boolean airborne = !player.onGround() && !player.isInWater();
        boolean triggerThrust = (state != FlightState.HOVERING) &&
                (airborne || player.isSwimming()) &&
                ControlifyBridge.isTriggerThrusting();
        return normalThrust || triggerThrust;
    }
}
