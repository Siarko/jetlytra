package pl.siarko.jetlytra.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import pl.siarko.jetlytra.Jetlytra;
import pl.siarko.jetlytra.capability.JetpackCapabilityAttacher;
import pl.siarko.jetlytra.capability.JetpackCapabilityImpl;
import pl.siarko.jetlytra.flight.FlightState;

public record C2SCrouchPacket(boolean active) implements CustomPacketPayload {

    public static final Type<C2SCrouchPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Jetlytra.MODID, "crouch"));

    public static final StreamCodec<FriendlyByteBuf, C2SCrouchPacket> CODEC = StreamCodec.of(
            (buf, packet) -> buf.writeBoolean(packet.active),
            buf -> new C2SCrouchPacket(buf.readBoolean())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(C2SCrouchPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
            JetpackCapabilityImpl cap = JetpackCapabilityAttacher.get(player);

            FlightState current = cap.getState();
            FlightState next = current;

            if (packet.active && current == FlightState.JETPACK) {
                next = FlightState.HOVERING;
            } else if (packet.active && current == FlightState.ELYTRA) {
                next = FlightState.HOVERING;
            } else if (!packet.active && current == FlightState.HOVERING) {
                next = FlightState.JETPACK;
            }

            if (next != current) {
                cap.setState(next);
                PacketDistributor.sendToPlayer(player, new S2CSyncStatePacket(next));
            }
        });
    }
}
