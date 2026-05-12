package pl.siarko.jetlytra.flight;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import pl.siarko.jetlytra.JetlytraAttachments;
import pl.siarko.jetlytra.item.JetlytraItem;
import pl.siarko.jetlytra.item.JetlytraItems;
import pl.siarko.jetlytra.item.StoredElytra;


public class JetpackPhysicsHandler {

    public static void onPlayerTick(PlayerTickEvent.Pre event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.level().isClientSide()) return;

        FlightState state = player.getData(JetlytraAttachments.FLIGHT_STATE.get());

        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        if (!(chest.getItem() instanceof JetlytraItem)) {
            reset(player);
            return;
        }

        if (state != FlightState.JETPACK && player.onGround()) {
            reset(player);
            return;
        }

        player.setNoGravity(state == FlightState.HOVERING);
        // Suppress fall damage only when actively thrusting or hovering
        boolean thrusting = player.getData(JetlytraAttachments.THRUST_ACTIVE.get());
        if (state == FlightState.HOVERING || (state == FlightState.JETPACK && thrusting)) {
            player.resetFallDistance();
        }

        if (thrusting && !drainFuel(player)) {
            reset(player);
        }

        if (state == FlightState.ELYTRA && player.tickCount % 20 == 0) {
            applyElytraDamage(player, chest);
        }
    }

    private static void applyElytraDamage(ServerPlayer player, ItemStack jetlytraStack) {
        StoredElytra stored = jetlytraStack.get(JetlytraItems.ELYTRA_ITEM);
        if (stored == null || stored.isEmpty()) return;

        ItemStack elytra = stored.stack().copy();
        if (!elytra.isDamageableItem()) return;

        int damage = EnchantmentHelper.processDurabilityChange(player.serverLevel(), elytra, 1);
        if (damage > 0) {
            int newDamage = Math.min(elytra.getDamageValue() + damage, elytra.getMaxDamage());
            elytra.setDamageValue(newDamage);
            jetlytraStack.set(JetlytraItems.ELYTRA_ITEM, new StoredElytra(elytra));
            if (newDamage >= elytra.getMaxDamage()) {
                reset(player);
            }
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

    private static void reset(ServerPlayer player) {
        player.setData(JetlytraAttachments.FLIGHT_STATE.get(), FlightState.JETPACK);
        player.setData(JetlytraAttachments.THRUST_ACTIVE.get(), false);
        player.setData(JetlytraAttachments.FUEL_TICK_COUNTER.get(), 0);
        player.setNoGravity(false);
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        if (chest.getItem() instanceof JetlytraItem) {
            chest.set(JetlytraItems.FLIGHT_STATE_COMPONENT, FlightState.JETPACK);
            chest.set(JetlytraItems.THRUST_ACTIVE_COMPONENT, false);
        }
    }
}
