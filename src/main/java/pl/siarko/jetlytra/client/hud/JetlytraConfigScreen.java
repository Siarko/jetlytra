package pl.siarko.jetlytra.client.hud;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.layouts.SpacerElement;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.common.ModConfigSpec;
import pl.siarko.jetlytra.client.hud.config.WarningLevelSlider;
import pl.siarko.jetlytra.config.JetlytraClientConfig;

public class JetlytraConfigScreen extends Screen {

    private static final Component TITLE = Component.translatable("screen.jetlytra.config");
    private static final int LABEL_W = 150;
    private static final int TOGGLE_W = 100;
    private static final int SLIDER_W = 100;
    private static final int ROW_H = 20;

    private final Screen parent;
    private WarningLevelSlider warnSlider;

    public JetlytraConfigScreen(Screen parent) {
        super(TITLE);
        this.parent = parent;
    }

    @Override
    protected void init() {
        HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);
        layout.addTitleHeader(TITLE, font);

        LinearLayout content = layout.addToContents(LinearLayout.vertical().spacing(6));

        content.addChild(sectionHeader("screen.jetlytra.section.client_config"));
        content.addChild(sectionLabel("screen.jetlytra.section.hud"));
        content.addChild(labeledRow("config.jetlytra.show_fuel_percentage",
                toggleButton(JetlytraClientConfig.SHOW_FUEL_PERCENTAGE, null)));

        content.addChild(labeledRow("config.jetlytra.show_fuel_gauge",
                toggleButton(JetlytraClientConfig.SHOW_FUEL_GAUGE, null)));

        content.addChild(labeledRow("config.jetlytra.show_fuel_warning",
                toggleButton(JetlytraClientConfig.SHOW_FUEL_WARNING,
                        () -> warnSlider.active = JetlytraClientConfig.SHOW_FUEL_WARNING.get())));

        warnSlider = new WarningLevelSlider(0, 0, SLIDER_W, ROW_H, JetlytraClientConfig.FUEL_WARNING_LEVEL.get());
        warnSlider.active = JetlytraClientConfig.SHOW_FUEL_WARNING.get();
        content.addChild(labeledRow("config.jetlytra.fuel_warning_label", warnSlider));

        boolean inGame = minecraft.level != null;
        Button configBtn = Button.builder(
                        Component.literal("Configure HUD"),
                        btn -> minecraft.setScreen(new HudConfigScreen(this)))
                .size(TOGGLE_W, ROW_H)
                .tooltip(inGame ? null : Tooltip.create(
                        Component.literal("A world must be loaded to configure HUD position")))
                .build();
        configBtn.active = inGame;
        content.addChild(labeledRow("screen.jetlytra.hud_config", configBtn));

        content.addChild(SpacerElement.height(4));
        content.addChild(sectionLabel("screen.jetlytra.section.movement"));
        content.addChild(labeledRow("config.jetlytra.jump_before_thrust",
                toggleButton(JetlytraClientConfig.JUMP_BEFORE_THRUST, null, "config.jetlytra.jump_before_thrust.tooltip")));


        layout.addToFooter(Button.builder(Component.literal("Done"), btn -> onClose())
                .size(100, ROW_H).build());

        layout.arrangeElements();
        layout.visitWidgets(this::addRenderableWidget);
    }

    private StringWidget sectionHeader(String key) {
        return new StringWidget(LABEL_W + TOGGLE_W + 8, ROW_H + 4,
                Component.translatable(key).withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD), font)
                .alignCenter();
    }

    private StringWidget sectionLabel(String key) {
        return new StringWidget(LABEL_W + TOGGLE_W + 8, ROW_H,
                Component.translatable(key).withStyle(ChatFormatting.GRAY), font)
                .alignLeft();
    }

    private LinearLayout labeledRow(String labelKey, LayoutElement control) {
        LinearLayout row = LinearLayout.horizontal().spacing(8);
        row.defaultCellSetting().alignVerticallyMiddle();
        row.addChild(new StringWidget(LABEL_W, ROW_H, Component.translatable(labelKey), font).alignLeft());
        row.addChild(control);
        return row;
    }

    private Button toggleButton(ModConfigSpec.BooleanValue config, Runnable onToggle) {
        return toggleButton(config, onToggle, null);
    }

    private Button toggleButton(ModConfigSpec.BooleanValue config, Runnable onToggle, String tooltipKey) {
        var builder = Button.builder(toggleLabel(config.get()), b -> {
            boolean next = !config.get();
            config.set(next);
            JetlytraClientConfig.SPEC.save();
            b.setMessage(toggleLabel(next));
            if (onToggle != null) onToggle.run();
        }).size(TOGGLE_W, ROW_H);
        if (tooltipKey != null) builder.tooltip(Tooltip.create(Component.translatable(tooltipKey)));
        return builder.build();
    }

    private static Component toggleLabel(boolean on) {
        return on ? Component.literal("ON").withColor(0x55FF55)
                  : Component.literal("OFF").withColor(0xFF5555);
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }
}
