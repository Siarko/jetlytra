package pl.siarko.jetlytra.client.render;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoArmorRenderer;
import pl.siarko.jetlytra.item.JetlytraItem;

public class JetpackArmorRenderer extends GeoArmorRenderer<JetlytraItem> {

    public JetpackArmorRenderer() {
        super(new JetpackGeoModel());
    }

    @Override
    public RenderType getRenderType(JetlytraItem animatable, ResourceLocation texture, MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityTranslucent(texture);
    }
}
