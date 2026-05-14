package pl.siarko.jetlytra.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class JetlytraClientConfig {

    public static final ModConfigSpec SPEC;

    // Fuel percentage widget
    public static final ModConfigSpec.BooleanValue SHOW_FUEL_PERCENTAGE;
    public static final ModConfigSpec.DoubleValue FUEL_PERCENT_X;
    public static final ModConfigSpec.DoubleValue FUEL_PERCENT_Y;

    // Fuel gauge widget
    public static final ModConfigSpec.BooleanValue SHOW_FUEL_GAUGE;
    public static final ModConfigSpec.DoubleValue FUEL_GAUGE_X;
    public static final ModConfigSpec.DoubleValue FUEL_GAUGE_Y;

    // Fuel warning widget
    public static final ModConfigSpec.BooleanValue SHOW_FUEL_WARNING;
    public static final ModConfigSpec.IntValue FUEL_WARNING_LEVEL;
    public static final ModConfigSpec.DoubleValue FUEL_WARNING_X;
    public static final ModConfigSpec.DoubleValue FUEL_WARNING_Y;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.push("hud");

        builder.push("fuelPercentage");
        SHOW_FUEL_PERCENTAGE = builder.comment("Show fuel percentage text").define("enabled", true);
        FUEL_PERCENT_X = builder.comment("HUD center X (0.0 = left, 1.0 = right)").defineInRange("x", 0.5, 0.0, 1.0);
        FUEL_PERCENT_Y = builder.comment("HUD center Y (0.0 = top, 1.0 = bottom)").defineInRange("y", 0.91, 0.0, 1.0);
        builder.pop();

        builder.push("fuelGauge");
        SHOW_FUEL_GAUGE = builder.comment("Show fuel gauge bar").define("enabled", true);
        FUEL_GAUGE_X = builder.defineInRange("x", 0.5, 0.0, 1.0);
        FUEL_GAUGE_Y = builder.defineInRange("y", 0.87, 0.0, 1.0);
        builder.pop();

        builder.push("fuelWarning");
        SHOW_FUEL_WARNING = builder.comment("Show flashing warning when fuel is low").define("enabled", true);
        FUEL_WARNING_LEVEL = builder.comment("Fuel % at which the warning activates (0-100)").defineInRange("level", 20, 0, 100);
        FUEL_WARNING_X = builder.defineInRange("x", 0.5, 0.0, 1.0);
        FUEL_WARNING_Y = builder.defineInRange("y", 0.84, 0.0, 1.0);
        builder.pop();

        builder.pop(); // hud
        SPEC = builder.build();
    }
}
