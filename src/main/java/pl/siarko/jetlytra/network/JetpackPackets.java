package pl.siarko.jetlytra.network;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class JetpackPackets {

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1.0");
        registrar.playToServer(C2SToggleJetpackPacket.TYPE, C2SToggleJetpackPacket.CODEC, C2SToggleJetpackPacket::handle);
        registrar.playToServer(C2SThrustPacket.TYPE, C2SThrustPacket.CODEC, C2SThrustPacket::handle);
        registrar.playToServer(C2SHoverPacket.TYPE, C2SHoverPacket.CODEC, C2SHoverPacket::handle);
        registrar.playToServer(C2SToggleElytraPacket.TYPE, C2SToggleElytraPacket.CODEC, C2SToggleElytraPacket::handle);
        registrar.playToClient(S2CFuelTypeSyncPacket.TYPE, S2CFuelTypeSyncPacket.CODEC, S2CFuelTypeSyncPacket::handle);
        registrar.playToClient(S2CFlightStateSyncPacket.TYPE, S2CFlightStateSyncPacket.CODEC, S2CFlightStateSyncPacket::handle);
    }
}
