package pl.siarko.jetlytra.config;

import pl.siarko.jetlytra.flight.JetpackPhysicsConfig;

public class ServerPhysicsConfig {
    // Defaults match JetlytraServerConfig so singleplayer works without receiving a sync packet
    public static JetpackPhysicsConfig physics = defaults();

    private static JetpackPhysicsConfig defaults() {
        JetpackPhysicsConfig cfg = new JetpackPhysicsConfig();
        cfg.setThrustAccel(0.10);
        cfg.setThrustAccelDown(0.23);
        cfg.setMaxThrustVel(0.6);
        cfg.setHoverThrustAccel(0.05);
        cfg.setHoverThrustMax(0.3);
        cfg.setElytraBoostAccel(0.1);
        cfg.setElytraBoostMax(1.5);
        cfg.setSwimBoostMax(0.6);
        cfg.setSprintBoostAccel(0.05);
        cfg.setSprintBoostMax(0.8);
        return cfg;
    }
}
