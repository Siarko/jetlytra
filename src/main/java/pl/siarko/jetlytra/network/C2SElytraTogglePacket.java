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

public record C2SElytraTogglePacket() implements CustomPacketPayload {

    public static final Type<C2SElytraTogglePacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Jetlytra.MODID, "elytra_toggle"));

    public static final StreamCodec<FriendlyByteBuf, C2SElytraTogglePacket> CODEC = StreamCodec.of(
            (buf, packet) -> {},
            buf -> new C2SElytraTogglePacket()
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(C2SElytraTogglePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
            JetpackCapabilityImpl cap = JetpackCapabilityAttacher.get(player);
            FlightState current = cap.getState();

            FlightState next = switch (current) {
                case JETPACK, HOVERING -> FlightState.ELYTRA;
                case ELYTRA -> FlightState.JETPACK;
                default -> current; // OFF — ignore
            };

            if (next != current) {
                cap.setState(next);
                PacketDistributor.sendToPlayer(player, new S2CSyncStatePacket(next));
            }
        });
    }
}
