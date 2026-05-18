package pl.siarko.jetlytra;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import pl.siarko.jetlytra.compat.CuriosBridge;
import pl.siarko.jetlytra.item.JetlytraItemBase;

public class JetlytraSlotHelper {

    public static ItemStack getWornJetlytra(Player player) {
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        if (chest.getItem() instanceof JetlytraItemBase) return chest;
        return CuriosBridge.findBackSlotJetlytra(player);
    }
}
