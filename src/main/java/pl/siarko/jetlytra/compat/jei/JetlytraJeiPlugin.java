package pl.siarko.jetlytra.compat.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import pl.siarko.jetlytra.Jetlytra;
import pl.siarko.jetlytra.flight.FuelTypeDefinition;
import pl.siarko.jetlytra.flight.FuelTypeRegistry;
import pl.siarko.jetlytra.item.JetlytraItems;

import javax.annotation.Nullable;
import java.util.List;

@JeiPlugin
public class JetlytraJeiPlugin implements IModPlugin {

    @Nullable
    private static IJeiRuntime runtime;
    private static List<FuelTypeDefinition> addedRecipes = List.of();

    @Override
    public @NotNull ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(Jetlytra.MODID, "jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(
                new JetlytraFuelCategory(registration.getJeiHelpers().getGuiHelper())
        );
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalysts(
                JetlytraFuelCategory.RECIPE_TYPE,
                JetlytraItems.JETPACK.get(),
                JetlytraItems.JETPACK_DIAMOND.get(),
                JetlytraItems.JETPACK_NETHERITE.get()
        );
    }

    @Override
    public void onRuntimeAvailable(@NotNull IJeiRuntime jeiRuntime) {
        runtime = jeiRuntime;
        JeiIntegration.setCallback(JetlytraJeiPlugin::refreshRecipes);
        addCurrentRecipes();
    }

    @Override
    public void onRuntimeUnavailable() {
        JeiIntegration.setCallback(null);
        runtime = null;
        addedRecipes = List.of();
    }

    private static void addCurrentRecipes() {
        if (runtime == null) return;
        List<FuelTypeDefinition> recipes = List.copyOf(FuelTypeRegistry.getDefinitions().values());
        if (recipes.isEmpty()) return;
        runtime.getRecipeManager().addRecipes(JetlytraFuelCategory.RECIPE_TYPE, recipes);
        addedRecipes = recipes;
    }

    // Called by JeiIntegration when S2CFuelTypeSyncPacket arrives (e.g. /reload)
    private static void refreshRecipes() {
        if (runtime == null) return;
        if (!addedRecipes.isEmpty()) {
            runtime.getRecipeManager().hideRecipes(JetlytraFuelCategory.RECIPE_TYPE, addedRecipes);
        }
        List<FuelTypeDefinition> updated = List.copyOf(FuelTypeRegistry.getDefinitions().values());
        runtime.getRecipeManager().addRecipes(JetlytraFuelCategory.RECIPE_TYPE, updated);
        addedRecipes = updated;
    }
}
