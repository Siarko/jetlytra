package pl.siarko.jetlytra;

import net.minecraft.core.Direction;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import pl.siarko.jetlytra.block.JetlytraBlocks;

public class JetlytraCapabilities {

    public static void register(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
            Capabilities.ItemHandler.BLOCK,
            JetlytraBlocks.JETPACK_BE.get(),
            (be, side) -> (side == Direction.UP || side == null) ? be.getItemHandler() : null
        );
    }
}
