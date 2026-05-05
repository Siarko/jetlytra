package pl.siarko.jetlytra.item;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredRegister;
import pl.siarko.jetlytra.Jetlytra;

import java.util.function.Supplier;

public class JetlytraItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(Registries.ITEM, Jetlytra.MODID);

    public static final Supplier<JetlytraItem> JETPACK = ITEMS.register(
            "jetpack",
            () -> new JetlytraItem(ArmorMaterials.GOLD, new Item.Properties().durability(1000))
    );
}
