package pl.siarko.jetlytra.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import pl.siarko.jetlytra.Jetlytra;
import pl.siarko.jetlytra.client.ClientJetpackState;
import pl.siarko.jetlytra.flight.FlightState;

public record S2CSyncStatePacket(FlightState state, boolean showMessage) implements CustomPacketPayload {

    public S2CSyncStatePacket(FlightState state) {
        this(state, true);
    }

    public static final Type<S2CSyncStatePacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Jetlytra.MODID, "sync_state"));

    public static final StreamCodec<FriendlyByteBuf, S2CSyncStatePacket> CODEC = StreamCodec.of(
            (buf, packet) -> { buf.writeEnum(packet.state); buf.writeBoolean(packet.showMessage); },
            buf -> new S2CSyncStatePacket(buf.readEnum(FlightState.class), buf.readBoolean())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(S2CSyncStatePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> ClientJetpackState.setState(packet.state));
    }
}
