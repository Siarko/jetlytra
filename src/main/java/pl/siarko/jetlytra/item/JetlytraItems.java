package pl.siarko.jetlytra.item;

import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Unbreakable;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import pl.siarko.jetlytra.Jetlytra;
import pl.siarko.jetlytra.flight.FlightState;
import pl.siarko.jetlytra.flight.FuelData;

import java.util.function.Supplier;

public class JetlytraItems {

    public static final String TIER_NETHERITE = "netherite";
    public static final String TIER_DIAMOND = "diamond";

    public static final String ITEM_THRUSTER = "thruster";
    public static final String ITEM_JETPACK = "jetpack";
    public static final String ITEM_JETPACK_DIAMOND = "jetpack_diamond";
    public static final String ITEM_JETPACK_NETHERITE = "jetpack_netherite";

    public static final String COMPONENT_JETPACK_ENABLED = "jetpack_enabled";
    public static final String COMPONENT_FUEL_DATA = "fuel_data";
    public static final String COMPONENT_ELYTRA_ITEM = "elytra_item";
    public static final String COMPONENT_FLIGHT_STATE = "flight_state";
    public static final String COMPONENT_THRUST_ACTIVE = "thrust_active";
    public static final String COMPONENT_PREVIEW = "preview";

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(
            Registries.ITEM,
            Jetlytra.MODID
    );

    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS = DeferredRegister.create(
            Registries.DATA_COMPONENT_TYPE,
            Jetlytra.MODID
    );

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> JETPACK_ENABLED =
            DATA_COMPONENTS.register(COMPONENT_JETPACK_ENABLED, () ->
                    DataComponentType.<Boolean>builder()
                            .persistent(Codec.BOOL)
                            .networkSynchronized(ByteBufCodecs.BOOL)
                            .build()
            );

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<FuelData>> FUEL_DATA =
            DATA_COMPONENTS.register(COMPONENT_FUEL_DATA, () ->
                    DataComponentType.<FuelData>builder()
                            .persistent(FuelData.CODEC)
                            .networkSynchronized(FuelData.STREAM_CODEC)
                            .build()
            );

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<StoredElytra>> ELYTRA_ITEM =
            DATA_COMPONENTS.register(COMPONENT_ELYTRA_ITEM, () ->
                    DataComponentType.<StoredElytra>builder()
                            .persistent(StoredElytra.CODEC)
                            .build()
            );

    // Rendering-only components: authoritative state lives in JetlytraAttachments (server)
    // and ClientJetpackState (client). These are written server-side on every state change
    // so that nearby players receive them via vanilla equipment sync and Curios NBT sync.
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<FlightState>> FLIGHT_STATE_COMPONENT =
            DATA_COMPONENTS.register(COMPONENT_FLIGHT_STATE, () ->
                    DataComponentType.<FlightState>builder()
                            .persistent(FlightState.CODEC)
                            .networkSynchronized(FlightState.STREAM_CODEC)
                            .build()
            );

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> THRUST_ACTIVE_COMPONENT =
            DATA_COMPONENTS.register(COMPONENT_THRUST_ACTIVE, () ->
                    DataComponentType.<Boolean>builder()
                            .persistent(Codec.BOOL)
                            .networkSynchronized(ByteBufCodecs.BOOL)
                            .build()
            );

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> PREVIEW =
            DATA_COMPONENTS.register(COMPONENT_PREVIEW, () ->
                    DataComponentType.<Boolean>builder()
                            .persistent(Codec.BOOL)
                            .build()
            );

    public static boolean isJetpackAvailable(ItemStack stack) {
        return stack.getOrDefault(JETPACK_ENABLED, true) && stack.has(FUEL_DATA);
    }

    public static final Supplier<Item> THRUSTER = ITEMS.register(
            ITEM_THRUSTER,
            () -> new Item(new Item.Properties())
    );

    public static final Supplier<JetlytraItemBase> JETPACK = ITEMS.register(
            ITEM_JETPACK,
            () -> getSidedInstance(
                    FMLEnvironment.dist.isClient(),
                    ArmorMaterials.IRON,
                    "",
                    new Item.Properties()
                            .durability(FuelData.MAX_COUNT)
                            .component(DataComponents.DAMAGE, FuelData.MAX_COUNT)
                            .component(DataComponents.UNBREAKABLE, new Unbreakable(false))
            )
    );

    public static final Supplier<JetlytraItemBase> JETPACK_DIAMOND = ITEMS.register(
            ITEM_JETPACK_DIAMOND,
            () -> getSidedInstance(
                    FMLEnvironment.dist.isClient(),
                    ArmorMaterials.DIAMOND,
                    TIER_DIAMOND,
                    new Item.Properties()
                            .durability(FuelData.MAX_COUNT)
                            .component(DataComponents.DAMAGE, FuelData.MAX_COUNT)
                            .component(DataComponents.UNBREAKABLE, new Unbreakable(false))
            )
    );

    public static final Supplier<JetlytraItemBase> JETPACK_NETHERITE = ITEMS.register(
            ITEM_JETPACK_NETHERITE,
            () -> getSidedInstance(
                    FMLEnvironment.dist.isClient(),
                    ArmorMaterials.NETHERITE,
                    TIER_NETHERITE,
                    new Item.Properties()
                            .durability(FuelData.MAX_COUNT)
                            .component(DataComponents.DAMAGE, FuelData.MAX_COUNT)
                            .component(DataComponents.UNBREAKABLE, new Unbreakable(false))
                            .fireResistant()
            )
    );

    public static JetlytraItemBase getItemForTier(String tier) {
        return switch (tier) {
            case TIER_DIAMOND -> JETPACK_DIAMOND.get();
            case TIER_NETHERITE -> JETPACK_NETHERITE.get();
            default -> JETPACK.get();
        };
    }

    public static JetlytraItemBase getSidedInstance(
            boolean isClient,
            Holder<ArmorMaterial> material,
            String tier,
            Item.Properties properties
    ) {
        if (isClient) {
            return new JetlytraItem(material, tier, properties);
        } else {
            return new JetlytraItemBase(material, tier, properties);
        }
    }
}
