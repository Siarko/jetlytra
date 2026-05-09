package pl.siarko.jetlytra.flight;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import pl.siarko.jetlytra.JetlytraAttachments;
import pl.siarko.jetlytra.item.JetlytraItem;
import pl.siarko.jetlytra.item.JetlytraItems;
import pl.siarko.jetlytra.network.S2CSyncStatePacket;


public class JetpackPhysicsHandler {

    public static void onPlayerTick(PlayerTickEvent.Pre event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.level().isClientSide()) return;

        FlightState state = player.getData(JetlytraAttachments.FLIGHT_STATE.get());

        if (state == FlightState.OFF) return;

        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        if (!(chest.getItem() instanceof JetlytraItem)) {
            transitionOff(player);
            return;
        }

        boolean jetpackEnabled = Boolean.TRUE.equals(chest.get(JetlytraItems.JETPACK_ENABLED));

        // On landing, restore to JETPACK if enabled, otherwise cut to OFF
        if (state != FlightState.JETPACK && player.onGround()) {
            if (jetpackEnabled) {
                player.setData(JetlytraAttachments.FLIGHT_STATE.get(), FlightState.JETPACK);
                player.setNoGravity(false);
                PacketDistributor.sendToPlayer(player, new S2CSyncStatePacket(FlightState.JETPACK));
            } else {
                transitionOff(player);
            }
            return;
        }

        // Hovering in water breaks swimming/sinking — cancel it
        if (state == FlightState.HOVERING && player.isInWater()) {
            if (jetpackEnabled) {
                player.setData(JetlytraAttachments.FLIGHT_STATE.get(), FlightState.JETPACK);
                player.setNoGravity(false);
                PacketDistributor.sendToPlayer(player, new S2CSyncStatePacket(FlightState.JETPACK));
            } else {
                transitionOff(player);
            }
            return;
        }

        // Disable gravity while hovering, restore otherwise
        player.setNoGravity(state == FlightState.HOVERING);

        // Suppress fall damage only when actively thrusting or hovering
        boolean thrusting = player.getData(JetlytraAttachments.THRUST_ACTIVE.get());
        if (state == FlightState.HOVERING || (state == FlightState.JETPACK && thrusting)) {
            player.resetFallDistance();
        }

        // Fuel consumption: drain 1 unit per second; elytra boost also drains when jetpack is on
        boolean consuming = jetpackEnabled && (
                (state == FlightState.JETPACK && thrusting)
                || state == FlightState.HOVERING
                || (state == FlightState.ELYTRA && thrusting)
                || (state.isActive() && player.isSwimming())
        );

        if (consuming && !drainFuel(player)) {
            transitionOff(player);
        }
    }

    private static boolean drainFuel(ServerPlayer player) {
        ItemStack stack = player.getItemBySlot(EquipmentSlot.CHEST);
        FuelData fuel = stack.get(JetlytraItems.FUEL_DATA);
        if (fuel == null || fuel.count() <= 0) return false;

        int counter = player.getData(JetlytraAttachments.FUEL_TICK_COUNTER.get()) + 1;
        if (counter < 20) {
            player.setData(JetlytraAttachments.FUEL_TICK_COUNTER.get(), counter);
            return true;
        }

        // Full second elapsed — drain one unit
        player.setData(JetlytraAttachments.FUEL_TICK_COUNTER.get(), 0);
        int remaining = fuel.count() - 1;
        if (remaining <= 0) {
            stack.remove(JetlytraItems.FUEL_DATA);
            return false;
        }
        stack.set(JetlytraItems.FUEL_DATA, new FuelData(fuel.type(), remaining));
        return true;
    }

    private static void transitionOff(ServerPlayer player) {
        player.setData(JetlytraAttachments.FLIGHT_STATE.get(), FlightState.OFF);
        player.setData(JetlytraAttachments.THRUST_ACTIVE.get(), false);
        player.setData(JetlytraAttachments.FUEL_TICK_COUNTER.get(), 0);
        player.setNoGravity(false);
        PacketDistributor.sendToPlayer(player, new S2CSyncStatePacket(FlightState.OFF));
    }
}
