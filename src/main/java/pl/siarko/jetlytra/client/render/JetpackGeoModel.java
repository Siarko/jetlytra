package pl.siarko.jetlytra.client.render;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;
import pl.siarko.jetlytra.item.JetlytraItem;

public class JetpackGeoModel extends GeoModel<JetlytraItem> {

    static final ResourceLocation ANIMATION = ResourceLocation.fromNamespaceAndPath("jetlytra", "animations/jetpack.animation.json");

    static ResourceLocation assetPath(String tier, String folder, String file) {
        String sub = tier.isEmpty() ? "" : tier + "/";
        return ResourceLocation.fromNamespaceAndPath("jetlytra", folder + "/" + sub + file);
    }

    @Override
    public ResourceLocation getModelResource(JetlytraItem animatable) {
        return assetPath(animatable.getTier(), "geo", "jetpack.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(JetlytraItem animatable) {
        return assetPath(animatable.getTier(), "textures/geo", "jetpack.png");
    }

    @Override
    public ResourceLocation getAnimationResource(JetlytraItem animatable) {
        return ANIMATION;
    }

    @Override
    public RenderType getRenderType(JetlytraItem animatable, ResourceLocation texture) {
        return RenderType.entityTranslucent(texture);
    }
}
