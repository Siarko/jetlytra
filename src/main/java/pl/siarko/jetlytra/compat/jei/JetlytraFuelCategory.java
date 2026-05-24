package pl.siarko.jetlytra.compat.jei;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import pl.siarko.jetlytra.Jetlytra;
import pl.siarko.jetlytra.flight.FuelTypeDefinition;
import pl.siarko.jetlytra.item.JetlytraItems;

public class JetlytraFuelCategory extends AbstractRecipeCategory<FuelTypeDefinition> {

    public static final RecipeType<FuelTypeDefinition> RECIPE_TYPE = new RecipeType<>(
            ResourceLocation.fromNamespaceAndPath(Jetlytra.MODID, "fuel"),
            FuelTypeDefinition.class
    );

    private static final int WIDTH = 140;
    private static final int HEIGHT = 36;
    private static final int SLOT_Y = 9;
    private static final int TEXT_X = 22;
    private static final int TEXT_COLOR = 0x404040;
    private static final int LINE_HEIGHT = 11;

    public JetlytraFuelCategory(IGuiHelper helper) {
        super(
                RECIPE_TYPE,
                Component.translatable("jei.jetlytra.fuel"),
                helper.createDrawableItemLike(JetlytraItems.JETPACK.get()),
                WIDTH,
                HEIGHT
        );
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, FuelTypeDefinition def, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 0, SLOT_Y)
               .addItemStack(new ItemStack(def.item()));
    }

    @Override
    public void draw(FuelTypeDefinition def, IRecipeSlotsView slotsView, GuiGraphics graphics, double mouseX, double mouseY) {
        var font = Minecraft.getInstance().font;
        graphics.drawString(font, thrustLine(def), TEXT_X, 2, TEXT_COLOR, false);
        graphics.drawString(font, hoverLine(def), TEXT_X, 2 + LINE_HEIGHT, TEXT_COLOR, false);
        graphics.drawString(font, speedLine(def), TEXT_X, 2 + LINE_HEIGHT * 2, TEXT_COLOR, false);
    }

    private static Component thrustLine(FuelTypeDefinition def) {
        return Component.translatable("jei.jetlytra.fuel.thrust", formatTime(def.ticksPerUnit()));
    }

    private static Component hoverLine(FuelTypeDefinition def) {
        return Component.translatable("jei.jetlytra.fuel.hover", formatTime(def.ticksPerUnitHover()));
    }

    private static Component speedLine(FuelTypeDefinition def) {
        float m = def.accelerationMultiplier();
        String formatted = (m == Math.floor(m))
                ? String.valueOf((int) m)
                : String.format("%.2f", m).replaceAll("0+$", "");
        return Component.translatable("jei.jetlytra.fuel.speed", formatted + "×");
    }

    private static String formatTime(int ticks) {
        if (ticks < 20) {
            return ticks + " t";
        }
        double seconds = ticks / 20.0;
        return (seconds == Math.floor(seconds))
                ? (int) seconds + " s"
                : String.format("%.1f s", seconds);
    }
}
