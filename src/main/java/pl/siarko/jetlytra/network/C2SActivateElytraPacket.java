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
import pl.siarko.jetlytra.item.JetlytraItem;
import pl.siarko.jetlytra.item.JetlytraItems;
import pl.siarko.jetlytra.item.StoredElytra;

public record C2SActivateElytraPacket() implements CustomPacketPayload {

    public static final Type<C2SActivateElytraPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Jetlytra.MODID, "activate_elytra"));

    public static final StreamCodec<FriendlyByteBuf, C2SActivateElytraPacket> CODEC = StreamCodec.of(
            (buf, packet) -> {},
            buf -> new C2SActivateElytraPacket()
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(C2SActivateElytraPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
            ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
            if (!(chest.getItem() instanceof JetlytraItem)) return;

            StoredElytra elytra = chest.get(JetlytraItems.ELYTRA_ITEM);
            if (elytra == null || elytra.isEmpty()) return;

            if (player.onGround()) return;

            FlightState current = player.getData(JetlytraAttachments.FLIGHT_STATE.get());
            if (current == FlightState.ELYTRA) return;

            player.setNoGravity(false);
            player.setData(JetlytraAttachments.FLIGHT_STATE.get(), FlightState.ELYTRA);
            PacketDistributor.sendToPlayer(player, new S2CSyncStatePacket(FlightState.ELYTRA));
        });
    }
}
