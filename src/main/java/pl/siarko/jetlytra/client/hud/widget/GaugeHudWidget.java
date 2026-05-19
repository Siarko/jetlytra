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
        int clampedCx = (int) clampCenter(cx(sw), gw / 2f, sw);
        int clampedCy = (int) clampCenter(cy(sh), gh / 2f, sh);
        return new int[]{ clampedCx - gw / 2, clampedCy - gh / 2, gw, gh };
    }

    @Override
    public void renderContent(GuiGraphics g, int sw, int sh) {
        render(g, cx(sw), cy(sh), (float) scale, PREVIEW_PERCENT, sw, sh);
    }

    public static void render(GuiGraphics g, float cx, float cy, float scale, int percent, int sw, int sh) {
        cx = clampCenter(cx, GAUGE_W * scale / 2f, sw);
        cy = clampCenter(cy, GAUGE_H * scale / 2f, sh);

        int xpos = -GAUGE_W / 2;
        int ypos = -GAUGE_H / 2;

        // Active fill zone excludes the fixed top/bottom decorative rows.
        int gaugeHeight = GAUGE_H - FILL_OFFSET_TOP - FILL_OFFSET_BOTTOM;
        int fillHeight = (int)(gaugeHeight * percent / 100f);

        // Scissor clips from the bottom of the active zone upward by the fill amount.
        // Coordinates must be in GUI screen-space (not local PoseStack space), so apply
        // the same translate+scale manually before passing to enableScissor. :/
        float localFillBottom = ypos + GAUGE_H - FILL_OFFSET_BOTTOM;
        float localFillTop = localFillBottom - fillHeight;
        int screenLeft   = (int)(cx + xpos              * scale);
        int screenRight  = (int)(cx + (xpos + GAUGE_W)  * scale);
        int screenTop    = (int)(cy + localFillTop       * scale);
        int screenBottom = (int)(cy + localFillBottom    * scale);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        g.pose().pushPose();
        g.pose().translate(cx, cy, 0);
        g.pose().scale(scale, scale, 1f);

        g.blit(GAUGE_BG, xpos, ypos, 0, 0, GAUGE_W, GAUGE_H, GAUGE_W, GAUGE_H);
        g.enableScissor(screenLeft, screenTop, screenRight, screenBottom);
        g.blit(GAUGE_FILL, xpos, ypos, 0, 0, GAUGE_W, GAUGE_H, GAUGE_W, GAUGE_H);
        g.disableScissor();

        g.pose().popPose();
        RenderSystem.disableBlend();
    }
}
