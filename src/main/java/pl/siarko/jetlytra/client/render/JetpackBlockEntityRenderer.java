package pl.siarko.jetlytra.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoBlockRenderer;
import software.bernie.geckolib.renderer.layer.BlockAndItemGeoLayer;
import pl.siarko.jetlytra.block.JetpackBlock;
import pl.siarko.jetlytra.block.JetpackBlockEntity;
import pl.siarko.jetlytra.flight.FuelData;

public class JetpackBlockEntityRenderer extends GeoBlockRenderer<JetpackBlockEntity> {

    private static final String FUEL_DISPLAY_BONE = "fuel_display";

    // Offsets in block-local space (relative to facing direction), in blocks.
    // OFFSET_X = right/left, OFFSET_Z = forward/back, OFFSET_Y = up/down.
    public static final double OFFSET_X = 0.0;
    public static final double OFFSET_Y = -0.7;
    public static final double OFFSET_Z = -0.1;

    private static final float ITEM_SCALE = 0.1f;

    public JetpackBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        super(new JetpackBlockGeoModel());
        addRenderLayer(new BlockAndItemGeoLayer<>(this) {
            @Override
            protected ItemStack getStackForBone(GeoBone bone, JetpackBlockEntity animatable) {
                if (!bone.getName().equals(FUEL_DISPLAY_BONE)) return ItemStack.EMPTY;
                FuelData fuelData = animatable.getFuelData();
                if (fuelData == null) return ItemStack.EMPTY;
                return fuelData.getDefinition().map(d -> new ItemStack(d.item())).orElse(ItemStack.EMPTY);
            }

            @Override
            protected ItemDisplayContext getTransformTypeForStack(GeoBone bone, ItemStack stack, JetpackBlockEntity animatable) {
                return ItemDisplayContext.FIXED;
            }

            @Override
            protected void renderStackForBone(PoseStack poseStack, GeoBone bone, ItemStack stack, JetpackBlockEntity animatable,
                                              MultiBufferSource bufferSource, float partialTick, int packedLight, int packedOverlay) {
                // Add any extra transforms here (scale, rotation offset, etc.) before delegating
                poseStack.mulPose(Axis.XP.rotationDegrees(90));
                poseStack.scale(ITEM_SCALE, ITEM_SCALE, ITEM_SCALE);
                super.renderStackForBone(poseStack, bone, stack, animatable, bufferSource, partialTick, packedLight, packedOverlay);
            }
        });
    }

    @Override
    public void preRender(PoseStack poseStack, JetpackBlockEntity animatable, BakedGeoModel model,
                          MultiBufferSource bufferSource, com.mojang.blaze3d.vertex.VertexConsumer buffer,
                          boolean isReRender, float partialTick, int packedLight, int packedOverlay, int color) {
        Direction facing = animatable.getBlockState().getValue(JetpackBlock.FACING);
        double wx, wz;
        switch (facing) {
            case NORTH -> { wx = OFFSET_X; wz =  -OFFSET_Z; }
            case WEST  -> { wx = -OFFSET_Z; wz =  OFFSET_X; }
            case EAST  -> { wx = OFFSET_Z; wz =  -OFFSET_X; }
            default    -> { wx = -OFFSET_X; wz =  OFFSET_Z; }  // SOUTH
        }
        poseStack.translate(wx, OFFSET_Y, wz);

        boolean hasElytra = !animatable.getElytraItem().isEmpty();
        model.getBone("wings").ifPresent(bone -> bone.setHidden(!hasElytra));

        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, color);
    }

    @Override
    protected void rotateBlock(Direction facing, PoseStack poseStack) {
        super.rotateBlock(facing.getOpposite(), poseStack);
    }
}
