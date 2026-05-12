package pl.siarko.jetlytra.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
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
            boolean hasFuel = chest.has(JetlytraItems.FUEL_DATA);
            boolean jetpackAvailable = jetpackEnabled && hasFuel;
            FlightState current = player.getData(JetlytraAttachments.FLIGHT_STATE.get());

            FlightState nextState = null;
            if(packet.active) {
                if (current.equals(FlightState.ELYTRA)) {
                    if (player.isInWater()) {
                        nextState = FlightState.JETPACK;
                    } else if (jetpackAvailable) {
                        nextState = FlightState.HOVERING;
                    }
                } else if (jetpackAvailable && !player.isInWater() && !player.onGround()) {
                    nextState = FlightState.HOVERING;
                }
            }else{
                if (current.equals(FlightState.HOVERING)) {
                    nextState = FlightState.JETPACK;
                }
            }

            if(nextState != null) {
                if(nextState.equals(FlightState.HOVERING)) {
                    player.setData(JetlytraAttachments.THRUST_ACTIVE.get(), true);
                    chest.set(JetlytraItems.THRUST_ACTIVE_COMPONENT, true);
                } else if (current.equals(FlightState.HOVERING)) {
                    player.setData(JetlytraAttachments.THRUST_ACTIVE.get(), false);
                    chest.set(JetlytraItems.THRUST_ACTIVE_COMPONENT, false);
                }
                player.setData(JetlytraAttachments.FLIGHT_STATE.get(), nextState);
                chest.set(JetlytraItems.FLIGHT_STATE_COMPONENT, nextState);
            }
        });
    }
}
