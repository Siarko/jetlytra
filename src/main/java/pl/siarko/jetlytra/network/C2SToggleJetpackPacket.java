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
import pl.siarko.jetlytra.item.JetlytraItems;

public record C2SToggleJetpackPacket() implements CustomPacketPayload {

    public static final Type<C2SToggleJetpackPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Jetlytra.MODID, "toggle_jetpack"));

    public static final StreamCodec<FriendlyByteBuf, C2SToggleJetpackPacket> CODEC = StreamCodec.of(
            (buf, packet) -> {},
            buf -> new C2SToggleJetpackPacket()
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(C2SToggleJetpackPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();

            // Must be wearing the jetpack to toggle
            if (!(player.getInventory().armor.get(2).getItem() instanceof pl.siarko.jetlytra.item.JetlytraItem)) {
                return;
            }

            JetpackCapabilityImpl cap = JetpackCapabilityAttacher.get(player);
            FlightState next = cap.getState().isActive() ? FlightState.OFF : FlightState.JETPACK;
            cap.setState(next);

            if (next == FlightState.OFF) {
                player.setNoGravity(false);
            }

            PacketDistributor.sendToPlayer(player, new S2CSyncStatePacket(next));
        });
    }
}
