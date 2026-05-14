package pl.siarko.jetlytra.client.hud;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.common.ModConfigSpec;
import pl.siarko.jetlytra.config.JetlytraClientConfig;

import java.util.ArrayList;
import java.util.List;

public class HudConfigScreen extends Screen {

    private static final Component TITLE = Component.translatable("screen.jetlytra.hud_config");
    private static final Component INSTRUCTIONS = Component.literal("Drag to reposition — ESC to save and close");

    private final Screen parent;
    private final List<HudWidget> widgets = new ArrayList<>();
    private HudWidget dragging = null;

    public HudConfigScreen(Screen parent) {
        super(TITLE);
        this.parent = parent;
    }

    @Override
    protected void init() {
        widgets.clear();

        if (JetlytraClientConfig.SHOW_FUEL_PERCENTAGE.get()) {
            widgets.add(new HudWidget("100%", 8,
                    JetlytraClientConfig.FUEL_PERCENT_X,
                    JetlytraClientConfig.FUEL_PERCENT_Y));
        }
        if (JetlytraClientConfig.SHOW_FUEL_GAUGE.get()) {
            widgets.add(new HudWidget("[ Gauge ]", 12,
                    JetlytraClientConfig.FUEL_GAUGE_X,
                    JetlytraClientConfig.FUEL_GAUGE_Y));
        }
        if (JetlytraClientConfig.SHOW_FUEL_WARNING.get()) {
            widgets.add(new HudWidget("Low Fuel!", 8,
                    JetlytraClientConfig.FUEL_WARNING_X,
                    JetlytraClientConfig.FUEL_WARNING_Y));
        }

        addRenderableWidget(Button.builder(
                Component.literal("Reset to Default"),
                btn -> {
                    for (HudWidget w : widgets) w.resetToDefault();
                })
                .pos(width / 2 - 60, height - 30)
                .size(120, 20)
                .build());
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // No background — world renders behind this screen
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        for (HudWidget w : widgets) {
            w.render(graphics, font, width, height);
        }

        graphics.fill(0, 0, width, 14, 0xCC000000);
        graphics.drawCenteredString(font, INSTRUCTIONS, width / 2, 3, 0xFFFFFF);

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            for (HudWidget w : widgets) {
                if (w.isOver(mouseX, mouseY, width, height)) {
                    dragging = w;
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (dragging != null) {
            dragging.normX = Math.clamp(mouseX / width, 0.0, 1.0);
            dragging.normY = Math.clamp(mouseY / height, 0.0, 1.0);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        dragging = null;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void onClose() {
        for (HudWidget w : widgets) w.save();
        JetlytraClientConfig.SPEC.save();
        minecraft.setScreen(parent);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private class HudWidget {
        final String label;
        final int padding;
        final ModConfigSpec.DoubleValue cfgX;
        final ModConfigSpec.DoubleValue cfgY;
        double normX;
        double normY;

        HudWidget(String label, int padding,
                  ModConfigSpec.DoubleValue cfgX, ModConfigSpec.DoubleValue cfgY) {
            this.label = label;
            this.padding = padding;
            this.cfgX = cfgX;
            this.cfgY = cfgY;
            this.normX = cfgX.get();
            this.normY = cfgY.get();
        }

        int boxW(net.minecraft.client.gui.Font font) { return font.width(label) + padding; }
        int boxH(net.minecraft.client.gui.Font font) { return font.lineHeight + 6; }
        int boxLeft(net.minecraft.client.gui.Font font, int screenW) { return (int)(normX * screenW) - boxW(font) / 2; }
        int boxTop(net.minecraft.client.gui.Font font, int screenH) { return (int)(normY * screenH) - boxH(font) / 2; }

        void render(GuiGraphics g, net.minecraft.client.gui.Font f, int sw, int sh) {
            int bx = boxLeft(f, sw);
            int by = boxTop(f, sh);
            int bw = boxW(f);
            int bh = boxH(f);
            g.fill(bx, by, bx + bw, by + bh, 0xFF1a1a1a);
            g.renderOutline(bx, by, bw, bh, 0xFFFFAA00);
            g.drawCenteredString(f, label, bx + bw / 2, by + (bh - f.lineHeight) / 2, 0xFFFFAA00);
        }

        boolean isOver(double mx, double my, int sw, int sh) {
            int bx = boxLeft(font, sw);
            int by = boxTop(font, sh);
            return mx >= bx && mx <= bx + boxW(font) && my >= by && my <= by + boxH(font);
        }

        void save() {
            cfgX.set(normX);
            cfgY.set(normY);
        }

        void resetToDefault() {
            normX = cfgX.getDefault();
            normY = cfgY.getDefault();
        }
    }
}
