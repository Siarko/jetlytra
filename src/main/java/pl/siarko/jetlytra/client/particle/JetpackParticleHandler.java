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
        boolean thrusting = state == FlightState.JETPACK && mc.options.keyJump.isDown();
        boolean hovering = state == FlightState.HOVERING;

        if (!thrusting && !hovering) return;
        // Hovering spawns half as often
        if (hovering && player.tickCount % 2 != 0) return;

        // Nozzle positions: behind player's back, chest height, left/right offset
        Vec3 look = player.getLookAngle();
        Vec3 right = new Vec3(-look.z, 0, look.x).normalize().scale(0.2);
        Vec3 back = look.scale(-0.4);
        Vec3 base = player.position().add(0, 0.2, 0).add(back);

        spawnNozzle(mc, player, base.subtract(right));
        spawnNozzle(mc, player, base.add(right));
    }

    private static void spawnNozzle(Minecraft mc, Player player, Vec3 pos) {
        var rand = player.getRandom();
        double vx = (rand.nextDouble() - 0.5) * 0.08;
        double vz = (rand.nextDouble() - 0.5) * 0.08;

        mc.level.addParticle(ParticleTypes.FLAME,
                pos.x, pos.y, pos.z, vx, -0.2, vz);
        mc.level.addParticle(ParticleTypes.LARGE_SMOKE,
                pos.x, pos.y, pos.z, vx * 0.5, -0.15, vz * 0.5);
    }
}
