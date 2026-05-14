package pl.siarko.jetlytra.controlify;

import dev.isxander.controlify.api.ControlifyApi;
import dev.isxander.controlify.api.bind.ControlifyBindApi;
import dev.isxander.controlify.bindings.BindContext;
import dev.isxander.controlify.api.bind.InputBindingSupplier;
import dev.isxander.controlify.api.entrypoint.ControlifyEntrypoint;
import dev.isxander.controlify.api.entrypoint.InitContext;
import dev.isxander.controlify.api.entrypoint.PreInitContext;
import net.minecraft.network.chat.Component;
import pl.siarko.jetlytra.client.input.integration.ControlifyBridge;

public class JetlytraControlifyEntrypoint implements ControlifyEntrypoint {

    public static InputBindingSupplier TRIGGER_THRUST;
    public static InputBindingSupplier TOGGLE_ELYTRA;

    @Override
    public void onControllersDiscovered(ControlifyApi api) {}

    @Override
    public void onControlifyPreInit(PreInitContext context) {
        ControlifyBindApi api = ControlifyBindApi.get();

        TRIGGER_THRUST = api.registerBinding(builder -> builder
                .id("jetlytra", "trigger_thrust")
                .category(Component.translatable("key.categories.jetlytra"))
                .allowedContexts(BindContext.IN_GAME)
        );

        TOGGLE_ELYTRA = api.registerBinding(builder -> builder
                .id("jetlytra", "toggle_elytra")
                .category(Component.translatable("key.categories.jetlytra"))
                .allowedContexts(BindContext.IN_GAME)
        );

        ControlifyBridge.setTriggerThrustSupplier(() ->
                ControlifyApi.get().getCurrentController()
                        .map(controller -> TRIGGER_THRUST.on(controller).digitalNow())
                        .orElse(false)
        );

        ControlifyBridge.setElytraToggleSupplier(() ->
                ControlifyApi.get().getCurrentController()
                        .map(controller -> TOGGLE_ELYTRA.on(controller).justPressed())
                        .orElse(false)
        );
    }

    @Override
    public void onControlifyInit(InitContext context) {}
}
