package pl.siarko.jetlytra.client.hud;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import pl.siarko.jetlytra.block.JetpackBlock;
import pl.siarko.jetlytra.block.JetpackBlockEntityBase;
import pl.siarko.jetlytra.client.tooltip.ElytraTooltipData;
import pl.siarko.jetlytra.flight.FuelData;
import pl.siarko.jetlytra.flight.FuelTypeDefinition;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JetpackBlockTooltipRenderer {

    private static Matrix4f capturedMVP = null;
    private static Vec3 capturedCameraPos = null;

    // Reused per-frame to avoid allocating a new Vector4f for each corner projection.
    private static final Vector4f CLIP = new Vector4f();

    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_SOLID_BLOCKS) return;
        capturedMVP = new Matrix4f(event.getProjectionMatrix()).mul(event.getModelViewMatrix());
        capturedCameraPos = event.getCamera().getPosition();
    }

    public static void onRenderHud(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;
        if (capturedMVP == null || capturedCameraPos == null) return;
        if (!(mc.hitResult instanceof BlockHitResult bhr)) return;

        BlockPos pos = bhr.getBlockPos();
        BlockState state = mc.level.getBlockState(pos);
        if (!(state.getBlock() instanceof JetpackBlock)) return;
        if (!(mc.level.getBlockEntity(pos) instanceof JetpackBlockEntityBase be)) return;

        GuiGraphics g = event.getGuiGraphics();
        int sw = g.guiWidth();
        int sh = g.guiHeight();

        AABB box = state.getShape(mc.level, pos).bounds().move(pos);
        float[] edge = projectBoxRightEdge(box, sw, sh);
        if (edge == null) return;

        ItemStack elytra = be.getElytraItem();
        Optional<net.minecraft.world.inventory.tooltip.TooltipComponent> elytraTooltip =
                elytra.isEmpty() ? Optional.empty() : Optional.of(new ElytraTooltipData(elytra));

        g.renderTooltip(mc.font, buildTooltip(be.getCustomName(), be.getFuelData()), elytraTooltip,
                (int) edge[0] + 4, (int) edge[1]);
    }

    // Returns [maxScreenX, midScreenY] of the projected AABB corners, or null if all are behind the camera.
    @Nullable
    private static float[] projectBoxRightEdge(AABB box, int sw, int sh) {
        float maxX = -Float.MAX_VALUE;
        float sumY = 0;
        int count = 0;

        for (int i = 0; i < 8; i++) {
            double wx = (i & 1) != 0 ? box.maxX : box.minX;
            double wy = (i & 2) != 0 ? box.maxY : box.minY;
            double wz = (i & 4) != 0 ? box.maxZ : box.minZ;

            CLIP.set(
                    (float)(wx - capturedCameraPos.x),
                    (float)(wy - capturedCameraPos.y),
                    (float)(wz - capturedCameraPos.z),
                    1f
            );
            capturedMVP.transform(CLIP);
            if (CLIP.w <= 0f) continue;

            float sx = (CLIP.x / CLIP.w + 1f) / 2f * sw;
            float sy = (1f - CLIP.y / CLIP.w) / 2f * sh;
            if (sx > maxX) maxX = sx;
            sumY += sy;
            count++;
        }

        return count > 0 ? new float[]{maxX, sumY / count} : null;
    }

    private static List<Component> buildTooltip(@Nullable Component customName, FuelData fuel) {
        List<Component> lines = new ArrayList<>();
        if (customName != null) {
            lines.add(customName.copy().withStyle(ChatFormatting.ITALIC));
        } else {
            lines.add(Component.translatable("block.jetlytra.jetpack"));
        }
        if (fuel == null || fuel.count() == 0) {
            lines.add(Component.translatable("tooltip.jetlytra.block.no_fuel").withStyle(ChatFormatting.DARK_GRAY));
        } else {
            String displayName = fuel.getDefinition().map(FuelTypeDefinition::displayName).orElse("?");
            lines.add(Component.literal(displayName).withStyle(ChatFormatting.GOLD));
            lines.add(Component.translatable("tooltip.jetlytra.block.fuel_count", fuel.count(), FuelData.MAX_COUNT)
                    .withStyle(ChatFormatting.GRAY));
        }
        return lines;
    }
}
