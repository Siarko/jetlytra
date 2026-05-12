package pl.siarko.jetlytra.client.render;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;
import pl.siarko.jetlytra.block.JetpackBlockEntity;

public class JetpackBlockGeoModel extends GeoModel<JetpackBlockEntity> {

    private ResourceLocation jetpackPath(JetpackBlockEntity animatable, String folder, String file) {
        String sub = animatable.getTier().isEmpty() ? "" : animatable.getTier() + "/";
        return ResourceLocation.fromNamespaceAndPath("jetlytra", folder + "/" + sub + file);
    }

    @Override
    public ResourceLocation getModelResource(JetpackBlockEntity animatable) {
        return jetpackPath(animatable, "geo", "jetpack.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(JetpackBlockEntity animatable) {
        return jetpackPath(animatable, "textures/geo", "jetpack.png");
    }

    @Override
    public ResourceLocation getAnimationResource(JetpackBlockEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath("jetlytra", "animations/jetpack.animation.json");
    }

    @Override
    public RenderType getRenderType(JetpackBlockEntity animatable, ResourceLocation texture) {
        return RenderType.entityTranslucent(texture);
    }
}
