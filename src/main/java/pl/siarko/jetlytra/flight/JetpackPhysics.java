package pl.siarko.jetlytra.flight;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class JetpackPhysics {

    public static Vec3 applyJetpackThrust(
            Vec3 vel,
            double accelFactor,
            double thrustAccel,
            double thrustAccelDown,
            double maxThrustVel
    ) {
        double newY = vel.y < 0
                ? vel.y + thrustAccelDown
                : Math.min(vel.y + thrustAccel * accelFactor, maxThrustVel * accelFactor);
        return new Vec3(vel.x, newY, vel.z);
    }

    public static Vec3 applyHover(
            Vec3 vel,
            boolean thrustKey,
            double hoverAccel,
            double hoverMax
    ) {
        double targetY = thrustKey ? Math.min(vel.y + hoverAccel, hoverMax) : 0;
        return new Vec3(vel.x, targetY, vel.z);
    }

    public static Vec3 applyElytraBoost(
            Vec3 vel,
            Vec3 look,
            double accelFactor,
            double boostAccel,
            double boostMax
    ) {
        double max = boostMax * accelFactor;
        double accel = boostAccel * accelFactor;
        return new Vec3(
                vel.x + look.x * accel + (look.x * max - vel.x) * 0.5,
                vel.y + look.y * accel + (look.y * max - vel.y) * 0.5,
                vel.z + look.z * accel + (look.z * max - vel.z) * 0.5
        );
    }

    public static Vec3 applySwimBoost(Vec3 look, double accelFactor, double swimBoostMax) {
        return look.scale(swimBoostMax * accelFactor);
    }

    public static Vec3 applyHorizontalBoost(
            Vec3 vel,
            Vec3 lookH,
            double sprintAccel,
            double sprintMax
    ) {
        double newX = vel.x + lookH.x * sprintAccel;
        double newZ = vel.z + lookH.z * sprintAccel;
        if (Math.sqrt(newX * newX + newZ * newZ) < sprintMax) {
            return new Vec3(newX, vel.y, newZ);
        }
        return vel;
    }

    public static void applyPhysics(
            Player player,
            FlightState state,
            boolean thrustKey,
            boolean sprinting,
            double accelFactor,
            JetpackPhysicsConfig cfg
    ) {
        boolean thrusting = state == FlightState.JETPACK && thrustKey;
        boolean hovering = state == FlightState.HOVERING;
        boolean elytraBoost = state == FlightState.ELYTRA && thrustKey;
        boolean swimBoost = player.isSwimming() && thrustKey;

        if (elytraBoost) {
            player.setDeltaMovement(
                    applyElytraBoost(
                            player.getDeltaMovement(),
                            player.getLookAngle(),
                            accelFactor,
                            cfg.elytraBoostAccel(),
                            cfg.elytraBoostMax()
                    )
            );
        } else if (hovering) {
            player.setDeltaMovement(
                    applyHover(
                            player.getDeltaMovement(),
                            thrustKey,
                            cfg.hoverThrustAccel(),
                            cfg.hoverThrustMax()
                    )
            );
            player.resetFallDistance();
        } else if (swimBoost) {
            player.setDeltaMovement(applySwimBoost(player.getLookAngle(), accelFactor, cfg.swimBoostMax()));
        } else if (thrusting) {
            player.setDeltaMovement(
                    applyJetpackThrust(
                            player.getDeltaMovement(),
                            accelFactor,
                            cfg.thrustAccel(),
                            cfg.thrustAccelDown(),
                            cfg.maxThrustVel()
                    )
            );
            player.resetFallDistance();
        }

        if (sprinting && thrusting) {
            Vec3 look = player.getLookAngle();
            Vec3 lookH = new Vec3(look.x, 0, look.z).normalize();
            player.setDeltaMovement(
                    applyHorizontalBoost(
                            player.getDeltaMovement(), lookH, cfg.sprintBoostAccel(), cfg.sprintBoostMax()
                    )
            );
        }
    }
}
