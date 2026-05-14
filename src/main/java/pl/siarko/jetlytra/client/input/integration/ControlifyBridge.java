package pl.siarko.jetlytra.client.input.integration;

import java.util.function.BooleanSupplier;

public class ControlifyBridge {
    private static BooleanSupplier triggerThrustSupplier = () -> false;
    private static BooleanSupplier elytraToggleSupplier = () -> false;

    public static void setTriggerThrustSupplier(BooleanSupplier supplier) {
        triggerThrustSupplier = supplier;
    }

    public static void setElytraToggleSupplier(BooleanSupplier supplier) {
        elytraToggleSupplier = supplier;
    }

    public static boolean isTriggerThrusting() {
        return triggerThrustSupplier.getAsBoolean();
    }

    public static boolean isElytraToggleJustPressed() {
        return elytraToggleSupplier.getAsBoolean();
    }
}
