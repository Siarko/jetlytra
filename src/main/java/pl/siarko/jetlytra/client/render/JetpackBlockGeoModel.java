package pl.siarko.jetlytra.client.render;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;
import pl.siarko.jetlytra.block.JetpackBlockEntity;

public class JetpackBlockGeoModel extends GeoModel<JetpackBlockEntity> {

    @Override
    public ResourceLocation getModelResource(JetpackBlockEntity animatable) {
        return JetpackGeoModel.assetPath(animatable.getTier(), "geo", "jetpack.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(JetpackBlockEntity animatable) {
        return JetpackGeoModel.assetPath(animatable.getTier(), "textures/geo", "jetpack.png");
    }

    @Override
    public ResourceLocation getAnimationResource(JetpackBlockEntity animatable) {
        return JetpackGeoModel.ANIMATION;
    }

    @Override
    public RenderType getRenderType(JetpackBlockEntity animatable, ResourceLocation texture) {
        return RenderType.entityTranslucent(texture);
    }
}
