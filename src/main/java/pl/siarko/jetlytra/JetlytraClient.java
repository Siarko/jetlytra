package pl.siarko.jetlytra;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import pl.siarko.jetlytra.client.tooltip.ElytraClientTooltipComponent;
import pl.siarko.jetlytra.client.tooltip.ElytraTooltipData;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import pl.siarko.jetlytra.block.JetlytraBlocks;
import pl.siarko.jetlytra.client.hud.JetpackHudRenderer;
import pl.siarko.jetlytra.client.hud.JetlytraConfigScreen;
import pl.siarko.jetlytra.client.input.JetpackInputHandler;
import pl.siarko.jetlytra.client.input.JetpackKeyMappings;
import pl.siarko.jetlytra.client.input.KeyStateTracker;
import pl.siarko.jetlytra.client.render.JetpackBlockEntityRenderer;

@Mod(value = Jetlytra.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = Jetlytra.MODID, value = Dist.CLIENT)
public class JetlytraClient {

    public JetlytraClient(IEventBus modEventBus, ModContainer modContainer) {
        KeyStateTracker keyStateTracker = new KeyStateTracker();
        JetpackInputHandler jetpackInputHandler = new JetpackInputHandler(keyStateTracker);
        NeoForge.EVENT_BUS.addListener(jetpackInputHandler::onClientTick);
        NeoForge.EVENT_BUS.addListener((ClientPlayerNetworkEvent.LoggingOut event) -> keyStateTracker.reset());
        NeoForge.EVENT_BUS.addListener(JetpackHudRenderer::onRenderHud);
        modEventBus.addListener(JetlytraClient::onRegisterRenderers);
        modContainer.registerExtensionPoint(
                IConfigScreenFactory.class,
                (mc, parent) -> new JetlytraConfigScreen(parent)
        );
    }

    @SubscribeEvent
    public static void onRegisterTooltipFactories(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(ElytraTooltipData.class, ElytraClientTooltipComponent::new);
    }

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(JetlytraBlocks.JETPACK_BE.get(), JetpackBlockEntityRenderer::new);
    }

    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        JetpackKeyMappings.register(event);
    }

}
