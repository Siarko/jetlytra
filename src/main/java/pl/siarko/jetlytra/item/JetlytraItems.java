package pl.siarko.jetlytra.item;

import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import pl.siarko.jetlytra.Jetlytra;
import pl.siarko.jetlytra.flight.FlightState;
import pl.siarko.jetlytra.flight.FuelData;

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
                            .networkSynchronized(ByteBufCodecs.BOOL)
                            .build()
            );

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<FuelData>> FUEL_DATA =
            DATA_COMPONENTS.register("fuel_data", () ->
                    DataComponentType.<FuelData>builder()
                            .persistent(FuelData.CODEC)
                            .networkSynchronized(FuelData.STREAM_CODEC)
                            .build()
            );

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<StoredElytra>> ELYTRA_ITEM =
            DATA_COMPONENTS.register("elytra_item", () ->
                    DataComponentType.<StoredElytra>builder()
                            .persistent(StoredElytra.CODEC)
                            .build()
            );

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<FlightState>> FLIGHT_STATE_COMPONENT =
            DATA_COMPONENTS.register("flight_state", () ->
                    DataComponentType.<FlightState>builder()
                            .networkSynchronized(FlightState.STREAM_CODEC)
                            .build()
            );

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> THRUST_ACTIVE_COMPONENT =
            DATA_COMPONENTS.register("thrust_active", () ->
                    DataComponentType.<Boolean>builder()
                            .networkSynchronized(ByteBufCodecs.BOOL)
                            .build()
            );

    public static boolean isJetpackAvailable(ItemStack stack) {
        return Boolean.TRUE.equals(stack.get(JETPACK_ENABLED)) && stack.has(FUEL_DATA);
    }

    public static final Supplier<JetlytraItem> JETPACK = ITEMS.register(
            "jetpack",
            () -> new JetlytraItem(ArmorMaterials.IRON, "", new Item.Properties().durability(1000))
    );

    public static final Supplier<JetlytraItem> JETPACK_DIAMOND = ITEMS.register(
            "jetpack_diamond",
            () -> new JetlytraItem(ArmorMaterials.DIAMOND, "diamond", new Item.Properties().durability(1000))
    );

    public static final Supplier<JetlytraItem> JETPACK_NETHERITE = ITEMS.register(
            "jetpack_netherite",
            () -> new JetlytraItem(ArmorMaterials.NETHERITE, "netherite", new Item.Properties().durability(1000).fireResistant())
    );

    public static JetlytraItem getItemForTier(String tier) {
        return switch (tier) {
            case "diamond"   -> JETPACK_DIAMOND.get();
            case "netherite" -> JETPACK_NETHERITE.get();
            default          -> JETPACK.get();
        };
    }
}
