package pl.siarko.jetlytra.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import pl.siarko.jetlytra.JetlytraAttachments;
import pl.siarko.jetlytra.JetlytraSlotHelper;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import pl.siarko.jetlytra.Jetlytra;
import pl.siarko.jetlytra.flight.FlightState;
import pl.siarko.jetlytra.item.JetlytraItems;

public record C2SThrustPacket(boolean active) implements CustomPacketPayload {

    public static final Type<C2SThrustPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Jetlytra.MODID, "thrust"));

    public static final StreamCodec<FriendlyByteBuf, C2SThrustPacket> CODEC = StreamCodec.of(
            (buf, packet) -> buf.writeBoolean(packet.active),
            buf -> new C2SThrustPacket(buf.readBoolean())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(C2SThrustPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
            ItemStack itemStack = JetlytraSlotHelper.getWornJetlytra(player);
            if (JetlytraItems.isJetpackAvailable(itemStack)) {
                boolean hovering = player.getData(JetlytraAttachments.FLIGHT_STATE) == FlightState.HOVERING;
                player.setData(JetlytraAttachments.THRUST_ACTIVE, packet.active());
                itemStack.set(JetlytraItems.THRUST_ACTIVE_COMPONENT, packet.active() || hovering);
            }
        });
    }
}
