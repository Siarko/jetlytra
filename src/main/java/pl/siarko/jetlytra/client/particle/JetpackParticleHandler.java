package pl.siarko.jetlytra.client.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import pl.siarko.jetlytra.client.ClientJetpackState;
import pl.siarko.jetlytra.flight.FlightState;

public class JetpackParticleHandler {

    public static void onClientTick(ClientTickEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || mc.level == null) return;

        FlightState state = ClientJetpackState.getState();
        boolean jumping = mc.options.keyJump.isDown();
        boolean thrusting = state == FlightState.JETPACK && jumping;
        boolean hovering = state == FlightState.HOVERING;
        boolean elytraBoost = state == FlightState.ELYTRA && jumping;
        boolean swimBoost = state.isActive() && player.isSwimming() && jumping;

        if (!thrusting && !hovering && !elytraBoost && !swimBoost) return;

        Vec3 look = player.getLookAngle();
        Vec3 right = new Vec3(-look.z, 0, look.x).normalize().scale(0.2);
        Vec3 back = look.scale(-0.4);
        Vec3 base = player.position().add(0, 0.2, 0).add(back);

        boolean directional = elytraBoost || swimBoost; // exhaust shoots backward; otherwise downward
        boolean inWater = player.isInWater();

        if (inWater) {
            if(!hovering) {
                spawnBubbleNozzle(mc, player, base.subtract(right), directional ? look : null);
                spawnBubbleNozzle(mc, player, base.add(right), directional ? look : null);
            }
        } else if (directional) {
            spawnBoostNozzle(mc, player, base.subtract(right), look);
            spawnBoostNozzle(mc, player, base.add(right), look);
        } else {
            spawnNozzle(mc, player, base.subtract(right));
            spawnNozzle(mc, player, base.add(right));
        }
    }

    private static void spawnNozzle(Minecraft mc, Player player, Vec3 pos) {
        var rand = player.getRandom();
        double vx = (rand.nextDouble() - 0.5) * 0.08;
        double vz = (rand.nextDouble() - 0.5) * 0.08;

        mc.level.addParticle(ParticleTypes.FLAME,
                pos.x, pos.y, pos.z, vx, -0.2, vz);
        mc.level.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                pos.x, pos.y, pos.z, vx * 0.5, -0.15, vz * 0.5);
    }

    private static void spawnBubbleNozzle(Minecraft mc, Player player, Vec3 pos, Vec3 look) {
        var rand = player.getRandom();
        double vx, vy, vz;
        if (look != null) {
            // Directional: exhaust shoots backward opposite to look
            Vec3 exhaust = look.scale(-0.3);
            vx = exhaust.x + (rand.nextDouble() - 0.5) * 0.06;
            vy = exhaust.y + (rand.nextDouble() - 0.5) * 0.06;
            vz = exhaust.z + (rand.nextDouble() - 0.5) * 0.06;
        } else {
            // Downward: like normal thrust but bubbles
            vx = (rand.nextDouble() - 0.5) * 0.08;
            vy = -0.2;
            vz = (rand.nextDouble() - 0.5) * 0.08;
        }
        mc.level.addParticle(ParticleTypes.BUBBLE, pos.x, pos.y, pos.z, vx, vy, vz);
    }

    private static void spawnBoostNozzle(Minecraft mc, Player player, Vec3 pos, Vec3 look) {
        var rand = player.getRandom();
        // Exhaust shoots backward opposite to look direction
        Vec3 exhaust = look.scale(-0.4);
        double vx = exhaust.x + (rand.nextDouble() - 0.5) * 0.08;
        double vy = exhaust.y + (rand.nextDouble() - 0.5) * 0.08;
        double vz = exhaust.z + (rand.nextDouble() - 0.5) * 0.08;

        mc.level.addParticle(ParticleTypes.FLAME,
                pos.x, pos.y, pos.z, vx, vy, vz);
    }
}
