package pl.siarko.jetlytra.block;

import com.mojang.serialization.DynamicOps;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;
import pl.siarko.jetlytra.flight.FuelData;
import pl.siarko.jetlytra.item.JetlytraItems;

import javax.annotation.Nullable;

public class JetpackBlockEntity extends BlockEntity implements GeoBlockEntity {

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private boolean jetpackEnabled = false;
    @Nullable private FuelData fuelData = null;

    public JetpackBlockEntity(BlockPos pos, BlockState state) {
        super(JetlytraBlocks.JETPACK_BE.get(), pos, state);
    }

    public void readFromItem(ItemStack stack) {
        jetpackEnabled = Boolean.TRUE.equals(stack.get(JetlytraItems.JETPACK_ENABLED));
        fuelData = stack.get(JetlytraItems.FUEL_DATA);
        setChanged();
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
    }

    @Nullable
    public FuelData getFuelData() {
        return fuelData;
    }

    public void setFuelData(@Nullable FuelData fuel) {
        this.fuelData = fuel;
        setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putBoolean("jetpack_enabled", jetpackEnabled);
        if (fuelData != null) {
            FuelData.CODEC.encodeStart(NbtOps.INSTANCE, fuelData).result()
                    .ifPresent(t -> tag.put("fuel_data", t));
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        jetpackEnabled = tag.getBoolean("jetpack_enabled");
        if (tag.contains("fuel_data")) {
            fuelData = FuelData.CODEC.parse(NbtOps.INSTANCE, tag.get("fuel_data")).result().orElse(null);
        } else {
            fuelData = null;
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // Static display — no animations
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
