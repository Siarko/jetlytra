package pl.siarko.jetlytra.flight;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class HoverController {

    // Gravity constant in Minecraft: 0.08 blocks/tick²
    private static final double GRAVITY = 0.08;
    // XZ drag while hovering — slows horizontal drift
    private static final double XZ_DRAG = 0.85;

    public static void applyHover(Player player) {
        Vec3 vel = player.getDeltaMovement();
        // Set Y to +GRAVITY so that after travel() subtracts gravity, net Y = 0
        player.setDeltaMovement(vel.x * XZ_DRAG, GRAVITY, vel.z * XZ_DRAG);
        player.resetFallDistance();
    }
}
