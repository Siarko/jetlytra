package pl.siarko.jetlytra.compat;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import pl.siarko.jetlytra.item.JetlytraItems;

import java.util.function.Function;

public class CuriosBridge {

    private static Function<Player, ItemStack> finder = p -> ItemStack.EMPTY;

    public static void setFinder(Function<Player, ItemStack> f) {
        finder = f;
    }

    public static ItemStack findBackSlotJetlytra(Player player) {
        return finder.apply(player);
    }
}
