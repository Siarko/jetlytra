package pl.siarko.jetlytra.config;

public class ClientServerConfig {
    // Defaults match JetlytraServerConfig so singleplayer works without receiving a sync packet
    public static double thrustAccel = 0.10;
    public static double maxThrustVel = 0.6;
    public static double thrustAccelDown = 0.23;
    public static double hoverThrustAccel = 0.05;
    public static double hoverThrustMax = 0.3;
    public static double elytraBoostAccel = 0.1;
    public static double elytraBoostMax = 1.5;
    public static double swimBoostMax = 0.6;
    public static double sprintBoostAccel = 0.05;
    public static double sprintBoostMax = 0.8;
}
