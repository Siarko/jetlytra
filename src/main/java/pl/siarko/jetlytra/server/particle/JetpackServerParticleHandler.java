package pl.siarko.jetlytra.server.particle;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import pl.siarko.jetlytra.flight.FlightState;
import pl.siarko.jetlytra.flight.FuelTypeDefinition;

public class JetpackServerParticleHandler {

    public static void spawnExhaustParticles(ServerPlayer player, FuelTypeDefinition fuelDef, FlightState state) {
        ServerLevel level = player.serverLevel();
        float yawRad = (float) Math.toRadians(player.yBodyRot);
        Vec3 front = new Vec3(-Math.sin(yawRad), 0, Math.cos(yawRad));
        Vec3 right = new Vec3(Math.cos(yawRad), 0, Math.sin(yawRad)).scale(0.2);
        Vec3 base = player.position().subtract(front.scale(0.4)).add(0, 0.4, 0);
        Vec3 look = player.getLookAngle();

        if (player.isInWater()) {
            spawnBubbleNozzle(level, player, base.subtract(right), look);
            spawnBubbleNozzle(level, player, base.add(right), look);
        } else if (state == FlightState.ELYTRA) {
            spawnBoostNozzle(level, player, fuelDef, base.subtract(right), look);
            spawnBoostNozzle(level, player, fuelDef, base.add(right), look);
        } else {
            if (state == FlightState.HOVERING) base = base.subtract(front.scale(0.25));
            spawnNozzle(level, player, fuelDef, base.subtract(right));
            spawnNozzle(level, player, fuelDef, base.add(right));
        }
    }

    private static void sendToOthers(ServerLevel level, ServerPlayer exclude, ParticleOptions particle,
                                     double x, double y, double z, double vx, double vy, double vz) {
        for (ServerPlayer target : level.players()) {
            if (!target.equals(exclude)) {
                level.sendParticles(target, particle, false, x, y, z, 0, vx, vy, vz, 1.0);
            }
        }
    }

    private static void spawnNozzle(ServerLevel level, ServerPlayer exclude, FuelTypeDefinition fuelDef, Vec3 pos) {
        var rand = exclude.getRandom();
        double vx = (rand.nextDouble() - 0.5) * 0.08;
        double vz = (rand.nextDouble() - 0.5) * 0.08;
        if (fuelDef != null && fuelDef.exhaustParticle() != null)
            sendToOthers(level, exclude, fuelDef.exhaustParticle(), pos.x, pos.y, pos.z, vx, -0.4, vz);
        if (fuelDef != null && fuelDef.trailParticle() != null)
            sendToOthers(level, exclude, fuelDef.trailParticle(), pos.x, pos.y, pos.z, vx * 0.5, -0.3, vz * 0.5);
    }

    private static void spawnBubbleNozzle(ServerLevel level, ServerPlayer exclude, Vec3 pos, Vec3 look) {
        var rand = exclude.getRandom();
        Vec3 exhaust = look.scale(-0.3);
        double vx = exhaust.x + (rand.nextDouble() - 0.5) * 0.06;
        double vy = exhaust.y + (rand.nextDouble() - 0.5) * 0.06;
        double vz = exhaust.z + (rand.nextDouble() - 0.5) * 0.06;
        sendToOthers(level, exclude, ParticleTypes.BUBBLE, pos.x, pos.y, pos.z, vx, vy, vz);
    }

    private static void spawnBoostNozzle(ServerLevel level, ServerPlayer exclude, FuelTypeDefinition fuelDef, Vec3 pos, Vec3 look) {
        if (fuelDef == null || fuelDef.boostParticle() == null) return;
        var rand = exclude.getRandom();
        Vec3 exhaust = look.scale(-0.4);
        double vx = exhaust.x + (rand.nextDouble() - 0.5) * 0.08;
        double vy = exhaust.y + (rand.nextDouble() - 0.5) * 0.08;
        double vz = exhaust.z + (rand.nextDouble() - 0.5) * 0.08;
        sendToOthers(level, exclude, fuelDef.boostParticle(), pos.x, pos.y, pos.z, vx, vy, vz);
    }
}
