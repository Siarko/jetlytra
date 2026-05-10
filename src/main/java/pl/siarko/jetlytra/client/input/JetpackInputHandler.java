package pl.siarko.jetlytra.client.input;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import pl.siarko.jetlytra.client.ClientJetpackState;
import pl.siarko.jetlytra.client.particle.JetpackParticleHandler;
import pl.siarko.jetlytra.client.particle.ParticleSpawnType;
import pl.siarko.jetlytra.flight.FlightState;
import pl.siarko.jetlytra.item.JetlytraItems;
import pl.siarko.jetlytra.network.*;

public class JetpackInputHandler {

    // Jetpack UP acceleration
    private final double THRUST_ACCEL = 0.15;
    private final double MAX_THRUST_VEL = 0.8;

    // When uses jetpack, he gains some boost to movement
    private final double SPRINT_BOOST = 1.08;

    private final long DOUBLE_TAP_WINDOW_MS = 300;
    private final double ELYTRA_BOOST_ACCEL = 0.1;
    private final double ELYTRA_BOOST_MAX = 1.5;
    private final double SWIM_BOOST_MAX = 0.6;

    private long lastSprintPressTime = 0;

    private final KeyStateTracker keyStateTracker;

    public JetpackInputHandler(KeyStateTracker keyStateTracker) {
        this.keyStateTracker = keyStateTracker;
    }

    public void onClientTick(ClientTickEvent.Pre event) {
        var mc = Minecraft.getInstance();
        var player = mc.player;
        if (player == null) return;
        keyStateTracker.update(mc);

        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        boolean jetpackEnabled = Boolean.TRUE.equals(chest.get(JetlytraItems.JETPACK_ENABLED));
        boolean hasFuel = chest.has(JetlytraItems.FUEL_DATA);
        boolean jetpackAvailable = jetpackEnabled && hasFuel;

        FlightState state = ClientJetpackState.getState();

        handleToggleActions(player, state, jetpackAvailable);

        sendStateChangePackets(player, state, jetpackAvailable);
        if (jetpackAvailable) {
            applyPhysics(player, state);
        } else {
            ClientJetpackState.setThrustActive(false);
        }
    }

    // Discrete one-shot actions: keybind toggles and fall+jump elytra activation.
    private void handleToggleActions(Player player, FlightState state, boolean jetpackAvailable) {
        while (JetpackKeyMappings.TOGGLE_JETPACK.consumeClick()) {
            PacketDistributor.sendToServer(new C2SToggleJetpackPacket());
        }

        while (JetpackKeyMappings.TOGGLE_ELYTRA.consumeClick()) {
            PacketDistributor.sendToServer(new C2SToggleElytraPacket(ToggleType.TOGGLE));
        }

        KeyState jump = keyStateTracker.getJump();
        boolean airborne = !player.onGround() && !player.isInWater();
        if (jump.isJustPressed() && !jetpackAvailable && state != FlightState.ELYTRA) {
            boolean notRisingFast = player.getDeltaMovement().y <= 0.1;
            if (airborne && notRisingFast) {
                PacketDistributor.sendToServer(new C2SToggleElytraPacket(ToggleType.ENABLE));
            }
        }
    }

    // Sends packets when held-key states change (thrust, crouch, sprint double-tap).
    private void sendStateChangePackets(Player player, FlightState state, boolean jetpackAvailable) {
        if (keyStateTracker.getJump().changed()) {
            PacketDistributor.sendToServer(new C2SThrustPacket(
                    keyStateTracker.getJump().isActive() && jetpackAvailable
            ));
        }

        if (keyStateTracker.getCrouch().changed()) {
            PacketDistributor.sendToServer(new C2SCrouchPacket(
                    keyStateTracker.getCrouch().isActive() && jetpackAvailable
            ));
        }

        KeyState sprint = keyStateTracker.getSprint();
        boolean airborne = !player.onGround() && !player.isInWater();
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
    private void applyPhysics(Player player, FlightState state) {

        boolean jump = keyStateTracker.getJump().isActive();

        boolean thrusting = state == FlightState.JETPACK && jump;
        boolean hovering = state == FlightState.HOVERING;
        boolean elytraBoost = state == FlightState.ELYTRA && jump;
        boolean swimBoost = player.isSwimming() && jump;
        Vec3 deltaMovement = player.getDeltaMovement();

        ParticleSpawnType particleSpawnType = null;
        if (elytraBoost) {
            Vec3 look = player.getLookAngle();
            Vec3 ev = player.getDeltaMovement();
            player.setDeltaMovement(
                    ev.x + look.x * ELYTRA_BOOST_ACCEL + (look.x * ELYTRA_BOOST_MAX - ev.x) * 0.5,
                    ev.y + look.y * ELYTRA_BOOST_ACCEL + (look.y * ELYTRA_BOOST_MAX - ev.y) * 0.5,
                    ev.z + look.z * ELYTRA_BOOST_ACCEL + (look.z * ELYTRA_BOOST_MAX - ev.z) * 0.5
            );
            particleSpawnType = ParticleSpawnType.BOOSTING;
        } else if (hovering) {
            player.setDeltaMovement(deltaMovement.x, 0, deltaMovement.z);
            player.resetFallDistance();
            particleSpawnType = ParticleSpawnType.HOVERING;
        } else if (swimBoost) {
            player.setDeltaMovement(player.getLookAngle().scale(SWIM_BOOST_MAX));
            particleSpawnType = ParticleSpawnType.BOOSTING;
        } else if (thrusting) {
            player.setDeltaMovement(deltaMovement.x, Math.min(deltaMovement.y + THRUST_ACCEL, MAX_THRUST_VEL), deltaMovement.z);
            player.resetFallDistance();
            particleSpawnType = ParticleSpawnType.THRUSTING;
        }

        boolean sprint = keyStateTracker.getSprint().isActive();
        if (sprint && thrusting) {
            Vec3 boosted = player.getDeltaMovement();
            player.setDeltaMovement(boosted.x * SPRINT_BOOST, boosted.y, boosted.z * SPRINT_BOOST);
        }

        ClientJetpackState.setThrustActive(particleSpawnType != null);
        if(particleSpawnType != null) {
            JetpackParticleHandler.spawnExhaustParticles(particleSpawnType, player);
        }
    }
}
