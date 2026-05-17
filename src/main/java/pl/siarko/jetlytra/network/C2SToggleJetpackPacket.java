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
import net.neoforged.neoforge.network.handling.IPayloadContext;
import pl.siarko.jetlytra.Jetlytra;
import pl.siarko.jetlytra.flight.FlightState;
import pl.siarko.jetlytra.item.JetlytraItemBase;
import pl.siarko.jetlytra.item.JetlytraItems;

public record C2SToggleJetpackPacket() implements CustomPacketPayload {

    private static final String LABEL_JETPACK_ENABLED = "message.jetlytra.jetpack.enabled";
    private static final String LABEL_JETPACK_DISABLED = "message.jetlytra.jetpack.disabled";

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
            if (!(stack.getItem() instanceof JetlytraItemBase)) return;

            boolean newState = !Boolean.TRUE.equals(stack.get(JetlytraItems.JETPACK_ENABLED));
            stack.set(JetlytraItems.JETPACK_ENABLED, newState);
            if (!newState) {
                player.setNoGravity(false);
                stack.set(JetlytraItems.FLIGHT_STATE_COMPONENT, FlightState.JETPACK);
                stack.set(JetlytraItems.THRUST_ACTIVE_COMPONENT, false);
                stack.set(JetlytraItems.FUEL_TICK_COMPONENT, 0);
            }
            player.displayClientMessage(
                Component.translatable(newState ? LABEL_JETPACK_ENABLED : LABEL_JETPACK_DISABLED).withStyle(
                        newState ? ChatFormatting.GREEN : ChatFormatting.RED
                ),
                true
            );
        });
    }
}
