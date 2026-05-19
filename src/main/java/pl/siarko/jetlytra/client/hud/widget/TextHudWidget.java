package pl.siarko.jetlytra.client.hud.widget;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.neoforged.neoforge.common.ModConfigSpec;

public class TextHudWidget extends HudWidget {

    private final Font font;
    private final String text;
    private final int color;

    public TextHudWidget(Font font, String text, int color,
                         ModConfigSpec.DoubleValue cfgX, ModConfigSpec.DoubleValue cfgY,
                         ModConfigSpec.DoubleValue cfgScale) {
        super(cfgX, cfgY, cfgScale);
        this.font = font;
        this.text = text;
        this.color = color;
    }

    @Override
    public int[] box(int sw, int sh) {
        int w = (int)(font.width(text) * scale);
        int h = (int)(font.lineHeight * scale);
        int clampedCx = (int) clampCenter(cx(sw), w / 2f, sw);
        int clampedCy = (int) clampCenter(cy(sh), h / 2f, sh);
        return new int[]{ clampedCx - w / 2, clampedCy - h / 2, w, h };
    }

    @Override
    public void renderContent(GuiGraphics g, int sw, int sh) {
        render(g, font, text, cx(sw), cy(sh), (float) scale, color, sw, sh);
    }

    public static void render(GuiGraphics g, Font font, String text,
                              float cx, float cy, float scale, int color, int sw, int sh) {
        cx = clampCenter(cx, font.width(text) * scale / 2f, sw);
        cy = clampCenter(cy, font.lineHeight * scale / 2f, sh);
        g.pose().pushPose();
        g.pose().translate(cx, cy, 0);
        g.pose().scale(scale, scale, 1f);
        g.drawString(font, text, -font.width(text) / 2, -font.lineHeight / 2, color, true);
        g.pose().popPose();
    }
}
