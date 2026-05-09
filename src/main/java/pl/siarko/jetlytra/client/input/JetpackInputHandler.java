package pl.siarko.jetlytra.client.input;

import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import pl.siarko.jetlytra.client.ClientJetpackState;
import pl.siarko.jetlytra.client.particle.JetpackParticleHandler;
import pl.siarko.jetlytra.flight.FlightState;
import pl.siarko.jetlytra.item.JetlytraItems;
import pl.siarko.jetlytra.network.C2SActivateElytraPacket;
import pl.siarko.jetlytra.network.C2SCrouchPacket;
import pl.siarko.jetlytra.network.C2SElytraTogglePacket;
import pl.siarko.jetlytra.network.C2SThrustPacket;
import pl.siarko.jetlytra.network.C2SToggleJetpackPacket;

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
        FlightState state = ClientJetpackState.getState();
        boolean hasFuel = chest.has(JetlytraItems.FUEL_DATA);

        handleToggleActions(player, state, jetpackEnabled, hasFuel);

        if (state == FlightState.OFF) {
            //keyStateTracker.reset();
            return;
        }

        sendStateChangePackets(state, jetpackEnabled, hasFuel);
        if(hasFuel){
            applyPhysics(player, state, jetpackEnabled);
        }
    }

    // Discrete one-shot actions: keybind toggles and fall+jump elytra activation.
    private void handleToggleActions(Player player, FlightState state, boolean jetpackEnabled, boolean hasFuel) {
        while (JetpackKeyMappings.TOGGLE_JETPACK.consumeClick()) {
            PacketDistributor.sendToServer(new C2SToggleJetpackPacket());
        }

        while (JetpackKeyMappings.TOGGLE_ELYTRA.consumeClick()) {
            if (state == FlightState.OFF) {
                PacketDistributor.sendToServer(new C2SActivateElytraPacket());
                System.out.println("[JETLYTRA][HT] Send activate elytra");
            } else if (jetpackEnabled) {
                PacketDistributor.sendToServer(new C2SElytraTogglePacket());
                System.out.println("[JETLYTRA][HT] Send toggle elytra");
            }
        }

        KeyState jump = keyStateTracker.getJump();
        if (jump.changed() && jump.isActive() && (state == FlightState.OFF || !hasFuel)) {
            boolean airborne = !player.onGround() && !player.isSwimming();
            boolean notRisingFast = player.getDeltaMovement().y <= 0.1;
            if (airborne && notRisingFast) {
                PacketDistributor.sendToServer(new C2SActivateElytraPacket());
                System.out.println("[JETLYTRA][HT] Send space-enable elytra");
            }
        }
    }

    // Sends packets when held-key states change (thrust, crouch, sprint double-tap).
    private void sendStateChangePackets(FlightState state, boolean jetpackEnabled, boolean hasFuel) {
        if (keyStateTracker.getJump().changed()) {
            if(hasFuel) {
                PacketDistributor.sendToServer(new C2SThrustPacket(keyStateTracker.getJump().isActive()));
                System.out.println("[JETLYTRA][SC] Send Thrust "+keyStateTracker.getJump().isActive());
            }
        }

        if (keyStateTracker.getCrouch().changed()) {
            PacketDistributor.sendToServer(new C2SCrouchPacket(keyStateTracker.getCrouch().isActive()));
            System.out.println("[JETLYTRA][SC] Send Crouch");
        }

        KeyState sprint = keyStateTracker.getSprint();
        if (sprint.changed() && sprint.isActive()) {
            long now = System.currentTimeMillis();
            if (now - lastSprintPressTime < DOUBLE_TAP_WINDOW_MS
                    && jetpackEnabled
                    && (state == FlightState.JETPACK || state == FlightState.HOVERING)) {
                PacketDistributor.sendToServer(new C2SElytraTogglePacket());
                System.out.println("[JETLYTRA][SC] Send Elytra toggle");
            }
            lastSprintPressTime = now;
        }
    }

    // Applies client-side movement physics for all active flight states.
    private void applyPhysics(Player player, FlightState state, boolean jetpackEnabled) {
        boolean jump = keyStateTracker.getJump().isActive();
        boolean sprint = keyStateTracker.getSprint().isActive();
        boolean thrusting  = state == FlightState.JETPACK  && jump;
        boolean hovering   = state == FlightState.HOVERING;
        boolean elytraBoost = state == FlightState.ELYTRA  && jump && jetpackEnabled;
        boolean swimBoost  = state.isActive() && player.isSwimming() && jump && jetpackEnabled;

        if (swimBoost) {
            player.setDeltaMovement(player.getLookAngle().scale(SWIM_BOOST_MAX));
        }

        Vec3 vel = player.getDeltaMovement();
        if (thrusting) {
            player.setDeltaMovement(vel.x, Math.min(vel.y + THRUST_ACCEL, MAX_THRUST_VEL), vel.z);
            player.resetFallDistance();
        } else if (hovering) {
            player.setDeltaMovement(vel.x, 0, vel.z);
            player.resetFallDistance();
        }

        if (elytraBoost) {
            Vec3 look = player.getLookAngle();
            Vec3 ev = player.getDeltaMovement();
            player.setDeltaMovement(
                ev.x + look.x * ELYTRA_BOOST_ACCEL + (look.x * ELYTRA_BOOST_MAX - ev.x) * 0.5,
                ev.y + look.y * ELYTRA_BOOST_ACCEL + (look.y * ELYTRA_BOOST_MAX - ev.y) * 0.5,
                ev.z + look.z * ELYTRA_BOOST_ACCEL + (look.z * ELYTRA_BOOST_MAX - ev.z) * 0.5
            );
            player.resetFallDistance();
        }

        if (sprint && (state == FlightState.JETPACK || state == FlightState.HOVERING)) {
            Vec3 boosted = player.getDeltaMovement();
            player.setDeltaMovement(boosted.x * SPRINT_BOOST, boosted.y, boosted.z * SPRINT_BOOST);
        }

        JetpackParticleHandler.spawnExhaustParticles(player, thrusting, hovering, elytraBoost, swimBoost);
    }
}
