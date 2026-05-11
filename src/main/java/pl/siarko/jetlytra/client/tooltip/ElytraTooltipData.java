package pl.siarko.jetlytra.client.tooltip;

import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

public record ElytraTooltipData(ItemStack elytraStack) implements TooltipComponent {}
