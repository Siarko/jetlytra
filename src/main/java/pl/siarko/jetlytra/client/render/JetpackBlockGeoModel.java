package pl.siarko.jetlytra.client.render;

import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;
import pl.siarko.jetlytra.block.JetpackBlockEntity;

public class JetpackBlockGeoModel extends GeoModel<JetpackBlockEntity> {

    @Override
    public ResourceLocation getModelResource(JetpackBlockEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath("jetlytra", "geo/jetpack.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(JetpackBlockEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath("jetlytra", "textures/geo/jetpack.png");
    }

    @Override
    public ResourceLocation getAnimationResource(JetpackBlockEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath("jetlytra", "animations/jetpack.animation.json");
    }
}
