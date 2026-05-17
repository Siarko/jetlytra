package pl.siarko.jetlytra.flight;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import pl.siarko.jetlytra.item.JetlytraItemBase;
import pl.siarko.jetlytra.item.JetlytraItems;
import pl.siarko.jetlytra.item.StoredElytra;
import pl.siarko.jetlytra.server.particle.JetpackServerParticleHandler;


public class JetpackPhysicsHandler {

    public static void onPlayerTick(PlayerTickEvent.Pre event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.level().isClientSide()) return;

        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        if (!(chest.getItem() instanceof JetlytraItemBase)) {
            reset(player, chest);
            return;
        }

        FlightState state = chest.getOrDefault(JetlytraItems.FLIGHT_STATE_COMPONENT, FlightState.JETPACK);

        if (state != FlightState.JETPACK && player.onGround()) {
            reset(player, chest);
            return;
        }

        player.setNoGravity(state == FlightState.HOVERING);
        boolean thrusting = Boolean.TRUE.equals(chest.get(JetlytraItems.THRUST_ACTIVE_COMPONENT));
        if (state == FlightState.HOVERING || (state == FlightState.JETPACK && thrusting)) {
            player.resetFallDistance();
        }

        if (thrusting) {
            FuelData fuel = chest.get(JetlytraItems.FUEL_DATA);
            FuelTypeDefinition fuelDef = fuel != null ? fuel.getDefinition().orElse(null) : null;
            JetpackServerParticleHandler.spawnExhaustParticles(player, fuelDef, state);
            if (!drainFuel(chest)) {
                reset(player, chest);
            }
        }

        if (state == FlightState.ELYTRA && player.tickCount % 20 == 0 && player.getDeltaMovement().lengthSqr() > 0.01) {
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
                reset(player, jetlytraStack);
            }
        }
    }

    private static boolean drainFuel(ItemStack stack) {
        FuelData fuel = stack.get(JetlytraItems.FUEL_DATA);
        if (fuel == null || fuel.count() <= 0) return false;
        
        int counter = stack.getOrDefault(JetlytraItems.FUEL_TICK_COMPONENT, 0) + 1;
        if (counter < getTargetTicksPerUnit(stack, fuel)) {
            stack.set(JetlytraItems.FUEL_TICK_COMPONENT, counter);
            return true;
        }

        // Full unit elapsed — drain one unit
        stack.set(JetlytraItems.FUEL_TICK_COMPONENT, 0);
        int remaining = fuel.count() - 1;
        if (remaining <= 0) {
            stack.remove(JetlytraItems.FUEL_DATA);
            JetlytraItemBase.syncFuelDamage(stack);
            return false;
        }
        stack.set(JetlytraItems.FUEL_DATA, new FuelData(fuel.typeId(), remaining));
        JetlytraItemBase.syncFuelDamage(stack);
        return true;
    }

    static void reset(ServerPlayer player, ItemStack chest) {
        player.setNoGravity(false);
        if (chest.getItem() instanceof JetlytraItemBase) {
            chest.set(JetlytraItems.FLIGHT_STATE_COMPONENT, FlightState.JETPACK);
            chest.set(JetlytraItems.THRUST_ACTIVE_COMPONENT, false);
            chest.set(JetlytraItems.FUEL_TICK_COMPONENT, 0);
        }
    }

    private static int getTargetTicksPerUnit(ItemStack stack, FuelData fuel) {
        FlightState state = stack.getOrDefault(JetlytraItems.FLIGHT_STATE_COMPONENT, FlightState.JETPACK);
        int ticksPerUnit = fuel.getDefinition()
                .map(FuelTypeDefinition::ticksPerUnit)
                .orElse(20);
        int ticksPerUnitHover = fuel.getDefinition()
                .map(FuelTypeDefinition::ticksPerUnitHover)
                .orElse(ticksPerUnit);
        return (state == FlightState.HOVERING ? ticksPerUnitHover : ticksPerUnit);
    }
}
