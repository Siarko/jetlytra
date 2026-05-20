package pl.siarko.jetlytra.item;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import org.jetbrains.annotations.NotNull;
import pl.siarko.jetlytra.client.tooltip.ElytraTooltipData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import pl.siarko.jetlytra.block.JetpackBlock;
import pl.siarko.jetlytra.block.JetpackBlockEntityBase;
import pl.siarko.jetlytra.block.JetlytraBlocks;
import pl.siarko.jetlytra.flight.FuelData;
import pl.siarko.jetlytra.flight.FuelTypeDefinition;

import java.util.List;
import java.util.Optional;

public class JetlytraItemBase extends ArmorItem {

    private static final String TOOLTIP_LABEL_JETPACK_ENABLED = "item.jetlytra.jetpack.enabled";
    private static final String TOOLTIP_LABEL_JETPACK_DISABLED = "item.jetlytra.jetpack.disabled";
    private static final String TOOLTIP_LABEL_FUEL = "item.jetlytra.jetpack.fuel";
    private static final String TOOLTIP_LABEL_NO_FUEL = "item.jetlytra.jetpack.no_fuel";
    private static final String TOOLTIP_LABEL_THRUST_TIME = "item.jetlytra.jetpack.thrust_time";
    private static final String TOOLTIP_LABEL_SHOW_TIP = "item.jetlytra.jetpack.show_tip";
    private static final String TOOLTIP_LABEL_PLACEMENT_TIP = "item.jetlytra.jetpack.placement_tip";

    private final String tier;

    public JetlytraItemBase(Holder<ArmorMaterial> material, String tier, Properties properties) {
        super(material, Type.CHESTPLATE, properties);
        this.tier = tier;
    }

    public String getTier() {
        return tier;
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos clickedPos = context.getClickedPos();
        Direction face = context.getClickedFace();
        BlockPos placePos = clickedPos.relative(face);

        if (context.getPlayer() == null || !context.getPlayer().isShiftKeyDown()) return InteractionResult.PASS;

        if (!level.getBlockState(placePos).canBeReplaced()) return InteractionResult.FAIL;

        if (!level.isClientSide) {
            Direction facing = context.getHorizontalDirection().getOpposite();
            BlockState newState = JetlytraBlocks.JETPACK.get().defaultBlockState().setValue(JetpackBlock.FACING, facing);
            level.setBlock(placePos, newState, 3);

            if (level.getBlockEntity(placePos) instanceof JetpackBlockEntityBase be) {
                be.readFromItem(context.getItemInHand());
            }

            context.getItemInHand().shrink(1);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            @NotNull TooltipContext context,
            List<Component> tooltipComponents,
            @NotNull TooltipFlag tooltipFlag
    ) {
        FuelData fuel = stack.get(JetlytraItems.FUEL_DATA);
        addTooltipFuelInfo(tooltipComponents, fuel);

        addTooltipJetpackStateInfo(tooltipComponents, stack);

        tooltipComponents.add(Component.empty());
        if(Screen.hasShiftDown()){
            addTooltipFuelTime(tooltipComponents, fuel);
            tooltipComponents.add(
                    Component.translatable(TOOLTIP_LABEL_PLACEMENT_TIP).withStyle(ChatFormatting.DARK_GRAY)
            );
        }else{
            tooltipComponents.add(
                    Component.translatable(TOOLTIP_LABEL_SHOW_TIP, "SHIFT").withStyle(ChatFormatting.AQUA)
            );
        }
    }

    private static String formatTicks(int ticks) {
        int totalSeconds = ticks / 20;
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return minutes > 0 ? minutes + "m " + seconds + "s" : seconds + "s";
    }

    @Override
    public boolean isEnchantable(@NotNull ItemStack stack) {
        return false;
    }

    @Override
    public boolean isBookEnchantable(@NotNull ItemStack stack, @NotNull ItemStack book) {
        return false;
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        if (Boolean.TRUE.equals(stack.get(JetlytraItems.PREVIEW))) return false;
        FuelData fuel = stack.get(JetlytraItems.FUEL_DATA);
        int fuelCount = (fuel != null) ? fuel.count() : 0;
        return fuelCount < FuelData.MAX_COUNT;
    }

    public static void syncFuelDamage(ItemStack stack) {
        FuelData fuel = stack.get(JetlytraItems.FUEL_DATA);
        int damage = (fuel == null || fuel.count() <= 0) ? FuelData.MAX_COUNT : FuelData.MAX_COUNT - fuel.count();
        stack.set(DataComponents.DAMAGE, damage);
    }

    @Override
    public @NotNull Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        StoredElytra stored = stack.get(JetlytraItems.ELYTRA_ITEM);
        if (stored == null || stored.isEmpty()) return Optional.empty();
        return Optional.of(new ElytraTooltipData(stored.stack()));
    }

    private void addTooltipFuelInfo(List<Component> tooltipComponents, FuelData fuel) {
        if (fuel != null) {
            String fuelName = fuel.getDefinition().map(FuelTypeDefinition::displayName).orElse("?");
            tooltipComponents.add(
                    Component.translatable(TOOLTIP_LABEL_FUEL, fuel.count(), fuelName)
                            .withStyle(ChatFormatting.GOLD)
            );
        } else {
            tooltipComponents.add(
                    Component.translatable(TOOLTIP_LABEL_NO_FUEL).withStyle(ChatFormatting.DARK_GRAY)
            );
        }
    }

    private void addTooltipFuelTime(List<Component> tooltipComponents, FuelData fuel) {
        if (fuel != null) {
            fuel.getDefinition().ifPresent(def -> tooltipComponents.add(
                    Component.translatable(
                            TOOLTIP_LABEL_THRUST_TIME,
                            formatTicks(fuel.count() * def.ticksPerUnit()),
                            formatTicks(fuel.count() * def.ticksPerUnitHover())
                    ).withStyle(ChatFormatting.AQUA)
            ));
        }
    }

    private void addTooltipJetpackStateInfo(List<Component> tooltipComponents, ItemStack stack) {
        boolean enabled = Boolean.TRUE.equals(stack.get(JetlytraItems.JETPACK_ENABLED));
        tooltipComponents.add(
                Component.translatable(enabled ? TOOLTIP_LABEL_JETPACK_ENABLED : TOOLTIP_LABEL_JETPACK_DISABLED)
                        .withStyle(enabled ? ChatFormatting.GREEN : ChatFormatting.RED)
        );
    }
}
