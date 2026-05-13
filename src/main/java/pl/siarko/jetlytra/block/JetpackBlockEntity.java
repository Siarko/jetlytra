package pl.siarko.jetlytra.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.Animation;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.loading.math.MathParser;
import software.bernie.geckolib.util.GeckoLibUtil;
import pl.siarko.jetlytra.flight.FuelData;
import pl.siarko.jetlytra.item.JetlytraItem;
import pl.siarko.jetlytra.item.JetlytraItems;
import pl.siarko.jetlytra.item.StoredElytra;

import javax.annotation.Nullable;

public class JetpackBlockEntity extends BlockEntity implements GeoBlockEntity {

    private static final RawAnimation INIT_STATE = RawAnimation.begin().then("init_state", Animation.LoopType.HOLD_ON_LAST_FRAME);
    private static final RawAnimation FUEL_BAR = RawAnimation.begin().then("fuel_bar", Animation.LoopType.LOOP);

    private final JetpackFuelItemHandler itemHandler = new JetpackFuelItemHandler(this);

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private boolean jetpackEnabled = false;

    @Nullable
    private FuelData fuelData = null;
    private ItemStack elytraItem = ItemStack.EMPTY;
    private String tier = "";

    public JetpackBlockEntity(BlockPos pos, BlockState state) {
        super(JetlytraBlocks.JETPACK_BE.get(), pos, state);
    }

    public void readFromItem(ItemStack stack) {
        jetpackEnabled = Boolean.TRUE.equals(stack.get(JetlytraItems.JETPACK_ENABLED));
        fuelData = stack.get(JetlytraItems.FUEL_DATA);
        StoredElytra stored = stack.get(JetlytraItems.ELYTRA_ITEM);
        elytraItem = (stored != null && !stored.isEmpty()) ? stored.stack().copy() : ItemStack.EMPTY;
        if (stack.getItem() instanceof JetlytraItem item) tier = item.getTier();
        setChanged();
    }

    public String getTier() {
        return tier;
    }

    public ItemStack createBaseStack() {
        return new ItemStack(JetlytraItems.getItemForTier(tier));
    }

    public void writeToItem(ItemStack stack) {
        if (jetpackEnabled) {
            stack.set(JetlytraItems.JETPACK_ENABLED, true);
        } else {
            stack.remove(JetlytraItems.JETPACK_ENABLED);
        }
        if (fuelData != null) {
            stack.set(JetlytraItems.FUEL_DATA, fuelData);
        } else {
            stack.remove(JetlytraItems.FUEL_DATA);
        }
        if (!elytraItem.isEmpty()) {
            stack.set(JetlytraItems.ELYTRA_ITEM, new StoredElytra(elytraItem.copy()));
        } else {
            stack.remove(JetlytraItems.ELYTRA_ITEM);
        }
    }

    @Nullable
    public FuelData getFuelData() {
        return fuelData;
    }

    public void setFuelData(@Nullable FuelData fuel) {
        this.fuelData = fuel;
        setChanged();
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    public ItemStack getElytraItem() {
        return elytraItem;
    }

    public void setElytraItem(ItemStack stack) {
        this.elytraItem = stack;
        setChanged();
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putBoolean("jetpack_enabled", jetpackEnabled);
        if (!tier.isEmpty()) tag.putString("tier", tier);
        if (fuelData != null) {
            FuelData.CODEC.encodeStart(NbtOps.INSTANCE, fuelData).result()
                    .ifPresent(t -> tag.put("fuel_data", t));
        }
        if (!elytraItem.isEmpty()) {
            tag.put("elytra_item", elytraItem.save(registries));
        }
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadAdditional(tag, registries);
        jetpackEnabled = tag.getBoolean("jetpack_enabled");
        tier = tag.contains("tier") ? tag.getString("tier") : "";
        if (tag.contains("fuel_data")) {
            fuelData = FuelData.CODEC.parse(NbtOps.INSTANCE, tag.get("fuel_data")).result().orElse(null);
        } else {
            fuelData = null;
        }
        elytraItem = tag.contains("elytra_item", CompoundTag.TAG_COMPOUND)
                ? ItemStack.parseOptional(registries, tag.getCompound("elytra_item"))
                : ItemStack.EMPTY;
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "wing_controller", 0, state ->
                state.setAndContinue(INIT_STATE)));

        controllers.add(new AnimationController<>(this, "fuel_gauge_controller", 0, state -> {
            double scale = fuelData != null ? fuelData.count() / (double) FuelData.MAX_COUNT * 6.4 : 0.0;
            MathParser.setVariable("v.fuel_gauge_scale", () -> scale);
            return state.setAndContinue(FUEL_BAR);
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    public JetpackFuelItemHandler getItemHandler() {
        return itemHandler;
    }
}
