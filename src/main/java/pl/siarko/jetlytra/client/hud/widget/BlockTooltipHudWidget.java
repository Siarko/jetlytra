package pl.siarko.jetlytra.client.hud.widget;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

public class BlockTooltipHudWidget extends HudWidget {

    private static final List<Component> PREVIEW_LINES = List.of(
            Component.translatable("block.jetlytra.jetpack"),
            Component.literal("Blaze Rod").withStyle(ChatFormatting.GOLD),
            Component.literal("160 / 576").withStyle(ChatFormatting.GRAY)
    );

    private static final int PREVIEW_W = 65;
    private static final int PREVIEW_H = 40;

    public BlockTooltipHudWidget(ModConfigSpec.DoubleValue cfgX, ModConfigSpec.DoubleValue cfgY) {
        super(cfgX, cfgY, cfgX); // pass cfgX as dummy scale — overridden below, never saved
        this.scale = 1.0;
    }

    // Vanilla DefaultTooltipPositioner places the box at weird offset.
    private static final int TIP_OX = 9;
    private static final int TIP_OY = -15;

    @Override
    public int[] box(int sw, int sh) {
        return new int[]{cx(sw) + TIP_OX, cy(sh) + TIP_OY, PREVIEW_W, PREVIEW_H};
    }

    @Override
    public void renderContent(GuiGraphics g, int sw, int sh) {
        g.renderTooltip(Minecraft.getInstance().font, PREVIEW_LINES, java.util.Optional.empty(),
                cx(sw), cy(sh));
    }

    @Override
    public void render(GuiGraphics g, int sw, int sh) {
        int[] b = box(sw, sh);
        g.renderOutline(b[0] - BORDER, b[1] - BORDER, b[2] + BORDER * 2, b[3] + BORDER * 2, BORDER_COLOR);
        renderContent(g, sw, sh);
    }

    @Override
    public boolean isOverHandle(double mx, double my, int sw, int sh) {
        return false;
    }

    @Override
    public void save() {
        cfgX.set(normX);
        cfgY.set(normY);
    }

    @Override
    public void resetToDefault() {
        normX = cfgX.getDefault();
        normY = cfgY.getDefault();
        scale = 1.0;
    }
}
