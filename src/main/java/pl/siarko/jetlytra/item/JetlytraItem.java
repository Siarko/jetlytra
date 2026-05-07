package pl.siarko.jetlytra.item;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.Animation;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
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

    private static final RawAnimation WING_OUT = RawAnimation.begin().then("wing_out", Animation.LoopType.HOLD_ON_LAST_FRAME);
    private static final RawAnimation WING_IN = RawAnimation.begin().then("wing_in", Animation.LoopType.HOLD_ON_LAST_FRAME);

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "wing_controller", 5, state -> {
            if (ClientJetpackState.getState() == FlightState.ELYTRA) {
                return state.setAndContinue(WING_OUT);
            } else {
                return state.setAndContinue(WING_IN);
            }
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
