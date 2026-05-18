package pl.siarko.jetlytra.compat.curios;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;
import pl.siarko.jetlytra.compat.curios.render.JetpackCurioRenderer;
import pl.siarko.jetlytra.item.JetlytraItems;

public class JetlytraCuriosClientCompat {

    public static void register(IEventBus modBus) {
        modBus.addListener(JetlytraCuriosClientCompat::onClientSetup);
    }

    private static void onClientSetup(FMLClientSetupEvent event) {
        CuriosRendererRegistry.register(JetlytraItems.JETPACK.get(), JetpackCurioRenderer::new);
    }
}
