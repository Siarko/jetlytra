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
    private static final long DOUBLE_TAP_WINDOW_MS = 300;

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

        // Sprint boost: scale XZ when sprinting during active flight
        if (sprint && (state == FlightState.JETPACK || state == FlightState.HOVERING)) {
            Vec3 boosted = player.getDeltaMovement();
            player.setDeltaMovement(boosted.x * SPRINT_BOOST, boosted.y, boosted.z * SPRINT_BOOST);
        }
    }
}
