package pl.siarko.jetlytra.flight;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import pl.siarko.jetlytra.capability.JetpackCapabilityAttacher;
import pl.siarko.jetlytra.capability.JetpackCapabilityImpl;
import pl.siarko.jetlytra.item.JetlytraItem;
import pl.siarko.jetlytra.network.S2CSyncStatePacket;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class JetpackPhysicsHandler {

    private static final Map<UUID, FuelSystem> FUEL_SYSTEMS = new HashMap<>();

    public static void onPlayerTick(PlayerTickEvent.Pre event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.level().isClientSide()) return;

        JetpackCapabilityImpl cap = JetpackCapabilityAttacher.get(player);
        FlightState state = cap.getState();

        if (state == FlightState.OFF) return;

        // Verify jetpack is still equipped
        if (!(player.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof JetlytraItem)) {
            transitionOff(player, cap);
            return;
        }

        // Drain fuel; turn off if empty
        FuelSystem fuel = FUEL_SYSTEMS.computeIfAbsent(player.getUUID(), uuid -> new FuelSystem());
        if (!fuel.tick(player)) {
            transitionOff(player, cap);
            return;
        }

        // Land from elytra or hover mode — transition back to jetpack
        if ((state == FlightState.ELYTRA || state == FlightState.HOVERING) && player.onGround()) {
            cap.setState(FlightState.JETPACK);
            player.setNoGravity(false);
            PacketDistributor.sendToPlayer(player, new S2CSyncStatePacket(FlightState.JETPACK));
            return;
        }

        // Disable gravity while hovering, restore otherwise
        player.setNoGravity(state == FlightState.HOVERING);

        // Suppress fall damage only when actively thrusting or hovering
        if (state == FlightState.HOVERING || (state == FlightState.JETPACK && cap.isThrustActive())) {
            player.resetFallDistance();
        }
    }

    private static void transitionOff(ServerPlayer player, JetpackCapabilityImpl cap) {
        cap.setState(FlightState.OFF);
        cap.setThrustActive(false);
        player.setNoGravity(false);
        PacketDistributor.sendToPlayer(player, new S2CSyncStatePacket(FlightState.OFF));
    }
}
