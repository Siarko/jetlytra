package pl.siarko.jetlytra.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class JetlytraClientConfig {

    public static final ModConfigSpec SPEC;

    private static final boolean DEFAULT_PERCENT_ENABLED = false;
    private static final boolean DEFAULT_GAUGE_ENABLED = true;
    private static final boolean DEFAULT_WARNING_ENABLED = true;
    private static final int DEFAULT_WARNING_LEVEL = 2;

    // Fuel percentage widget
    public static final ModConfigSpec.BooleanValue SHOW_FUEL_PERCENTAGE;
    public static final ModConfigSpec.DoubleValue FUEL_PERCENT_X;
    public static final ModConfigSpec.DoubleValue FUEL_PERCENT_Y;
    public static final ModConfigSpec.DoubleValue FUEL_PERCENT_SCALE;

    // Fuel gauge widget
    public static final ModConfigSpec.BooleanValue SHOW_FUEL_GAUGE;
    public static final ModConfigSpec.DoubleValue FUEL_GAUGE_X;
    public static final ModConfigSpec.DoubleValue FUEL_GAUGE_Y;
    public static final ModConfigSpec.DoubleValue FUEL_GAUGE_SCALE;

    // Fuel warning widget
    public static final ModConfigSpec.BooleanValue SHOW_FUEL_WARNING;
    public static final ModConfigSpec.IntValue FUEL_WARNING_LEVEL;
    public static final ModConfigSpec.DoubleValue FUEL_WARNING_X;
    public static final ModConfigSpec.DoubleValue FUEL_WARNING_Y;
    public static final ModConfigSpec.DoubleValue FUEL_WARNING_SCALE;

    // Movement
    public static final ModConfigSpec.BooleanValue JUMP_BEFORE_THRUST;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.push("hud");

        builder.push("fuelPercentage");
        SHOW_FUEL_PERCENTAGE = builder.comment("Show fuel percentage text")
                .define("enabled", DEFAULT_PERCENT_ENABLED);
        FUEL_PERCENT_X = builder.comment("HUD center X (0.0 = left, 1.0 = right)")
                .defineInRange("x", 0.01, 0.0, 1.0);
        FUEL_PERCENT_Y = builder.comment("HUD center Y (0.0 = top, 1.0 = bottom)")
                .defineInRange("y", 0.065, 0.0, 1.0);
        FUEL_PERCENT_SCALE = builder.defineInRange("scale", 1.0, 0.25, 4.0);
        builder.pop();

        builder.push("fuelGauge");
        SHOW_FUEL_GAUGE = builder.comment("Show fuel gauge bar")
                .define("enabled", DEFAULT_GAUGE_ENABLED);
        FUEL_GAUGE_X = builder.defineInRange("x", 0.01, 0.0, 1.0);
        FUEL_GAUGE_Y = builder.defineInRange("y", 0.03, 0.0, 1.0);
        FUEL_GAUGE_SCALE = builder.defineInRange("scale", 0.5, 0.25, 4.0);
        builder.pop();

        builder.push("fuelWarning");
        SHOW_FUEL_WARNING = builder.comment("Show flashing warning when fuel is low")
                .define("enabled", DEFAULT_WARNING_ENABLED);
        FUEL_WARNING_LEVEL = builder.comment("Fuel % at which the warning activates (0-100)")
                .defineInRange("level", DEFAULT_WARNING_LEVEL, 0, 100);
        FUEL_WARNING_X = builder.defineInRange("x", 0.5, 0.0, 1.0);
        FUEL_WARNING_Y = builder.defineInRange("y", 0.04, 0.0, 1.0);
        FUEL_WARNING_SCALE = builder.defineInRange("scale", 3.0, 0.25, 5.0);
        builder.pop();

        builder.pop(); // hud

        builder.push("movement");
        JUMP_BEFORE_THRUST = builder
                .comment("When enabled, tapping jump on the ground lets you jump normally without activating the jetpack. Hold jump while airborne to thrust.")
                .define("jumpBeforeThrust", true);
        builder.pop(); // movement

        SPEC = builder.build();
    }
}
