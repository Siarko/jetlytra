package pl.siarko.jetlytra.client.hud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.entity.player.Player;
import pl.siarko.jetlytra.JetlytraSlotHelper;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import pl.siarko.jetlytra.client.hud.widget.GaugeHudWidget;
import pl.siarko.jetlytra.client.hud.widget.TextHudWidget;
import pl.siarko.jetlytra.config.JetlytraClientConfig;
import pl.siarko.jetlytra.flight.FuelData;
import pl.siarko.jetlytra.item.JetlytraItemBase;
import pl.siarko.jetlytra.item.JetlytraItems;

public class JetpackHudRenderer {

    private static final String LABEL_LOW_FUEL = "hud.jetlytra.low_fuel";

    public static void onRenderHud(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;

        ItemStack chest = JetlytraSlotHelper.getWornJetlytra(player);
        if (!(chest.getItem() instanceof JetlytraItemBase)) return;

        FuelData fuel = chest.get(JetlytraItems.FUEL_DATA);
        if (fuel == null) return;

        int percent = Math.round(fuel.count() * 100f / FuelData.MAX_COUNT);

        GuiGraphics g = event.getGuiGraphics();
        int sw = g.guiWidth();
        int sh = g.guiHeight();
        
        if (JetlytraClientConfig.SHOW_FUEL_PERCENTAGE.get()) {
            TextHudWidget.render(
                    g, mc.font, percent + "%",
                    (float)(JetlytraClientConfig.FUEL_PERCENT_X.get() * sw),
                    (float)(JetlytraClientConfig.FUEL_PERCENT_Y.get() * sh),
                    JetlytraClientConfig.FUEL_PERCENT_SCALE.get().floatValue(),
                    0xFFFFFF, sw, sh
            );
        }

        if (JetlytraClientConfig.SHOW_FUEL_GAUGE.get()) {
            GaugeHudWidget.render(
                    g,
                    (float)(JetlytraClientConfig.FUEL_GAUGE_X.get() * sw),
                    (float)(JetlytraClientConfig.FUEL_GAUGE_Y.get() * sh),
                    JetlytraClientConfig.FUEL_GAUGE_SCALE.get().floatValue(),
                    percent, sw, sh
            );
        }

        if (
                JetlytraClientConfig.SHOW_FUEL_WARNING.get()
                && percent <= JetlytraClientConfig.FUEL_WARNING_LEVEL.get()
                && (System.currentTimeMillis() / 1000) % 2 == 0
                && Boolean.TRUE.equals(chest.get(JetlytraItems.JETPACK_ENABLED))
        ) {
            TextHudWidget.render(
                    g, mc.font, I18n.get(LABEL_LOW_FUEL),
                    (float)(JetlytraClientConfig.FUEL_WARNING_X.get() * sw),
                    (float)(JetlytraClientConfig.FUEL_WARNING_Y.get() * sh),
                    JetlytraClientConfig.FUEL_WARNING_SCALE.get().floatValue(),
                    0xFF4444, sw, sh
            );
        }
    }
}
