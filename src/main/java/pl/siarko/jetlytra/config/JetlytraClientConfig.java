package pl.siarko.jetlytra.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class JetlytraClientConfig {

    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.DoubleValue HUD_X;
    public static final ModConfigSpec.DoubleValue HUD_Y;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.push("hud");
        HUD_X = builder.comment("HUD center X position (0.0 = left edge, 1.0 = right edge)")
                       .defineInRange("x", 0.5, 0.0, 1.0);
        HUD_Y = builder.comment("HUD center Y position (0.0 = top edge, 1.0 = bottom edge)")
                       .defineInRange("y", 0.91, 0.0, 1.0);
        builder.pop();
        SPEC = builder.build();
    }
}
