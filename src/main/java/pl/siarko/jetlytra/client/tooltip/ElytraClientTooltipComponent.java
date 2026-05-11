package pl.siarko.jetlytra.client.tooltip;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ElytraClientTooltipComponent implements ClientTooltipComponent {

    private static final int ICON_SIZE = 16;
    private static final int ICON_TEXT_GAP = 4;
    private static final int LINE_HEIGHT = 9;
    private static final int ENCHANT_COLOR = 0x55CCFF;

    private final ItemStack elytraStack;
    private final List<Component> enchantmentLines;

    public ElytraClientTooltipComponent(ElytraTooltipData data) {
        this.elytraStack = data.elytraStack();
        this.enchantmentLines = new ArrayList<>();

        ItemEnchantments enchantments = elytraStack.getTagEnchantments();
        for (Map.Entry<net.minecraft.core.Holder<Enchantment>, Integer> entry : enchantments.entrySet()) {
            Enchantment enchantment = entry.getKey().value();
            int lvl = entry.getValue();
            MutableComponent name = enchantment.description().copy();
            if (lvl != 1 || enchantment.getMaxLevel() != 1) {
                name = name.append(Component.literal(" ")).append(Component.translatable("enchantment.level." + lvl));
            }
            this.enchantmentLines.add(name.withStyle(Style.EMPTY.withColor(TextColor.fromRgb(ENCHANT_COLOR))));
        }
    }

    @Override
    public int getHeight() {
        int enchantHeight = enchantmentLines.isEmpty() ? 0 : enchantmentLines.size() * (LINE_HEIGHT + 1) - 1;
        return ICON_SIZE + enchantHeight + 4;
    }

    @Override
    public int getWidth(Font font) {
        int nameWidth = font.width(elytraStack.getHoverName());
        int enchantWidth = enchantmentLines.stream().mapToInt(font::width).max().orElse(0);
        return ICON_SIZE + ICON_TEXT_GAP + Math.max(60, Math.max(nameWidth, enchantWidth));
    }

    @Override
    public void renderImage(@NotNull Font font, int x, int y, GuiGraphics guiGraphics) {
        guiGraphics.fill(x - 1, y - 1, x + ICON_SIZE + 1, y + ICON_SIZE + 1, 0xFF3A3A3A);
        guiGraphics.renderItem(elytraStack, x, y);
        guiGraphics.renderItemDecorations(font, elytraStack, x, y);

        int textX = x + ICON_SIZE + ICON_TEXT_GAP;
        int nameY = y + (ICON_SIZE - font.lineHeight) / 2;
        guiGraphics.drawString(font, elytraStack.getHoverName(), textX, nameY, 0xFFFFFF, false);

        int enchantY = y + ICON_SIZE;
        for (Component line : enchantmentLines) {
            guiGraphics.drawString(font, line, textX, enchantY, 0xFFFFFF, false);
            enchantY += font.lineHeight + 1;
        }
    }
}
