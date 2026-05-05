package pl.siarko.jetlytra.capability;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import pl.siarko.jetlytra.Jetlytra;
import pl.siarko.jetlytra.flight.FlightState;
import pl.siarko.jetlytra.item.JetlytraItem;
import pl.siarko.jetlytra.item.JetlytraItems;
import pl.siarko.jetlytra.network.S2CSyncStatePacket;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class JetpackCapabilityAttacher {

    // Simple map-based storage since NeoForge 1.21.1 capability attachment for non-block entities
    // uses a different pattern than older versions
    private static final Map<UUID, JetpackCapabilityImpl> CAPABILITIES = new ConcurrentHashMap<>();

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(Jetlytra.MODID, "jetpack");

    public static JetpackCapabilityImpl get(Player player) {
        return CAPABILITIES.computeIfAbsent(player.getUUID(), uuid -> new JetpackCapabilityImpl());
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        Player clone = event.getEntity();
        ItemStack stack = clone.getItemBySlot(EquipmentSlot.CHEST);
        if (stack.getItem() instanceof JetlytraItem
                && Boolean.TRUE.equals(stack.get(JetlytraItems.JETPACK_ENABLED))) {
            get(clone).setState(FlightState.JETPACK);
        }
        // Otherwise fresh capability defaults to OFF — no action needed
    }

    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        ItemStack stack = player.getItemBySlot(EquipmentSlot.CHEST);
        FlightState restored = FlightState.OFF;
        if (stack.getItem() instanceof JetlytraItem
                && Boolean.TRUE.equals(stack.get(JetlytraItems.JETPACK_ENABLED))) {
            restored = FlightState.JETPACK;
        }
        get(player).setState(restored);
        PacketDistributor.sendToPlayer(player, new S2CSyncStatePacket(restored));
    }

    @SubscribeEvent
    public static void onEquipmentChange(LivingEquipmentChangeEvent event) {
        if (event.getSlot() != EquipmentSlot.CHEST) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        boolean wasJetpack = event.getFrom().getItem() instanceof JetlytraItem;
        boolean isJetpack = event.getTo().getItem() instanceof JetlytraItem;

        // Only react to actual item type transitions, not damage/component changes on the same item
        if (wasJetpack == isJetpack) return;

        FlightState next;
        if (isJetpack && Boolean.TRUE.equals(event.getTo().get(JetlytraItems.JETPACK_ENABLED))) {
            next = FlightState.JETPACK;
        } else {
            next = FlightState.OFF;
            player.setNoGravity(false);
        }
        get(player).setState(next);
        get(player).setThrustActive(false);
        PacketDistributor.sendToPlayer(player, new S2CSyncStatePacket(next));
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        // Clean up when player leaves to avoid memory leaks
        CAPABILITIES.remove(event.getEntity().getUUID());
    }
}
