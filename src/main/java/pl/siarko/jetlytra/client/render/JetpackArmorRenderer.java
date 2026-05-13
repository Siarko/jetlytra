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
import pl.siarko.jetlytra.item.JetlytraItems;
import pl.siarko.jetlytra.item.StoredElytra;

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
        // Counter bone state bleed from the block entity renderer (they share the same cached model).
        // Show wings only when an elytra is actually stored in the jetpack.
        StoredElytra stored = currentStack != null ? currentStack.get(JetlytraItems.ELYTRA_ITEM) : null;
        boolean hasElytra = stored != null && !stored.isEmpty();
        model.getBone("wings").ifPresent(bone -> bone.setHidden(!hasElytra));
        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, color);
    }
}
