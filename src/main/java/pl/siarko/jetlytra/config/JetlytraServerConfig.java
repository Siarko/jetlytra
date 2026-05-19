package pl.siarko.jetlytra.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class JetlytraServerConfig {

    public static final ModConfigSpec SPEC;

    // Jetpack mode
    public static final ModConfigSpec.DoubleValue THRUST_ACCEL;
    public static final ModConfigSpec.DoubleValue MAX_THRUST_VEL;
    public static final ModConfigSpec.DoubleValue THRUST_ACCEL_DOWN;

    // Hover mode
    public static final ModConfigSpec.DoubleValue HOVER_THRUST_ACCEL;
    public static final ModConfigSpec.DoubleValue HOVER_THRUST_MAX;

    // Elytra mode
    public static final ModConfigSpec.DoubleValue ELYTRA_BOOST_ACCEL;
    public static final ModConfigSpec.DoubleValue ELYTRA_BOOST_MAX;

    // Other
    public static final ModConfigSpec.DoubleValue SWIM_BOOST_MAX;
    public static final ModConfigSpec.DoubleValue SPRINT_BOOST_ACCEL;
    public static final ModConfigSpec.DoubleValue SPRINT_BOOST_MAX;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.push("jetpackMode");
        THRUST_ACCEL = builder
                .comment("Vertical thrust acceleration per tick while thrusting upward")
                .defineInRange("thrustAccel", 0.10, 0.01, 1.0);
        MAX_THRUST_VEL = builder
                .comment("Maximum vertical speed while thrusting upward")
                .defineInRange("maxThrustVel", 0.6, 0.1, 5.0);
        THRUST_ACCEL_DOWN = builder
                .comment("Vertical acceleration per tick applied when player is falling — higher than thrustAccel to feel natural")
                .defineInRange("thrustAccelDown", 0.23, 0.01, 1.0);
        builder.pop();

        builder.push("hoverMode");
        HOVER_THRUST_ACCEL = builder
                .comment("Vertical acceleration per tick while thrusting in hover mode — kept small for precise control")
                .defineInRange("hoverThrustAccel", 0.05, 0.01, 0.5);
        HOVER_THRUST_MAX = builder
                .comment("Maximum vertical speed while thrusting in hover mode")
                .defineInRange("hoverThrustMax", 0.3, 0.01, 2.0);
        builder.pop();

        builder.push("elytraMode");
        ELYTRA_BOOST_ACCEL = builder
                .comment("Acceleration applied each tick while boosting in elytra mode")
                .defineInRange("elytraBoostAccel", 0.1, 0.01, 0.5);
        ELYTRA_BOOST_MAX = builder
                .comment("Top speed to which elytra boost is applied — 1.5 is approximately firework boost speed")
                .defineInRange("elytraBoostMax", 1.5, 0.1, 5.0);
        builder.pop();

        builder.push("other");
        SWIM_BOOST_MAX = builder
                .comment("Maximum speed applied when using the jetpack while swimming")
                .defineInRange("swimBoostMax", 0.6, 0.1, 5.0);
        SPRINT_BOOST_ACCEL = builder
                .comment("Horizontal speed boost per tick while thrusting and sprinting")
                .defineInRange("sprintBoostAccel", 0.05, 0.01, 0.5);
        SPRINT_BOOST_MAX = builder
                .comment("Top speed to which horizontal sprint boost is applied")
                .defineInRange("sprintBoostMax", 0.8, 0.1, 5.0);
        builder.pop();

        SPEC = builder.build();
    }
}
