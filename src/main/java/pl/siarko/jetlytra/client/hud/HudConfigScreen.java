package pl.siarko.jetlytra.client.hud;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import pl.siarko.jetlytra.client.hud.widget.BlockTooltipHudWidget;
import pl.siarko.jetlytra.client.hud.widget.GaugeHudWidget;
import pl.siarko.jetlytra.client.hud.widget.HudWidget;
import pl.siarko.jetlytra.client.hud.widget.TextHudWidget;
import pl.siarko.jetlytra.config.BlockTooltipMode;
import pl.siarko.jetlytra.config.JetlytraClientConfig;

import java.util.ArrayList;
import java.util.List;

public class HudConfigScreen extends Screen {

    private static final Component TITLE = Component.translatable("screen.jetlytra.hud_config");
    private static final Component INSTRUCTIONS = Component.literal(
            "Drag widget to move — drag corner to resize — ESC to save"
    );

    private final Screen parent;
    private final List<HudWidget> widgets = new ArrayList<>();
    private HudWidget dragging = null;
    private HudWidget scaleDragging = null;

    public HudConfigScreen(Screen parent) {
        super(TITLE);
        this.parent = parent;
    }

    @Override
    protected void init() {
        widgets.clear();

        if (JetlytraClientConfig.SHOW_FUEL_PERCENTAGE.get())
            widgets.add(new TextHudWidget(font, "100%", 0xFFFFFFFF,
                    JetlytraClientConfig.FUEL_PERCENT_X, JetlytraClientConfig.FUEL_PERCENT_Y,
                    JetlytraClientConfig.FUEL_PERCENT_SCALE));

        if (JetlytraClientConfig.SHOW_FUEL_GAUGE.get())
            widgets.add(new GaugeHudWidget(
                    JetlytraClientConfig.FUEL_GAUGE_X, JetlytraClientConfig.FUEL_GAUGE_Y,
                    JetlytraClientConfig.FUEL_GAUGE_SCALE));

        if (JetlytraClientConfig.SHOW_FUEL_WARNING.get())
            widgets.add(new TextHudWidget(font, "Low Fuel!", 0xFFFF4444,
                    JetlytraClientConfig.FUEL_WARNING_X, JetlytraClientConfig.FUEL_WARNING_Y,
                    JetlytraClientConfig.FUEL_WARNING_SCALE));

        if (JetlytraClientConfig.BLOCK_TOOLTIP_MODE.get() == BlockTooltipMode.STATIC)
            widgets.add(
                    new BlockTooltipHudWidget(
                            JetlytraClientConfig.BLOCK_TOOLTIP_X,
                            JetlytraClientConfig.BLOCK_TOOLTIP_Y
                    )
            );

        addRenderableWidget(Button.builder(
                Component.literal("Reset to Default"),
                btn -> widgets.forEach(HudWidget::resetToDefault))
                .pos(width / 2 - 60, height - 20)
                .size(120, 20)
                .build());
    }

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        // world renders behind
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        for (HudWidget w : widgets) w.render(g, width, height);
        g.fill(0, 0, width, 14, 0xCC000000);
        g.drawCenteredString(font, INSTRUCTIONS, width / 2, 3, 0xFFFFFFFF);
        super.render(g, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            for (HudWidget w : widgets) {
                if (w.isOverHandle(mouseX, mouseY, width, height)) {
                    scaleDragging = w;
                    return true;
                }
            }
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
        if (scaleDragging != null) {
            scaleDragging.scale = Math.clamp(scaleDragging.scale + (dragX + dragY) * 0.02, 0.25, 4.0);
            return true;
        }
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
        scaleDragging = null;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void onClose() {
        widgets.forEach(HudWidget::save);
        JetlytraClientConfig.SPEC.save();
        minecraft.setScreen(parent);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
