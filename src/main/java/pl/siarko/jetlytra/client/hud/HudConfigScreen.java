package pl.siarko.jetlytra.client.hud;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import pl.siarko.jetlytra.config.JetlytraClientConfig;

public class HudConfigScreen extends Screen {

    private static final Component TITLE = Component.translatable("screen.jetlytra.hud_config");
    private static final Component INSTRUCTIONS = Component.literal("Drag to reposition — ESC to save and close");
    private static final String PREVIEW_TEXT = "100%";

    private final Screen parent;
    private double hudNormX;
    private double hudNormY;
    private boolean dragging = false;

    public HudConfigScreen(Screen parent) {
        super(TITLE);
        this.parent = parent;
        hudNormX = JetlytraClientConfig.HUD_X.get();
        hudNormY = JetlytraClientConfig.HUD_Y.get();
    }

    @Override
    protected void init() {
        addRenderableWidget(Button.builder(
                Component.literal("Reset to Default"),
                btn -> {
                    hudNormX = JetlytraClientConfig.HUD_X.getDefault();
                    hudNormY = JetlytraClientConfig.HUD_Y.getDefault();
                })
                .pos(width / 2 - 60, height - 30)
                .size(120, 20)
                .build());
    }

    private int boxWidth() { return font.width(PREVIEW_TEXT) + 8; }
    private int boxHeight() { return font.lineHeight + 6; }
    private int boxLeft() { return (int)(hudNormX * width) - boxWidth() / 2; }
    private int boxTop() { return (int)(hudNormY * height) - boxHeight() / 2; }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // No background — world renders behind this screen
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int bx = boxLeft();
        int by = boxTop();
        int bw = boxWidth();
        int bh = boxHeight();

        graphics.fill(bx, by, bx + bw, by + bh, 0xFF1a1a1a);
        graphics.renderOutline(bx, by, bw, bh, 0xFFFFAA00);
        graphics.drawCenteredString(font, PREVIEW_TEXT, bx + bw / 2, by + (bh - font.lineHeight) / 2, 0xFFFFAA00);

        // Instruction bar at top
        graphics.fill(0, 0, width, 14, 0xCC000000);
        graphics.drawCenteredString(font, INSTRUCTIONS, width / 2, 3, 0xFFFFFF);

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && isOverBox(mouseX, mouseY)) {
            dragging = true;
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (dragging) {
            hudNormX = Math.clamp(mouseX / width, 0.0, 1.0);
            hudNormY = Math.clamp(mouseY / height, 0.0, 1.0);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        dragging = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void onClose() {
        JetlytraClientConfig.HUD_X.set(hudNormX);
        JetlytraClientConfig.HUD_Y.set(hudNormY);
        JetlytraClientConfig.SPEC.save();
        minecraft.setScreen(parent);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private boolean isOverBox(double mouseX, double mouseY) {
        int bx = boxLeft();
        int by = boxTop();
        return mouseX >= bx && mouseX <= bx + boxWidth() && mouseY >= by && mouseY <= by + boxHeight();
    }
}
