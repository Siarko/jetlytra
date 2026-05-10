package pl.siarko.jetlytra.client.hud;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class JetlytraConfigScreen extends Screen {

    private static final Component TITLE = Component.literal("Jetlytra Config");
    private static final int ROW_WIDTH = 300;
    private static final int BUTTON_W = 100;
    private static final int BUTTON_H = 20;

    private final Screen parent;

    public JetlytraConfigScreen(Screen parent) {
        super(TITLE);
        this.parent = parent;
    }

    @Override
    protected void init() {
        int rowLeft = width / 2 - ROW_WIDTH / 2;
        int rowY = height / 2 - BUTTON_H / 2;

        boolean inGame = minecraft.level != null;
        Button configureBtn = Button.builder(
                Component.literal("Configure"),
                btn -> minecraft.setScreen(new HudConfigScreen(this)))
                .pos(rowLeft + ROW_WIDTH - BUTTON_W, rowY)
                .size(BUTTON_W, BUTTON_H)
                .tooltip(inGame ? null : Tooltip.create(
                        Component.literal("A world must be loaded to configure HUD position")))
                .build();
        configureBtn.active = inGame;
        addRenderableWidget(configureBtn);

        addRenderableWidget(Button.builder(
                Component.literal("Done"),
                btn -> onClose())
                .pos(width / 2 - 50, rowY + 36)
                .size(100, BUTTON_H)
                .build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(font, TITLE, width / 2, 20, 0xFFFFFF);

        int rowLeft = width / 2 - ROW_WIDTH / 2;
        int rowY = height / 2 - font.lineHeight / 2;
        graphics.drawString(font, "HUD Position", rowLeft, rowY, 0xFFFFFF);
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }
}
