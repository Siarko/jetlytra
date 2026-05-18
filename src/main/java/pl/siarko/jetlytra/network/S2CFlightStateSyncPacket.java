package pl.siarko.jetlytra.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import pl.siarko.jetlytra.Jetlytra;
import pl.siarko.jetlytra.client.ClientJetpackState;
import pl.siarko.jetlytra.flight.FlightState;

public record S2CFlightStateSyncPacket(FlightState state) implements CustomPacketPayload {

    public static final Type<S2CFlightStateSyncPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Jetlytra.MODID, "flight_state_sync"));

    public static final StreamCodec<FriendlyByteBuf, S2CFlightStateSyncPacket> CODEC = StreamCodec.of(
            (buf, packet) -> FlightState.STREAM_CODEC.encode(buf, packet.state),
            buf -> new S2CFlightStateSyncPacket(FlightState.STREAM_CODEC.decode(buf))
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(S2CFlightStateSyncPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> ClientJetpackState.setState(packet.state()));
    }
}
