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
import pl.siarko.jetlytra.item.JetlytraItem;
import pl.siarko.jetlytra.item.JetlytraItems;
import pl.siarko.jetlytra.item.StoredElytra;

public record C2SToggleElytraPacket(ToggleType toggleType) implements CustomPacketPayload {

    public static final Type<C2SToggleElytraPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Jetlytra.MODID, "elytra_toggle"));

    public static final StreamCodec<FriendlyByteBuf, C2SToggleElytraPacket> CODEC = StreamCodec.of(
            (buf, packet) -> buf.writeVarInt(packet.toggleType.ordinal()),
            buf -> new C2SToggleElytraPacket(ToggleType.values()[buf.readVarInt()])
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(C2SToggleElytraPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
            ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
            if (!(chest.getItem() instanceof JetlytraItem)) return;

            StoredElytra elytra = chest.get(JetlytraItems.ELYTRA_ITEM);
            boolean hasElytra = elytra != null && !elytra.isEmpty()
                    && elytra.stack().getDamageValue() < elytra.stack().getMaxDamage();
            FlightState current = chest.getOrDefault(JetlytraItems.FLIGHT_STATE_COMPONENT, FlightState.JETPACK);

            FlightState next = switch (packet.toggleType) {
                case TOGGLE -> !current.equals(FlightState.ELYTRA) && hasElytra ? FlightState.ELYTRA : FlightState.JETPACK;
                case DISABLE -> FlightState.JETPACK;
                case ENABLE -> hasElytra ? FlightState.ELYTRA : current;
            };

            if (next != current) {
                player.setNoGravity(false);
                chest.set(JetlytraItems.FLIGHT_STATE_COMPONENT, next);
                chest.set(JetlytraItems.THRUST_ACTIVE_COMPONENT, false);
            }
        });
    }
}
