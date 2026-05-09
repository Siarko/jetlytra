package pl.siarko.jetlytra;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import pl.siarko.jetlytra.flight.FlightState;
import pl.siarko.jetlytra.item.JetlytraItem;
import pl.siarko.jetlytra.network.S2CSyncStatePacket;

public class JetpackPlayerEvents {

    public static void onPlayerClone(PlayerEvent.Clone event) {
        Player clone = event.getEntity();
        ItemStack stack = clone.getItemBySlot(EquipmentSlot.CHEST);
        if (stack.getItem() instanceof JetlytraItem) {
            clone.setData(JetlytraAttachments.FLIGHT_STATE.get(), FlightState.JETPACK);
        }
    }

    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!(player.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof JetlytraItem)) return;
        player.setData(JetlytraAttachments.FLIGHT_STATE.get(), FlightState.JETPACK);
        PacketDistributor.sendToPlayer(player, new S2CSyncStatePacket(FlightState.JETPACK));
    }

    public static void onEquipmentChange(LivingEquipmentChangeEvent event) {
        if (event.getSlot() != EquipmentSlot.CHEST) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        boolean wasJetpack = event.getFrom().getItem() instanceof JetlytraItem;
        boolean isJetpack = event.getTo().getItem() instanceof JetlytraItem;

        // Only react to actual item type transitions, not damage/component changes on the same item
        if (wasJetpack == isJetpack) return;

        FlightState next = FlightState.JETPACK;

        player.setNoGravity(false);
        player.setData(JetlytraAttachments.FLIGHT_STATE.get(), next);
        player.setData(JetlytraAttachments.THRUST_ACTIVE.get(), false);
        PacketDistributor.sendToPlayer(player, new S2CSyncStatePacket(next));
    }
}
