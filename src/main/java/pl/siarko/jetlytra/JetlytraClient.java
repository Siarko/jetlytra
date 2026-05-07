package pl.siarko.jetlytra;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;
import pl.siarko.jetlytra.client.ClientJetpackState;
import pl.siarko.jetlytra.client.input.JetpackInputHandler;
import pl.siarko.jetlytra.client.input.JetpackKeyMappings;
import pl.siarko.jetlytra.client.particle.JetpackParticleHandler;
import pl.siarko.jetlytra.flight.FlightState;

@Mod(value = Jetlytra.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = Jetlytra.MODID, value = Dist.CLIENT)
public class JetlytraClient {

    public JetlytraClient(IEventBus modEventBus) {
        NeoForge.EVENT_BUS.addListener(JetpackInputHandler::onClientTick);
        NeoForge.EVENT_BUS.addListener(JetpackParticleHandler::onClientTick);
        NeoForge.EVENT_BUS.addListener(JetlytraClient::onLogout);
    }

    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        JetpackKeyMappings.register(event);
    }

    private static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        ClientJetpackState.setState(FlightState.OFF);
    }
}
