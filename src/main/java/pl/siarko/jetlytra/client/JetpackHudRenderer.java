package pl.siarko.jetlytra.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import pl.siarko.jetlytra.config.JetlytraClientConfig;
import pl.siarko.jetlytra.flight.FuelData;
import pl.siarko.jetlytra.item.JetlytraItem;
import pl.siarko.jetlytra.item.JetlytraItems;

public class JetpackHudRenderer {

    private static final ResourceLocation GAUGE_BG =
            ResourceLocation.fromNamespaceAndPath("jetlytra", "textures/hud/fuel_gauge_bg.png");
    private static final ResourceLocation GAUGE_FILL =
            ResourceLocation.fromNamespaceAndPath("jetlytra", "textures/hud/fuel_gauge_fill.png");
    private static final int GAUGE_TEX_W = 64;
    private static final int GAUGE_TEX_H = 8;

    public static void onRenderHud(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;

        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        if (!(chest.getItem() instanceof JetlytraItem)) return;

        FuelData fuel = chest.get(JetlytraItems.FUEL_DATA);
        if (fuel == null) return;

        int percent = Math.round(fuel.count() * 100f / FuelData.MAX_COUNT);

        GuiGraphics graphics = event.getGuiGraphics();
        int screenWidth = graphics.guiWidth();
        int screenHeight = graphics.guiHeight();

        if (JetlytraClientConfig.SHOW_FUEL_PERCENTAGE.get()) {
            String text = percent + "%";
            int cx = (int)(JetlytraClientConfig.FUEL_PERCENT_X.get() * screenWidth);
            int cy = (int)(JetlytraClientConfig.FUEL_PERCENT_Y.get() * screenHeight);
            int x = cx - mc.font.width(text) / 2;
            int y = cy - mc.font.lineHeight / 2;
            graphics.drawString(mc.font, text, x, y, 0xFFFFFF, true);
        }

        if (JetlytraClientConfig.SHOW_FUEL_GAUGE.get()) {
            int gw = GAUGE_TEX_W;
            int gh = GAUGE_TEX_H;
            int gx = (int)(JetlytraClientConfig.FUEL_GAUGE_X.get() * screenWidth) - gw / 2;
            int gy = (int)(JetlytraClientConfig.FUEL_GAUGE_Y.get() * screenHeight) - gh / 2;

            graphics.blit(GAUGE_BG, gx, gy, 0, 0, gw, gh, GAUGE_TEX_W, GAUGE_TEX_H);

            int fillW = Math.max(0, (int)(gw * percent / 100f));
            if (fillW > 0) {
                graphics.enableScissor(gx, gy, gx + fillW, gy + gh);
                graphics.blit(GAUGE_FILL, gx, gy, 0, 0, gw, gh, GAUGE_TEX_W, GAUGE_TEX_H);
                graphics.disableScissor();
            }
        }

        if (JetlytraClientConfig.SHOW_FUEL_WARNING.get()
                && percent <= JetlytraClientConfig.FUEL_WARNING_LEVEL.get()) {
            boolean visible = (System.currentTimeMillis() / 500) % 2 == 0;
            if (visible) {
                String warn = "Low Fuel!";
                int cx = (int)(JetlytraClientConfig.FUEL_WARNING_X.get() * screenWidth);
                int cy = (int)(JetlytraClientConfig.FUEL_WARNING_Y.get() * screenHeight);
                int x = cx - mc.font.width(warn) / 2;
                int y = cy - mc.font.lineHeight / 2;
                graphics.drawString(mc.font, warn, x, y, 0xFF4444, true);
            }
        }
    }
}
