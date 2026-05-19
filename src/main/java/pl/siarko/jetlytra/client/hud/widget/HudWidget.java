package pl.siarko.jetlytra.client.hud.widget;

import net.minecraft.client.gui.GuiGraphics;
import net.neoforged.neoforge.common.ModConfigSpec;

public abstract class HudWidget {

    public static final int BORDER = 2;
    public static final int BORDER_COLOR = 0xFFFFAA00;
    public static final int HANDLE_SIZE = 4;

    public final ModConfigSpec.DoubleValue cfgX, cfgY, cfgScale;
    public double normX, normY, scale;

    protected HudWidget(ModConfigSpec.DoubleValue cfgX, ModConfigSpec.DoubleValue cfgY,
                        ModConfigSpec.DoubleValue cfgScale) {
        this.cfgX = cfgX; this.cfgY = cfgY; this.cfgScale = cfgScale;
        this.normX = cfgX.get(); this.normY = cfgY.get(); this.scale = cfgScale.get();
    }

    public int cx(int sw) { return (int)(normX * sw); }
    public int cy(int sh) { return (int)(normY * sh); }

    /** Clamps a centered widget so its edges don't exceed the screen boundary. */
    protected static float clampCenter(float pos, float halfSize, float limit) {
        return Math.max(halfSize, Math.min(limit - halfSize, pos));
    }

    /** Content bounds in screen space: {x, y, w, h} */
    public abstract int[] box(int sw, int sh);
    public abstract void renderContent(GuiGraphics g, int sw, int sh);

    public void render(GuiGraphics g, int sw, int sh) {
        renderContent(g, sw, sh);
        int[] b = box(sw, sh);
        g.renderOutline(b[0] - BORDER, b[1] - BORDER, b[2] + BORDER * 2, b[3] + BORDER * 2, BORDER_COLOR);
        int hx = b[0] + b[2] + BORDER - HANDLE_SIZE;
        int hy = b[1] + b[3] + BORDER - HANDLE_SIZE;
        g.fill(hx, hy, hx + HANDLE_SIZE, hy + HANDLE_SIZE, 0xFFFFFFFF);
    }

    public boolean isOver(double mx, double my, int sw, int sh) {
        int[] b = box(sw, sh);
        return mx >= b[0] - BORDER && mx <= b[0] + b[2] + BORDER
                && my >= b[1] - BORDER && my <= b[1] + b[3] + BORDER;
    }

    public boolean isOverHandle(double mx, double my, int sw, int sh) {
        int[] b = box(sw, sh);
        int hx = b[0] + b[2] + BORDER - HANDLE_SIZE;
        int hy = b[1] + b[3] + BORDER - HANDLE_SIZE;
        return mx >= hx && mx <= hx + HANDLE_SIZE && my >= hy && my <= hy + HANDLE_SIZE;
    }

    public void save() { cfgX.set(normX); cfgY.set(normY); cfgScale.set(scale); }

    public void resetToDefault() {
        normX = cfgX.getDefault(); normY = cfgY.getDefault(); scale = cfgScale.getDefault();
    }
}
