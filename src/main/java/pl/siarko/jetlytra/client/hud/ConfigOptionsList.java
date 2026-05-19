package pl.siarko.jetlytra.client.hud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;

import java.util.List;

public class ConfigOptionsList extends ContainerObjectSelectionList<ConfigOptionsList.RowEntry> {

    private static final int ITEM_H = 26;
    private static final int LABEL_W = 150;
    private static final int CTRL_W = 100;
    private static final int GAP = 8;
    private static final int ROW_W = LABEL_W + GAP + CTRL_W;

    public ConfigOptionsList(Minecraft mc, int screenWidth, int listHeight, int listY) {
        super(mc, screenWidth, listHeight, listY, ITEM_H);
    }

    @Override
    public int getRowWidth() {
        return ROW_W;
    }

    public void addText(Component text, boolean centered) {
        addEntry(new TextEntry(minecraft.font, text, ROW_W, centered));
    }

    public void addWidget(Component label, AbstractWidget widget) {
        addEntry(new WidgetEntry(minecraft.font, label, widget, LABEL_W, GAP));
    }

    // ── Entry types ───────────────────────────────────────────────────────────

    public abstract static class RowEntry extends ContainerObjectSelectionList.Entry<RowEntry> {}

    static class TextEntry extends RowEntry {
        private final Font font;
        private final Component text;
        private final int rowWidth;
        private final boolean centered;

        TextEntry(Font font, Component text, int rowWidth, boolean centered) {
            this.font = font;
            this.text = text;
            this.rowWidth = rowWidth;
            this.centered = centered;
        }

        @Override
        public void render(GuiGraphics g, int index, int top, int left,
                           int width, int height, int mouseX, int mouseY,
                           boolean hovering, float partialTick) {
            int textY = top + (height - 8) / 2;
            if (centered) {
                g.drawCenteredString(font, text, left + rowWidth / 2, textY, 0xFFFFFF);
            } else {
                g.drawString(font, text, left + 4, textY, 0xFFFFFF);
            }
        }

        @Override
        public List<? extends GuiEventListener> children() { return List.of(); }

        @Override
        public List<? extends NarratableEntry> narratables() { return List.of(); }
    }

    static class WidgetEntry extends RowEntry {
        private final Font font;
        private final Component label;
        private final AbstractWidget widget;
        private final int labelW;
        private final int gap;

        WidgetEntry(Font font, Component label, AbstractWidget widget, int labelW, int gap) {
            this.font = font;
            this.label = label;
            this.widget = widget;
            this.labelW = labelW;
            this.gap = gap;
        }

        @Override
        public void render(GuiGraphics g, int index, int top, int left,
                           int width, int height, int mouseX, int mouseY,
                           boolean hovering, float partialTick) {
            int textY = top + (height - 8) / 2;
            g.drawString(font, label, left + 4, textY, 0xFFFFFF);
            widget.setX(left + labelW + gap);
            widget.setY(top + (height - widget.getHeight()) / 2);
            widget.render(g, mouseX, mouseY, partialTick);
        }

        @Override
        public List<? extends GuiEventListener> children() { return List.of(widget); }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return widget instanceof NarratableEntry n ? List.of(n) : List.of();
        }
    }
}
