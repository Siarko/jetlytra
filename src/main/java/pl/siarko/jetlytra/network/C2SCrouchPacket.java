package pl.siarko.jetlytra.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import pl.siarko.jetlytra.Jetlytra;
import pl.siarko.jetlytra.JetlytraAttachments;
import pl.siarko.jetlytra.flight.FlightState;
import pl.siarko.jetlytra.item.JetlytraItems;

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
            ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
            boolean jetpackEnabled = Boolean.TRUE.equals(chest.get(JetlytraItems.JETPACK_ENABLED));

            FlightState current = player.getData(JetlytraAttachments.FLIGHT_STATE.get());
            FlightState next = current;
            if (packet.active) {
                if (current == FlightState.ELYTRA && player.isInWater()) {
                    // Exit elytra while underwater
                    FlightState exitTo = jetpackEnabled ? FlightState.JETPACK : FlightState.OFF;
                    player.setData(JetlytraAttachments.THRUST_ACTIVE.get(), false);
                    player.setData(JetlytraAttachments.FUEL_TICK_COUNTER.get(), 0);
                    player.setNoGravity(false);
                    player.setData(JetlytraAttachments.FLIGHT_STATE.get(), exitTo);
                    PacketDistributor.sendToPlayer(player, new S2CSyncStatePacket(exitTo));
                    return;
                }
                if (!player.isInWater()
                        && (current == FlightState.JETPACK || (current == FlightState.ELYTRA && jetpackEnabled))) {
                    next = FlightState.HOVERING;
                }
            } else if (!packet.active && current == FlightState.HOVERING) {
                next = FlightState.JETPACK;
            }

            if (next != current) {
                player.setData(JetlytraAttachments.FLIGHT_STATE.get(), next);
                PacketDistributor.sendToPlayer(player, new S2CSyncStatePacket(next));
            }
        });
    }
}
