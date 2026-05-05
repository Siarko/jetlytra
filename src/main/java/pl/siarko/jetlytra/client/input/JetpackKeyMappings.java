package pl.siarko.jetlytra.client.input;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

public class JetpackKeyMappings {

    public static final KeyMapping TOGGLE_JETPACK = new KeyMapping(
            "key.jetlytra.toggle",
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_R,
            "key.categories.jetlytra"
    );

    public static final KeyMapping TOGGLE_ELYTRA = new KeyMapping(
            "key.jetlytra.toggle_elytra",
            InputConstants.Type.KEYSYM,
            InputConstants.UNKNOWN.getValue(),
            "key.categories.jetlytra"
    );

    public static void register(RegisterKeyMappingsEvent event) {
        event.register(TOGGLE_JETPACK);
        event.register(TOGGLE_ELYTRA);
    }
}
