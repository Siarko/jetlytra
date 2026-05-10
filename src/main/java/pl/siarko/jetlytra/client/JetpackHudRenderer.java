package pl.siarko.jetlytra.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import pl.siarko.jetlytra.config.JetlytraClientConfig;
import pl.siarko.jetlytra.flight.FuelData;
import pl.siarko.jetlytra.item.JetlytraItem;
import pl.siarko.jetlytra.item.JetlytraItems;

public class JetpackHudRenderer {

    public static void onRenderHud(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;

        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        if (!(chest.getItem() instanceof JetlytraItem)) return;

        FuelData fuel = chest.get(JetlytraItems.FUEL_DATA);
        if (fuel == null) return;

        int percent = Math.round(fuel.count() * 100f / FuelData.MAX_COUNT);
        String text = percent + "%";

        GuiGraphics graphics = event.getGuiGraphics();
        int screenWidth = graphics.guiWidth();
        int screenHeight = graphics.guiHeight();

        int cx = (int)(JetlytraClientConfig.HUD_X.get() * screenWidth);
        int cy = (int)(JetlytraClientConfig.HUD_Y.get() * screenHeight);
        int x = cx - mc.font.width(text) / 2;
        int y = cy - mc.font.lineHeight / 2;

        graphics.drawString(mc.font, text, x, y, 0xFFFFFF, true);
    }
}
