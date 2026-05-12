package pl.siarko.jetlytra.client.render;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;
import pl.siarko.jetlytra.item.JetlytraItem;

public class JetpackGeoModel extends GeoModel<JetlytraItem> {

    private ResourceLocation jetpackPath(JetlytraItem animatable, String folder, String file) {
        String sub = animatable.getTier().isEmpty() ? "" : animatable.getTier() + "/";
        return ResourceLocation.fromNamespaceAndPath("jetlytra", folder + "/" + sub + file);
    }

    @Override
    public ResourceLocation getModelResource(JetlytraItem animatable) {
        return jetpackPath(animatable, "geo", "jetpack.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(JetlytraItem animatable) {
        return jetpackPath(animatable, "textures/geo", "jetpack.png");
    }

    @Override
    public ResourceLocation getAnimationResource(JetlytraItem animatable) {
        return ResourceLocation.fromNamespaceAndPath("jetlytra", "animations/jetpack.animation.json");
    }

    @Override
    public RenderType getRenderType(JetlytraItem animatable, ResourceLocation texture) {
        return RenderType.entityTranslucent(texture);
    }
}
