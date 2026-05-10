package pl.siarko.jetlytra.client.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import pl.siarko.jetlytra.flight.FuelData;
import pl.siarko.jetlytra.flight.FuelType;
import pl.siarko.jetlytra.item.JetlytraItems;

public class JetpackParticleHandler {

    public static void spawnExhaustParticles(ParticleSpawnType particleSpawnType, Player player) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        FuelData fuelData = chest.get(JetlytraItems.FUEL_DATA);
        if (fuelData == null) return;
        FuelType fuelType = fuelData.type();

        Vec3 look = player.getLookAngle();
        float yawRad = (float) Math.toRadians(player.yBodyRot);
        Vec3 front = new Vec3(-Math.sin(yawRad), 0, Math.cos(yawRad));
        Vec3 right = new Vec3(Math.cos(yawRad), 0, Math.sin(yawRad)).scale(0.2);
        Vec3 base = player.position().subtract(front.scale(0.4)).add(0,0.4,0);

        boolean directional = particleSpawnType == ParticleSpawnType.BOOSTING;
        boolean hovering = particleSpawnType == ParticleSpawnType.HOVERING;

        if (player.isInWater()) {
            if (!hovering) {
                spawnBubbleNozzle(mc, player, base.subtract(right), directional ? look : null);
                spawnBubbleNozzle(mc, player, base.add(right), directional ? look : null);
            }
        } else if (directional) {
            spawnBoostNozzle(mc, player, fuelType, base.subtract(right), look);
            spawnBoostNozzle(mc, player, fuelType, base.add(right), look);
        } else {
            if(hovering) {
                base = base.subtract(front.scale(0.25));
            }
            spawnNozzle(mc, player, fuelType, base.subtract(right));
            spawnNozzle(mc, player, fuelType, base.add(right));
        }
    }

    private static void spawnNozzle(Minecraft mc, Player player, FuelType fuelType, Vec3 pos) {
        if (mc.level == null) return;
        var rand = player.getRandom();
        double vx = (rand.nextDouble() - 0.5) * 0.08;
        double vz = (rand.nextDouble() - 0.5) * 0.08;

        mc.level.addParticle(fuelType.exhaustParticle, pos.x, pos.y, pos.z, vx, -0.4, vz);
        mc.level.addParticle(fuelType.trailParticle, pos.x, pos.y, pos.z, vx * 0.5, -0.3, vz * 0.5);
    }

    private static void spawnBubbleNozzle(Minecraft mc, Player player, Vec3 pos, Vec3 look) {
        if (mc.level == null) return;
        var rand = player.getRandom();
        double vx, vy, vz;
        if (look != null) {
            Vec3 exhaust = look.scale(-0.3);
            vx = exhaust.x + (rand.nextDouble() - 0.5) * 0.06;
            vy = exhaust.y + (rand.nextDouble() - 0.5) * 0.06;
            vz = exhaust.z + (rand.nextDouble() - 0.5) * 0.06;
        } else {
            vx = (rand.nextDouble() - 0.5) * 0.08;
            vy = -0.4;
            vz = (rand.nextDouble() - 0.5) * 0.08;
        }
        mc.level.addParticle(ParticleTypes.BUBBLE, pos.x, pos.y, pos.z, vx, vy, vz);
    }

    private static void spawnBoostNozzle(Minecraft mc, Player player, FuelType fuelType, Vec3 pos, Vec3 look) {
        if (mc.level == null) return;
        var rand = player.getRandom();
        Vec3 exhaust = look.scale(-0.4);
        double vx = exhaust.x + (rand.nextDouble() - 0.5) * 0.08;
        double vy = exhaust.y + (rand.nextDouble() - 0.5) * 0.08;
        double vz = exhaust.z + (rand.nextDouble() - 0.5) * 0.08;

        mc.level.addParticle(fuelType.boostParticle, pos.x, pos.y, pos.z, vx, vy, vz);
    }
}
