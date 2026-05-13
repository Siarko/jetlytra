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
            FlightState current = chest.getOrDefault(JetlytraItems.FLIGHT_STATE_COMPONENT, FlightState.JETPACK);
            FlightState nextState = nextCrouchState(
                    packet.active, current,
                    JetlytraItems.isJetpackAvailable(chest),
                    player.isInWater(), player.onGround());

            if (nextState != null) {
                if (nextState == FlightState.HOVERING) {
                    chest.set(JetlytraItems.THRUST_ACTIVE_COMPONENT, true);
                } else if (current == FlightState.HOVERING) {
                    chest.set(JetlytraItems.THRUST_ACTIVE_COMPONENT, false);
                }
                chest.set(JetlytraItems.FLIGHT_STATE_COMPONENT, nextState);
            }
        });
    }

    private static FlightState nextCrouchState(boolean active, FlightState current,
                                               boolean jetpackAvailable, boolean inWater, boolean onGround) {
        if (!active) {
            return current == FlightState.HOVERING ? FlightState.JETPACK : null;
        }
        if (current == FlightState.ELYTRA) {
            if (inWater) return FlightState.JETPACK;
            return jetpackAvailable ? FlightState.HOVERING : null;
        }
        return jetpackAvailable && !inWater && !onGround ? FlightState.HOVERING : null;
    }
}
