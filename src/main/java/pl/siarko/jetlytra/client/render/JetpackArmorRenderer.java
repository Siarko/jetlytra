package pl.siarko.jetlytra.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoArmorRenderer;
import pl.siarko.jetlytra.item.JetlytraItem;

public class JetpackArmorRenderer extends GeoArmorRenderer<JetlytraItem> {

    public JetpackArmorRenderer() {
        super(new JetpackGeoModel());
    }

    public void prepare(Entity entity, ItemStack stack, EquipmentSlot slot, HumanoidModel<?> parentModel) {
        prepForRender(entity, stack, slot, parentModel, null, 0f, 0f, 0f, 0f, 0f);
    }

    @Override
    public RenderType getRenderType(JetlytraItem animatable, ResourceLocation texture, MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityTranslucent(texture);
    }

    @Override
    public void preRender(PoseStack poseStack, JetlytraItem animatable, BakedGeoModel model,
                          MultiBufferSource bufferSource, VertexConsumer buffer,
                          boolean isReRender, float partialTick, int packedLight, int packedOverlay, int color) {
        // Reset wings visibility — the block entity renderer mutates the shared cached bone,
        // which bleeds into this renderer when both use the same geo model path (same tier).
        // Wing fold/unfold is handled by animations, not setHidden, so wings are always visible here.
        model.getBone("wings").ifPresent(bone -> bone.setHidden(false));
        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, color);
    }
}
