package pl.siarko.jetlytra.compat.curios.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import pl.siarko.jetlytra.client.render.JetpackArmorRenderer;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;

import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;
import pl.siarko.jetlytra.item.JetlytraItem;

public class JetpackCurioRenderer implements ICurioRenderer {

    @Override
    @SuppressWarnings("UnstableApiUsage")
    public <T extends LivingEntity, M extends EntityModel<T>> void render(
            ItemStack stack, SlotContext slotContext, PoseStack poseStack,
            RenderLayerParent<T, M> renderLayerParent,
            MultiBufferSource bufferSource, int light,
            float limbSwing, float limbSwingAmount,
            float partialTicks, float ageInTicks,
            float netHeadYaw, float headPitch
    ) {

        if (!(stack.getItem() instanceof JetlytraItem item)) return;

        M entityModel = renderLayerParent.getModel();
        if (!(entityModel instanceof HumanoidModel<?>)) return;

        @SuppressWarnings("unchecked")
        T typedEntity = (T) slotContext.entity();
        @SuppressWarnings("unchecked")
        HumanoidModel<T> typedModel = (HumanoidModel<T>) entityModel;

        // getGeoArmorRenderer calls prepare() internally, which sets up the GeckoLib render state
        HumanoidModel<?> armorModel = GeoRenderProvider.of(item).getGeoArmorRenderer(typedEntity, stack, EquipmentSlot.CHEST, typedModel);
        if (!(armorModel instanceof JetpackArmorRenderer armorRenderer)) return;

        // renderToBuffer is GeckoLib's external render entry point; it resolves its own
        // MultiBufferSource and RenderType from Minecraft's global state and ignores the VertexConsumer parameter
        armorRenderer.renderToBuffer(poseStack, null, light, OverlayTexture.NO_OVERLAY, -1);
    }
}
