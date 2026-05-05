package pl.siarko.jetlytra.network;

import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import pl.siarko.jetlytra.Jetlytra;
import pl.siarko.jetlytra.client.ClientJetpackState;
import pl.siarko.jetlytra.flight.FlightState;

public record S2CSyncStatePacket(FlightState state) implements CustomPacketPayload {

    public static final Type<S2CSyncStatePacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Jetlytra.MODID, "sync_state"));

    public static final StreamCodec<FriendlyByteBuf, S2CSyncStatePacket> CODEC = StreamCodec.of(
            (buf, packet) -> buf.writeEnum(packet.state),
            buf -> new S2CSyncStatePacket(buf.readEnum(FlightState.class))
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(S2CSyncStatePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            FlightState previous = ClientJetpackState.getState();
            ClientJetpackState.setState(packet.state);

            Component message = switch (packet.state) {
                case OFF -> Component.literal("Jetpack OFF").withStyle(ChatFormatting.RED);
                case JETPACK -> previous == FlightState.OFF
                        ? Component.literal("Jetpack ON").withStyle(ChatFormatting.GREEN)
                        : null; // returning from hover — no message
                default -> null; // HOVERING, ELYTRA — no message
            };

            if (message != null) {
                context.player().displayClientMessage(message, true);
            }
        });
    }
}
