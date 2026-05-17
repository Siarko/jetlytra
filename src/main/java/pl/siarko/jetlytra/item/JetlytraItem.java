package pl.siarko.jetlytra.item;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;
import pl.siarko.jetlytra.client.animation.JetlytraItemAnimations;
import pl.siarko.jetlytra.client.render.JetpackArmorRenderer;

import java.util.function.Consumer;

public class JetlytraItem extends JetlytraItemBase implements GeoItem {

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public JetlytraItem(Holder<ArmorMaterial> material, String tier, Properties properties) {
        super(material, tier, properties);
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
