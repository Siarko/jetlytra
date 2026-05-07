package pl.siarko.jetlytra.item;

import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import pl.siarko.jetlytra.Jetlytra;

import java.util.function.Supplier;

public class JetlytraItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(
            Registries.ITEM,
            Jetlytra.MODID
    );

    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS = DeferredRegister.create(
            Registries.DATA_COMPONENT_TYPE,
            Jetlytra.MODID
    );

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> JETPACK_ENABLED =
            DATA_COMPONENTS.register("jetpack_enabled", () ->
                    DataComponentType.<Boolean>builder()
                            .persistent(Codec.BOOL)
                            .build()
            );

    public static final Supplier<JetlytraItem> JETPACK = ITEMS.register(
            "jetpack",
            () -> new JetlytraItem(ArmorMaterials.GOLD, new Item.Properties().durability(1000))
    );
}
