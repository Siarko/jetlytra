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
import pl.siarko.jetlytra.flight.FuelData;
import pl.siarko.jetlytra.item.JetlytraItemBase;
import pl.siarko.jetlytra.item.JetlytraItems;
import pl.siarko.jetlytra.item.StoredElytra;

import javax.annotation.Nullable;

public class JetpackBlockEntityBase extends BlockEntity {

    public static final String TAG_JETPACK_ENABLED = "jetpack_enabled";
    public static final String TAG_TIER = "tier";
    public static final String TAG_FUEL_DATA = "fuel_data";
    public static final String TAG_ELYTRA_ITEM = "elytra_item";

    private final JetpackFuelItemHandler itemHandler = new JetpackFuelItemHandler(this);

    private boolean jetpackEnabled = false;

    @Nullable
    private FuelData fuelData = null;
    private ItemStack elytraItem = ItemStack.EMPTY;
    private String tier = "";

    public JetpackBlockEntityBase(BlockPos pos, BlockState state) {
        super(JetlytraBlocks.JETPACK_BE.get(), pos, state);
    }

    public void readFromItem(ItemStack stack) {
        jetpackEnabled = Boolean.TRUE.equals(stack.get(JetlytraItems.JETPACK_ENABLED));
        fuelData = stack.get(JetlytraItems.FUEL_DATA);
        StoredElytra stored = stack.get(JetlytraItems.ELYTRA_ITEM);
        elytraItem = (stored != null && !stored.isEmpty()) ? stored.stack().copy() : ItemStack.EMPTY;
        if (stack.getItem() instanceof JetlytraItemBase item) tier = item.getTier();
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
        JetlytraItemBase.syncFuelDamage(stack);
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
        tag.putBoolean(TAG_JETPACK_ENABLED, jetpackEnabled);
        if (!tier.isEmpty()) tag.putString(TAG_TIER, tier);
        if (fuelData != null) {
            FuelData.CODEC.encodeStart(NbtOps.INSTANCE, fuelData).result().ifPresent(t -> tag.put(TAG_FUEL_DATA, t));
        }
        if (!elytraItem.isEmpty()) {
            tag.put(TAG_ELYTRA_ITEM, elytraItem.save(registries));
        }
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadAdditional(tag, registries);
        jetpackEnabled = tag.getBoolean(TAG_JETPACK_ENABLED);
        tier = tag.contains(TAG_TIER) ? tag.getString(TAG_TIER) : "";
        if (tag.contains(TAG_FUEL_DATA)) {
            fuelData = FuelData.CODEC.parse(NbtOps.INSTANCE, tag.get(TAG_FUEL_DATA)).result().orElse(null);
        } else {
            fuelData = null;
        }
        elytraItem = tag.contains(TAG_ELYTRA_ITEM, CompoundTag.TAG_COMPOUND)
                ? ItemStack.parseOptional(registries, tag.getCompound(TAG_ELYTRA_ITEM))
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

    public JetpackFuelItemHandler getItemHandler() {
        return itemHandler;
    }
}
