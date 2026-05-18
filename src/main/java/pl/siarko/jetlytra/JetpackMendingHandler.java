package pl.siarko.jetlytra;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import pl.siarko.jetlytra.JetlytraSlotHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.neoforged.neoforge.event.entity.player.PlayerXpEvent;
import pl.siarko.jetlytra.item.JetlytraItemBase;
import pl.siarko.jetlytra.item.JetlytraItems;
import pl.siarko.jetlytra.item.StoredElytra;

public class JetpackMendingHandler {

    public static void onXpChange(PlayerXpEvent.XpChange event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;
        if (event.getAmount() <= 0) return;

        ItemStack chest = JetlytraSlotHelper.getWornJetlytra(player);
        if (!(chest.getItem() instanceof JetlytraItemBase)) return;

        StoredElytra stored = chest.get(JetlytraItems.ELYTRA_ITEM);
        if (stored == null || stored.isEmpty()) return;

        ItemStack elytra = stored.stack();
        int damage = elytra.getDamageValue();
        if (damage <= 0) return;
        if (!hasMending(elytra)) return;

        ItemStack elytraCopy = elytra.copy();
        int xpToUse = Math.min(event.getAmount(), (damage + 1) / 2);
        int repaired = Math.min(xpToUse * 2, damage);
        elytraCopy.setDamageValue(damage - repaired);

        chest.set(JetlytraItems.ELYTRA_ITEM, new StoredElytra(elytraCopy));
        event.setAmount(event.getAmount() - xpToUse);
    }

    private static boolean hasMending(ItemStack stack) {
        return stack.getTagEnchantments().entrySet().stream()
                .anyMatch(e -> e.getKey().is(Enchantments.MENDING));
    }
}
