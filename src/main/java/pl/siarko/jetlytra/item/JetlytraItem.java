package pl.siarko.jetlytra.item;

import net.minecraft.ChatFormatting;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import pl.siarko.jetlytra.client.tooltip.ElytraTooltipData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import pl.siarko.jetlytra.block.JetpackBlock;
import pl.siarko.jetlytra.block.JetpackBlockEntity;
import pl.siarko.jetlytra.block.JetlytraBlocks;
import pl.siarko.jetlytra.flight.FuelData;

import java.util.List;
import java.util.Optional;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;
import pl.siarko.jetlytra.client.animation.JetlytraItemAnimations;
import pl.siarko.jetlytra.client.render.JetpackArmorRenderer;

import java.util.function.Consumer;

public class JetlytraItem extends ArmorItem implements GeoItem {

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final String tier;

    public JetlytraItem(Holder<ArmorMaterial> material, String tier, Properties properties) {
        super(material, Type.CHESTPLATE, properties);
        this.tier = tier;
    }

    public String getTier() {
        return tier;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos clickedPos = context.getClickedPos();
        Direction face = context.getClickedFace();
        BlockPos placePos = clickedPos.relative(face);

        if (!context.getPlayer().isShiftKeyDown()) return InteractionResult.PASS;

        if (!level.getBlockState(placePos).canBeReplaced()) return InteractionResult.FAIL;

        if (!level.isClientSide) {
            Direction facing = context.getHorizontalDirection().getOpposite();
            BlockState newState = JetlytraBlocks.JETPACK.get().defaultBlockState()
                    .setValue(JetpackBlock.FACING, facing);
            level.setBlock(placePos, newState, 3);

            if (level.getBlockEntity(placePos) instanceof JetpackBlockEntity be) {
                be.readFromItem(context.getItemInHand());
            }

            if (!context.getPlayer().isCreative()) {
                context.getItemInHand().shrink(1);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        boolean enabled = Boolean.TRUE.equals(stack.get(JetlytraItems.JETPACK_ENABLED));
        tooltipComponents.add(
            Component.translatable(enabled ? "item.jetlytra.jetpack.enabled" : "item.jetlytra.jetpack.disabled")
                .withStyle(enabled ? ChatFormatting.GREEN : ChatFormatting.RED)
        );

        FuelData fuel = stack.get(JetlytraItems.FUEL_DATA);
        if (fuel != null) {
            tooltipComponents.add(
                Component.translatable("item.jetlytra.jetpack.fuel", fuel.count(), fuel.type().displayName)
                    .withStyle(ChatFormatting.GOLD)
            );
        } else {
            tooltipComponents.add(
                Component.translatable("item.jetlytra.jetpack.no_fuel")
                    .withStyle(ChatFormatting.DARK_GRAY)
            );
        }

    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        StoredElytra stored = stack.get(JetlytraItems.ELYTRA_ITEM);
        if (stored == null || stored.isEmpty()) return Optional.empty();
        return Optional.of(new ElytraTooltipData(stored.stack()));
    }

    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(new GeoRenderProvider() {
            private JetpackArmorRenderer renderer;

            @Override
            public <T extends LivingEntity> HumanoidModel<?> getGeoArmorRenderer(
                    T entity,
                    ItemStack stack,
                    EquipmentSlot slot,
                    HumanoidModel<T> defaultModel
            ) {
                if (this.renderer == null) {
                    this.renderer = new JetpackArmorRenderer();
                }
                this.renderer.prepare(entity, stack, slot, defaultModel);
                return this.renderer;
            }
        });
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(JetlytraItemAnimations.wings(this));
        controllers.add(JetlytraItemAnimations.boost(this));
        controllers.add(JetlytraItemAnimations.fuelGauge(this));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
