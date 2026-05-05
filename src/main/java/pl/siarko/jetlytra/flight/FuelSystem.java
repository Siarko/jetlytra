package pl.siarko.jetlytra.flight;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class FuelSystem {

    // Ticks between each durability drain tick (20 = 1 drain per second)
    private static final int DRAIN_INTERVAL = 20;

    private int tickCounter = 0;

    public boolean tick(Player player) {
        tickCounter++;
        if (tickCounter < DRAIN_INTERVAL) {
            return true;
        }
        tickCounter = 0;

        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        if (chest.isEmpty() || chest.getMaxDamage() == 0) {
            return false;
        }

        if (chest.getDamageValue() >= chest.getMaxDamage() - 1) {
            return false; // out of fuel
        }

        chest.setDamageValue(chest.getDamageValue() + 1);
        return true;
    }

    public static boolean hasAnyFuel(Player player) {
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        return !chest.isEmpty() && chest.getDamageValue() < chest.getMaxDamage() - 1;
    }
}
