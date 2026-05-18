package pl.siarko.jetlytra.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import pl.siarko.jetlytra.JetlytraAttachments;
import pl.siarko.jetlytra.JetlytraSlotHelper;
import net.neoforged.neoforge.network.PacketDistributor;
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
            ItemStack jetpackStack = JetlytraSlotHelper.getWornJetlytra(player);
            FlightState current = player.getData(JetlytraAttachments.FLIGHT_STATE);
            FlightState nextState = nextCrouchState(
                    packet.active, current,
                    JetlytraItems.isJetpackAvailable(jetpackStack),
                    player.isInWater(), player.onGround());

            if (nextState != null) {
                boolean nextThrust = (nextState == FlightState.HOVERING);
                player.setData(JetlytraAttachments.FLIGHT_STATE, nextState);
                player.setData(JetlytraAttachments.THRUST_ACTIVE, nextThrust);
                jetpackStack.set(JetlytraItems.FLIGHT_STATE_COMPONENT, nextState);
                jetpackStack.set(JetlytraItems.THRUST_ACTIVE_COMPONENT, nextThrust);
                PacketDistributor.sendToPlayer(player, new S2CFlightStateSyncPacket(nextState, nextThrust));
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
