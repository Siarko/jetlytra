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
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import pl.siarko.jetlytra.block.JetpackBlock;
import pl.siarko.jetlytra.block.JetpackBlockEntity;
import pl.siarko.jetlytra.block.JetlytraBlocks;
import pl.siarko.jetlytra.flight.FuelData;

import java.util.List;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.Animation;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.loading.math.MathParser;
import software.bernie.geckolib.util.GeckoLibUtil;
import pl.siarko.jetlytra.client.render.JetpackArmorRenderer;
import pl.siarko.jetlytra.client.ClientJetpackState;
import pl.siarko.jetlytra.flight.FlightState;

import java.util.function.Consumer;

public class JetlytraItem extends ArmorItem implements GeoItem {

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public JetlytraItem(Holder<ArmorMaterial> material, Properties properties) {
        super(material, Type.CHESTPLATE, properties);
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

        StoredElytra elytra = stack.get(JetlytraItems.ELYTRA_ITEM);
        if (elytra != null && !elytra.isEmpty()) {
            tooltipComponents.add(
                Component.translatable("item.jetlytra.jetpack.elytra_stored",
                        elytra.stack().getHoverName())
                    .withStyle(ChatFormatting.GOLD)
            );
        }
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
                this.renderer.prepForRender(entity, stack, slot, defaultModel);
                return this.renderer;
            }
        });
    }

    private static final RawAnimation INIT_STATE = RawAnimation.begin().then("init_state", Animation.LoopType.HOLD_ON_LAST_FRAME);
    private static final RawAnimation FUEL_BAR = RawAnimation.begin().then("fuel_bar", Animation.LoopType.LOOP);
    private static final RawAnimation WINGS_OPEN_STATE = RawAnimation.begin().then("wings_open_state", Animation.LoopType.HOLD_ON_LAST_FRAME);
    private static final RawAnimation WINGS_OUT = RawAnimation.begin().then("wings_out", Animation.LoopType.HOLD_ON_LAST_FRAME);
    private static final RawAnimation WINGS_IN = RawAnimation.begin().then("wings_in", Animation.LoopType.HOLD_ON_LAST_FRAME);
    private static final RawAnimation BOOST = RawAnimation.begin()
            .then("boost_activate", Animation.LoopType.PLAY_ONCE)
            .then("boost_active", Animation.LoopType.LOOP);

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "wing_controller", 0, state -> {
            RawAnimation current = state.getController().getCurrentRawAnimation();

            if (current == null) {
                boolean elytraActive = ClientJetpackState.getState() == FlightState.ELYTRA;
                return state.setAndContinue(elytraActive ? WINGS_OPEN_STATE : INIT_STATE);
            }

            boolean elytraActive = ClientJetpackState.getState() == FlightState.ELYTRA;

            if (elytraActive) {
                return state.setAndContinue(WINGS_OUT);
            }

            if (current == WINGS_OUT) {
                return state.setAndContinue(WINGS_IN);
            }

            return PlayState.CONTINUE;
        }));

        controllers.add(new AnimationController<>(this, "boost_controller", 0, state -> {
            if (ClientJetpackState.isThrustActive()) {
                return state.setAndContinue(BOOST);
            }
            return PlayState.STOP;
        }));

        controllers.add(new AnimationController<>(this, "fuel_gauge_controller", 0, state -> {
            FuelData fuel = state.getData(DataTickets.ITEMSTACK).get(JetlytraItems.FUEL_DATA);
            double scale = fuel != null ? fuel.count() / (double) FuelData.MAX_COUNT * 6.4 : 0.0;
            MathParser.setVariable("v.fuel_gauge_scale", () -> scale);
            return state.setAndContinue(FUEL_BAR);
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
