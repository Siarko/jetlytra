package pl.siarko.jetlytra.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;
import pl.siarko.jetlytra.block.JetpackBlock;
import pl.siarko.jetlytra.block.JetpackBlockEntity;

public class JetpackBlockEntityRenderer extends GeoBlockRenderer<JetpackBlockEntity> {

    // Offsets in block-local space (relative to facing direction), in blocks.
    // OFFSET_X = right/left, OFFSET_Z = forward/back, OFFSET_Y = up/down.
    public static final double OFFSET_X = 0.0;
    public static final double OFFSET_Y = -0.7;
    public static final double OFFSET_Z = -0.1;

    public JetpackBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        super(new JetpackBlockGeoModel());
    }

    @Override
    public void preRender(PoseStack poseStack, JetpackBlockEntity animatable, BakedGeoModel model,
                          MultiBufferSource bufferSource, com.mojang.blaze3d.vertex.VertexConsumer buffer,
                          boolean isReRender, float partialTick, int packedLight, int packedOverlay, int color) {
        Direction facing = animatable.getBlockState().getValue(JetpackBlock.FACING);
        double wx, wz;
        switch (facing) {
            case NORTH -> { wx = -OFFSET_X; wz = -OFFSET_Z; }
            case WEST  -> { wx =  OFFSET_Z; wz = -OFFSET_X; }
            case EAST  -> { wx = -OFFSET_Z; wz =  OFFSET_X; }
            default    -> { wx =  OFFSET_X; wz =  OFFSET_Z; }  // SOUTH
        }
        poseStack.translate(wx, OFFSET_Y, wz);
        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, color);
    }
}
