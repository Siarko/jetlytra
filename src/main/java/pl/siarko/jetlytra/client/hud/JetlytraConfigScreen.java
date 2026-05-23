package pl.siarko.jetlytra.client.hud;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.network.PacketDistributor;
import pl.siarko.jetlytra.client.hud.config.ThrustAccelSlider;
import pl.siarko.jetlytra.config.BlockTooltipMode;
import pl.siarko.jetlytra.client.hud.config.WarningLevelSlider;
import pl.siarko.jetlytra.config.ServerPhysicsConfig;
import pl.siarko.jetlytra.config.JetlytraClientConfig;
import pl.siarko.jetlytra.network.C2SServerConfigChangePacket;

import java.util.function.DoubleConsumer;

public class JetlytraConfigScreen extends Screen {

    private static final Component TITLE = Component.translatable("screen.jetlytra.config");
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
        ConfigOptionsList list = addRenderableWidget(new ConfigOptionsList(
                minecraft, width, height - 68, 32
        ));

        addClientConfigWidgets(list);

        addServerConfigWidgets(list);

        addRenderableWidget(Button.builder(Component.literal("Done"), btn -> onClose())
                .bounds(width / 2 - 100, height - 27, 200, 20).build());
    }

    private void addClientConfigWidgets(ConfigOptionsList list) {
        list.addText(sectionHeader("screen.jetlytra.section.client_config"), true);
        list.addText(sectionLabel("screen.jetlytra.section.hud"), false);
        list.addWidget(label("config.jetlytra.show_fuel_percentage"),
                toggleButton(JetlytraClientConfig.SHOW_FUEL_PERCENTAGE, null));
        list.addWidget(label("config.jetlytra.show_fuel_gauge"),
                toggleButton(JetlytraClientConfig.SHOW_FUEL_GAUGE, null));
        list.addWidget(label("config.jetlytra.show_fuel_warning"),
                toggleButton(JetlytraClientConfig.SHOW_FUEL_WARNING,
                        () -> warnSlider.active = JetlytraClientConfig.SHOW_FUEL_WARNING.get()));
        list.addWidget(label("config.jetlytra.show_block_tooltip"),
                cycleBlockTooltipButton());

        warnSlider = new WarningLevelSlider(0, 0, SLIDER_W, ROW_H, JetlytraClientConfig.FUEL_WARNING_LEVEL.get());
        warnSlider.active = JetlytraClientConfig.SHOW_FUEL_WARNING.get();
        list.addWidget(label("config.jetlytra.fuel_warning_label"), warnSlider);

        boolean inGame = minecraft.level != null;
        Button configBtn = Button.builder(
                        Component.literal("Configure HUD"),
                        btn -> minecraft.setScreen(new HudConfigScreen(this)))
                .size(TOGGLE_W, ROW_H)
                .tooltip(inGame ? null : Tooltip.create(
                        Component.literal("A world must be loaded to configure HUD position")))
                .build();
        configBtn.active = inGame;
        list.addWidget(label("screen.jetlytra.hud_config"), configBtn);

        list.addText(sectionLabel("screen.jetlytra.section.movement"), false);
        list.addWidget(label("config.jetlytra.jump_before_thrust"),
                toggleButton(JetlytraClientConfig.JUMP_BEFORE_THRUST, null,
                        "config.jetlytra.jump_before_thrust.tooltip"));
    }

    private void addServerConfigWidgets(ConfigOptionsList list) {
        list.addText(sectionHeader("screen.jetlytra.section.server_config"), true);
        Component serverTarget = minecraft.getCurrentServer() != null
                ? Component.literal(minecraft.getCurrentServer().name)
                : Component.translatable("screen.jetlytra.server_config.local");
        list.addText(Component.translatable("screen.jetlytra.server_config.editing", serverTarget)
                .withStyle(ChatFormatting.YELLOW), false);

        boolean isOp = minecraft.player == null || minecraft.player.hasPermissions(2);

        list.addText(sectionLabel("screen.jetlytra.section.jetpack_mode"), false);
        list.addWidget(
                label("config.jetlytra.thrust_accel"),
                serverSlider(0.01, 1.0, ServerPhysicsConfig.physics.thrustAccel(), ServerPhysicsConfig.physics::setThrustAccel, isOp, null)
        );
        list.addWidget(
                label("config.jetlytra.max_thrust_vel"),
                serverSlider(0.1, 5.0, ServerPhysicsConfig.physics.maxThrustVel(), ServerPhysicsConfig.physics::setMaxThrustVel, isOp, null)
        );
        list.addWidget(
                label("config.jetlytra.thrust_accel_down"),
                serverSlider(
                        0.01,
                        1.0,
                        ServerPhysicsConfig.physics.thrustAccelDown(),
                        ServerPhysicsConfig.physics::setThrustAccelDown,
                        isOp,
                        "config.jetlytra.thrust_accel_down.tooltip"
                )
        );

        list.addText(sectionLabel("screen.jetlytra.section.hover_mode"), false);
        list.addWidget(
                label("config.jetlytra.hover_thrust_accel"),
                serverSlider(
                        0.01,
                        0.5,
                        ServerPhysicsConfig.physics.hoverThrustAccel(),
                        ServerPhysicsConfig.physics::setHoverThrustAccel,
                        isOp,
                        "config.jetlytra.hover_thrust_accel.tooltip"
                )
        );
        list.addWidget(
                label("config.jetlytra.hover_thrust_max"),
                serverSlider(
                        0.01,
                        2.0,
                        ServerPhysicsConfig.physics.hoverThrustMax(),
                        ServerPhysicsConfig.physics::setHoverThrustMax,
                        isOp,
                        "config.jetlytra.hover_thrust_max.tooltip"
                )
        );

        list.addText(sectionLabel("screen.jetlytra.section.elytra_mode"), false);
        list.addWidget(
                label("config.jetlytra.elytra_boost_accel"),
                serverSlider(
                        0.01,
                        0.5,
                        ServerPhysicsConfig.physics.elytraBoostAccel(),
                        ServerPhysicsConfig.physics::setElytraBoostAccel,
                        isOp,
                        "config.jetlytra.elytra_boost_accel.tooltip"
                )
        );
        list.addWidget(
                label("config.jetlytra.elytra_boost_max"),
                serverSlider(
                        0.1,
                        5.0,
                        ServerPhysicsConfig.physics.elytraBoostMax(),
                        ServerPhysicsConfig.physics::setElytraBoostMax,
                        isOp,
                        "config.jetlytra.elytra_boost_max.tooltip"
                )
        );

        list.addText(sectionLabel("screen.jetlytra.section.other"), false);
        list.addWidget(
                label("config.jetlytra.swim_boost_max"),
                serverSlider(
                        0.1,
                        5.0,
                        ServerPhysicsConfig.physics.swimBoostMax(),
                        ServerPhysicsConfig.physics::setSwimBoostMax,
                        isOp,
                        "config.jetlytra.swim_boost_max.tooltip"
                )
        );
        list.addWidget(
                label("config.jetlytra.sprint_boost_accel"),
                serverSlider(
                        0.01,
                        0.5,
                        ServerPhysicsConfig.physics.sprintBoostAccel(),
                        ServerPhysicsConfig.physics::setSprintBoostAccel,
                        isOp,
                        "config.jetlytra.sprint_boost_accel.tooltip"
                )
        );
        list.addWidget(
                label("config.jetlytra.sprint_boost_max"),
                serverSlider(
                        0.1,
                        5.0,
                        ServerPhysicsConfig.physics.sprintBoostMax(),
                        ServerPhysicsConfig.physics::setSprintBoostMax,
                        isOp,
                        "config.jetlytra.sprint_boost_max.tooltip"
                )
        );
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        g.drawCenteredString(font, TITLE, width / 2, 15, 0xFFFFFF);
    }

    private static Component sectionHeader(String key) {
        return Component.translatable(key).withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD);
    }

    private static Component sectionLabel(String key) {
        return Component.translatable(key).withStyle(ChatFormatting.GRAY);
    }

    private static Component label(String key) {
        return Component.translatable(key);
    }

    private ThrustAccelSlider serverSlider(
            double min,
            double max,
            double initial,
            DoubleConsumer onSet,
            boolean active,
            String tooltipKey
    ) {
        ThrustAccelSlider slider = new ThrustAccelSlider(0, 0, SLIDER_W, ROW_H, min, max, initial, val -> {
            onSet.accept(val);
            sendServerConfig();
        });
        slider.active = active;
        if (!active) {
            slider.setTooltip(Tooltip.create(Component.translatable("screen.jetlytra.server_config.no_op")));
        } else if (tooltipKey != null) {
            slider.setTooltip(Tooltip.create(Component.translatable(tooltipKey)));
        }
        return slider;
    }

    private void sendServerConfig() {
        PacketDistributor.sendToServer(new C2SServerConfigChangePacket(
                ServerPhysicsConfig.physics.thrustAccel(),
                ServerPhysicsConfig.physics.maxThrustVel(),
                ServerPhysicsConfig.physics.thrustAccelDown(),
                ServerPhysicsConfig.physics.hoverThrustAccel(),
                ServerPhysicsConfig.physics.hoverThrustMax(),
                ServerPhysicsConfig.physics.elytraBoostAccel(),
                ServerPhysicsConfig.physics.elytraBoostMax(),
                ServerPhysicsConfig.physics.swimBoostMax(),
                ServerPhysicsConfig.physics.sprintBoostAccel(),
                ServerPhysicsConfig.physics.sprintBoostMax()
        ));
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

    private Button cycleBlockTooltipButton() {
        BlockTooltipMode[] modes = BlockTooltipMode.values();
        BlockTooltipMode initial = JetlytraClientConfig.BLOCK_TOOLTIP_MODE.get();
        Button btn = Button.builder(blockTooltipModeLabel(initial), b -> {
            BlockTooltipMode current = JetlytraClientConfig.BLOCK_TOOLTIP_MODE.get();
            BlockTooltipMode next = modes[(current.ordinal() + 1) % modes.length];
            JetlytraClientConfig.BLOCK_TOOLTIP_MODE.set(next);
            JetlytraClientConfig.SPEC.save();
            b.setMessage(blockTooltipModeLabel(next));
            b.setTooltip(blockTooltipModeTooltip(next));
        }).size(TOGGLE_W, ROW_H).build();
        btn.setTooltip(blockTooltipModeTooltip(initial));
        return btn;
    }

    private static Tooltip blockTooltipModeTooltip(BlockTooltipMode mode) {
        return switch (mode) {
            case OFF -> null;
            case DYNAMIC -> Tooltip.create(Component.translatable("config.jetlytra.show_block_tooltip.dynamic.tooltip"));
            case STATIC -> Tooltip.create(Component.translatable("config.jetlytra.show_block_tooltip.static.tooltip"));
        };
    }

    private static Component blockTooltipModeLabel(BlockTooltipMode mode) {
        return switch (mode) {
            case OFF -> Component.literal("Off").withColor(0xFF5555);
            case DYNAMIC -> Component.literal("Dynamic").withColor(0x55FF55);
            case STATIC -> Component.literal("Static").withColor(0xFFAA00);
        };
    }

    private static Component toggleLabel(boolean on) {
        return on
                ? Component.literal("ON").withColor(0x55FF55)
                : Component.literal("OFF").withColor(0xFF5555);
    }

    @Override
    public void onClose() {
        if(minecraft == null) return;
        minecraft.setScreen(parent);
    }
}
