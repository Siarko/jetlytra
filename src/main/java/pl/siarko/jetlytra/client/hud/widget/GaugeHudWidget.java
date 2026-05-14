package pl.siarko.jetlytra.client.hud.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.ModConfigSpec;

public class GaugeHudWidget extends HudWidget {

    public static final ResourceLocation GAUGE_BG = ResourceLocation.fromNamespaceAndPath(
            "jetlytra",
            "textures/hud/fuel_gauge_bg.png"
    );
    public static final ResourceLocation GAUGE_FILL = ResourceLocation.fromNamespaceAndPath(
            "jetlytra",
            "textures/hud/fuel_gauge_fill.png"
    );
    public static final int GAUGE_W = 32;
    public static final int GAUGE_H = 63;
    public static final int FILL_OFFSET_TOP = 5;
    public static final int FILL_OFFSET_BOTTOM = 5;

    private static final int PREVIEW_PERCENT = 75;

    public GaugeHudWidget(
            ModConfigSpec.DoubleValue cfgX,
            ModConfigSpec.DoubleValue cfgY,
            ModConfigSpec.DoubleValue cfgScale
    ) {
        super(cfgX, cfgY, cfgScale);
    }

    @Override
    public int[] box(int sw, int sh) {
        int gw = (int)(GAUGE_W * scale);
        int gh = (int)(GAUGE_H * scale);
        return new int[]{ cx(sw) - gw / 2, cy(sh) - gh / 2, gw, gh };
    }

    @Override
    public void renderContent(GuiGraphics g, int sw, int sh) {
        render(g, cx(sw), cy(sh), (float) scale, PREVIEW_PERCENT);
    }

    public static void render(GuiGraphics g, float cx, float cy, float scale, int percent) {
        // The active fill zone excludes the fixed top/bottom offset rows.
        // Scissor runs from the bottom of the active zone upward by the proportional fill amount.
        int scLeft    = (int)(cx - GAUGE_W / 2f * scale);
        int scRight   = (int)(cx + GAUGE_W / 2f * scale);
        int activeBot = (int)(cy + (GAUGE_H / 2f - FILL_OFFSET_BOTTOM) * scale);
        int activeTop = (int)(cy - (GAUGE_H / 2f - FILL_OFFSET_TOP)   * scale);
        int activeH   = activeBot - activeTop;
        int scBot     = activeBot;
        int scTop     = scBot - Math.max(0, (int)(activeH * percent / 100f));

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        g.pose().pushPose();
        g.pose().translate(cx, cy, 0);
        g.pose().scale(scale, scale, 1f);
        // Fill drawn first, bg overlaid on top (bg has transparency)
        g.blit(GAUGE_BG, -GAUGE_W / 2, -GAUGE_H / 2, 0, 0, GAUGE_W, GAUGE_H, GAUGE_W, GAUGE_H);
        if (scTop < scBot) {
            g.enableScissor(scLeft, scTop, scRight, scBot);
            g.blit(GAUGE_FILL, -GAUGE_W / 2, -GAUGE_H / 2, 0, 0, GAUGE_W, GAUGE_H, GAUGE_W, GAUGE_H);
            g.disableScissor();
        }

        g.pose().popPose();

        RenderSystem.disableBlend();
    }
}
