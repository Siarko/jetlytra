package pl.siarko.jetlytra.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
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

            ItemStack stack = player.getItemBySlot(EquipmentSlot.CHEST);
            if (!(stack.getItem() instanceof JetlytraItem)) return;

            FlightState current = player.getData(JetlytraAttachments.FLIGHT_STATE.get());

            // While gliding, only toggle the enabled flag — don't interrupt flight
            if (current == FlightState.ELYTRA) {
                boolean nowEnabled = !Boolean.TRUE.equals(stack.get(JetlytraItems.JETPACK_ENABLED));
                stack.set(JetlytraItems.JETPACK_ENABLED, nowEnabled);
                if (!nowEnabled) {
                    player.setData(JetlytraAttachments.THRUST_ACTIVE.get(), false);
                    player.setData(JetlytraAttachments.FUEL_TICK_COUNTER.get(), 0);
                }
                player.displayClientMessage(
                    Component.translatable(nowEnabled ? "item.jetlytra.jetpack.enabled" : "item.jetlytra.jetpack.disabled")
                        .withStyle(nowEnabled ? ChatFormatting.GREEN : ChatFormatting.RED),
                    true
                );
                return;
            }

            FlightState next = current.isActive() ? FlightState.OFF : FlightState.JETPACK;
            player.setData(JetlytraAttachments.FLIGHT_STATE.get(), next);
            stack.set(JetlytraItems.JETPACK_ENABLED, next.isActive());

            if (next == FlightState.OFF) {
                player.setNoGravity(false);
                player.setData(JetlytraAttachments.THRUST_ACTIVE.get(), false);
                player.setData(JetlytraAttachments.FUEL_TICK_COUNTER.get(), 0);
            }

            PacketDistributor.sendToPlayer(player, new S2CSyncStatePacket(next, true));
        });
    }
}
