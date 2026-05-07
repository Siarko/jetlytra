package pl.siarko.jetlytra.flight;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import pl.siarko.jetlytra.JetlytraAttachments;
import pl.siarko.jetlytra.item.JetlytraItem;
import pl.siarko.jetlytra.network.S2CSyncStatePacket;

public class JetpackPhysicsHandler {

    public static void onPlayerTick(PlayerTickEvent.Pre event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.level().isClientSide()) return;

        FlightState state = player.getData(JetlytraAttachments.FLIGHT_STATE.get());

        if (state == FlightState.OFF) return;

        // Verify jetpack is still equipped
        if (!(player.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof JetlytraItem)) {
            transitionOff(player);
            return;
        }

        if (state != FlightState.JETPACK && player.onGround()) {
            player.setData(JetlytraAttachments.FLIGHT_STATE.get(), FlightState.JETPACK);
            player.setNoGravity(false);
            PacketDistributor.sendToPlayer(player, new S2CSyncStatePacket(FlightState.JETPACK));
            return;
        }

        // Disable gravity while hovering, restore otherwise
        player.setNoGravity(state == FlightState.HOVERING);

        // Suppress fall damage only when actively thrusting or hovering
        boolean thrusting = player.getData(JetlytraAttachments.THRUST_ACTIVE.get());
        if (state == FlightState.HOVERING || (state == FlightState.JETPACK && thrusting)) {
            player.resetFallDistance();
        }
    }

    private static void transitionOff(ServerPlayer player) {
        player.setData(JetlytraAttachments.FLIGHT_STATE.get(), FlightState.OFF);
        player.setData(JetlytraAttachments.THRUST_ACTIVE.get(), false);
        player.setNoGravity(false);
        PacketDistributor.sendToPlayer(player, new S2CSyncStatePacket(FlightState.OFF));
    }
}
