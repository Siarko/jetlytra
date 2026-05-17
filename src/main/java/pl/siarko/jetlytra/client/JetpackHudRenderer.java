package pl.siarko.jetlytra.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import pl.siarko.jetlytra.client.hud.widget.GaugeHudWidget;
import pl.siarko.jetlytra.client.hud.widget.TextHudWidget;
import pl.siarko.jetlytra.config.JetlytraClientConfig;
import pl.siarko.jetlytra.flight.FuelData;
import pl.siarko.jetlytra.item.JetlytraItemBase;
import pl.siarko.jetlytra.item.JetlytraItems;

public class JetpackHudRenderer {

    public static void onRenderHud(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;

        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        if (!(chest.getItem() instanceof JetlytraItemBase)) return;

        FuelData fuel = chest.get(JetlytraItems.FUEL_DATA);
        if (fuel == null) return;

        int percent = Math.round(fuel.count() * 100f / FuelData.MAX_COUNT);
        boolean thrusting = Boolean.TRUE.equals(chest.get(JetlytraItems.THRUST_ACTIVE_COMPONENT));

        GuiGraphics g = event.getGuiGraphics();
        int sw = g.guiWidth();
        int sh = g.guiHeight();

        if (JetlytraClientConfig.SHOW_FUEL_PERCENTAGE.get()) {
            TextHudWidget.render(g, mc.font, percent + "%",
                    (float)(JetlytraClientConfig.FUEL_PERCENT_X.get() * sw),
                    (float)(JetlytraClientConfig.FUEL_PERCENT_Y.get() * sh),
                    JetlytraClientConfig.FUEL_PERCENT_SCALE.get().floatValue(),
                    0xFFFFFF);
        }

        if (JetlytraClientConfig.SHOW_FUEL_GAUGE.get()) {
            GaugeHudWidget.render(g,
                    (float)(JetlytraClientConfig.FUEL_GAUGE_X.get() * sw),
                    (float)(JetlytraClientConfig.FUEL_GAUGE_Y.get() * sh),
                    JetlytraClientConfig.FUEL_GAUGE_SCALE.get().floatValue(),
                    percent);
        }

        if (
                JetlytraClientConfig.SHOW_FUEL_WARNING.get()
                && percent <= JetlytraClientConfig.FUEL_WARNING_LEVEL.get()
                && (System.currentTimeMillis() / 500) % 2 == 0
                && thrusting
        ) {
            TextHudWidget.render(g, mc.font, "Low Fuel!",
                    (float)(JetlytraClientConfig.FUEL_WARNING_X.get() * sw),
                    (float)(JetlytraClientConfig.FUEL_WARNING_Y.get() * sh),
                    JetlytraClientConfig.FUEL_WARNING_SCALE.get().floatValue(),
                    0xFF4444);
        }
    }
}
