package pl.siarko.jetlytra.client.input;

import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import pl.siarko.jetlytra.client.ClientJetpackState;
import pl.siarko.jetlytra.flight.FlightState;
import pl.siarko.jetlytra.network.C2SCrouchPacket;
import pl.siarko.jetlytra.network.C2SElytraTogglePacket;
import pl.siarko.jetlytra.network.C2SThrustPacket;
import pl.siarko.jetlytra.network.C2SToggleJetpackPacket;

public class JetpackInputHandler {

    private static final double THRUST_ACCEL = 0.15;
    private static final double MAX_THRUST_VEL = 0.8;
    private static final double SPRINT_BOOST = 1.08;
    private static final double ELYTRA_BOOST_ACCEL = 0.1;
    private static final double ELYTRA_BOOST_MAX = 1.5;
    private static final long DOUBLE_TAP_WINDOW_MS = 300;
    private static final double SWIM_BOOST_MAX = 0.6;

    private static boolean lastThrust = false;
    private static boolean lastCrouch = false;
    private static boolean lastSprint = false;
    private static long lastSprintPressTime = 0;


    public static void onClientTick(ClientTickEvent.Pre event) {
        var mc = Minecraft.getInstance();
        var options = mc.options;
        var player = mc.player;

        if (player == null) return;

        // Toggle jetpack on/off
        while (JetpackKeyMappings.TOGGLE_JETPACK.consumeClick()) {
            PacketDistributor.sendToServer(new C2SToggleJetpackPacket());
        }

        // Elytra toggle dedicated keybind
        while (JetpackKeyMappings.TOGGLE_ELYTRA.consumeClick()) {
            PacketDistributor.sendToServer(new C2SElytraTogglePacket());
        }

        // Swimming boost: accelerate in look direction when jump held while swimming (jetpack must be active)
        if (ClientJetpackState.getState().isActive() && player.isSwimming() && options.keyJump.isDown()) {
            Vec3 look = player.getLookAngle();
            player.setDeltaMovement(look.scale(SWIM_BOOST_MAX));
        }

        FlightState state = ClientJetpackState.getState();
        if (state == FlightState.OFF) {
            lastThrust = false;
            lastCrouch = false;
            lastSprint = false;
            return;
        }

        // Thrust: send only on state change, and apply physics locally
        boolean thrust = options.keyJump.isDown();
        if (thrust != lastThrust) {
            PacketDistributor.sendToServer(new C2SThrustPacket(thrust));
            lastThrust = thrust;
        }

        // Crouch / hover: send only on state change
        boolean crouch = options.keyShift.isDown();
        if (crouch != lastCrouch) {
            PacketDistributor.sendToServer(new C2SCrouchPacket(crouch));
            lastCrouch = crouch;
        }

        // Sprint: double-tap detection for elytra activation
        boolean sprint = options.keySprint.isDown();
        if (sprint && !lastSprint) {
            long now = System.currentTimeMillis();
            if ((state == FlightState.JETPACK || state == FlightState.HOVERING)
                    && now - lastSprintPressTime < DOUBLE_TAP_WINDOW_MS) {
                PacketDistributor.sendToServer(new C2SElytraTogglePacket());
            }
            lastSprintPressTime = now;
        }
        lastSprint = sprint;

        // Apply physics client-side (Minecraft player movement is client-authoritative)
        Vec3 vel = player.getDeltaMovement();
        if (state == FlightState.JETPACK && thrust) {
            double newY = Math.min(vel.y + THRUST_ACCEL, MAX_THRUST_VEL);
            player.setDeltaMovement(vel.x, newY, vel.z);
            player.resetFallDistance();
        } else if (state == FlightState.HOVERING) {
            // Gravity is disabled server-side via setNoGravity(true) — just zero Y
            player.setDeltaMovement(vel.x, 0, vel.z);
            player.resetFallDistance();
        }

        // Elytra boost: accelerate in look direction when jump held, capped at firework speed
        if (state == FlightState.ELYTRA && thrust) {
            Vec3 look = player.getLookAngle();
            Vec3 elytraVel = player.getDeltaMovement();
            player.setDeltaMovement(
                elytraVel.x + look.x * ELYTRA_BOOST_ACCEL + (look.x * ELYTRA_BOOST_MAX - elytraVel.x) * 0.5,
                elytraVel.y + look.y * ELYTRA_BOOST_ACCEL + (look.y * ELYTRA_BOOST_MAX - elytraVel.y) * 0.5,
                elytraVel.z + look.z * ELYTRA_BOOST_ACCEL + (look.z * ELYTRA_BOOST_MAX - elytraVel.z) * 0.5
            );
            player.resetFallDistance();
        }

        // Sprint boost: scale XZ when sprinting during active flight
        if (sprint && (state == FlightState.JETPACK || state == FlightState.HOVERING)) {
            Vec3 boosted = player.getDeltaMovement();
            player.setDeltaMovement(boosted.x * SPRINT_BOOST, boosted.y, boosted.z * SPRINT_BOOST);
        }
    }
}
