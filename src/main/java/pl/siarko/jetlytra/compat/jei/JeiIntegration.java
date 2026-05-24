package pl.siarko.jetlytra.compat.jei;

import javax.annotation.Nullable;

public class JeiIntegration {

    @Nullable
    private static Runnable onFuelTypesUpdated;

    static void setCallback(@Nullable Runnable callback) {
        onFuelTypesUpdated = callback;
    }

    public static void notifyFuelTypesUpdated() {
        if (onFuelTypesUpdated != null) onFuelTypesUpdated.run();
    }
}
