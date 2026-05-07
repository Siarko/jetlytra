package pl.siarko.jetlytra.client.render;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import software.bernie.geckolib.renderer.GeoBlockRenderer;
import pl.siarko.jetlytra.block.JetpackBlockEntity;

public class JetpackBlockEntityRenderer extends GeoBlockRenderer<JetpackBlockEntity> {

    public JetpackBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        super(new JetpackBlockGeoModel());
    }
}
