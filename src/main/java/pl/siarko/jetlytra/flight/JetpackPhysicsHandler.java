package pl.siarko.jetlytra.flight;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import pl.siarko.jetlytra.JetlytraAttachments;
import pl.siarko.jetlytra.JetlytraSlotHelper;
import pl.siarko.jetlytra.item.JetlytraItemBase;
import pl.siarko.jetlytra.item.JetlytraItems;
import pl.siarko.jetlytra.item.StoredElytra;
import pl.siarko.jetlytra.network.S2CFlightStateSyncPacket;
import pl.siarko.jetlytra.server.particle.JetpackServerParticleHandler;


public class JetpackPhysicsHandler {

    public static void onPlayerTick(PlayerTickEvent.Pre event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.level().isClientSide()) return;

        ItemStack jetpackStack = JetlytraSlotHelper.getWornJetlytra(player);
        if (!(jetpackStack.getItem() instanceof JetlytraItemBase)) {
            reset(player, jetpackStack);
            return;
        }

        FlightState state = player.getData(JetlytraAttachments.FLIGHT_STATE);

        if (state != FlightState.JETPACK && player.onGround()) {
            reset(player, jetpackStack);
            return;
        }

        player.setNoGravity(state == FlightState.HOVERING);
        boolean thrusting = player.getData(JetlytraAttachments.THRUST_ACTIVE);
        if (state == FlightState.HOVERING || (state == FlightState.JETPACK && thrusting)) {
            player.resetFallDistance();
        }

        if (thrusting) {
            FuelData fuel = jetpackStack.get(JetlytraItems.FUEL_DATA);
            FuelTypeDefinition fuelDef = fuel != null ? fuel.getDefinition().orElse(null) : null;
            JetpackServerParticleHandler.spawnExhaustParticles(player, fuelDef, state);
            if (!drainFuel(player, jetpackStack)) {
                reset(player, jetpackStack);
            }
        }

        if (state == FlightState.ELYTRA && player.tickCount % 20 == 0 && player.getDeltaMovement().lengthSqr() > 0.01) {
            applyElytraDamage(player, jetpackStack);
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

    private static boolean drainFuel(ServerPlayer player, ItemStack stack) {
        FuelData fuel = stack.get(JetlytraItems.FUEL_DATA);
        if (fuel == null || fuel.count() <= 0) return false;

        if (fuel.getDefinition().isEmpty()) {
            stack.remove(JetlytraItems.FUEL_DATA);
            JetlytraItemBase.syncFuelDamage(stack);
            return false;
        }

        int counter = player.getData(JetlytraAttachments.FUEL_TICK) + 1;
        if (counter < getTargetTicksPerUnit(player, fuel)) {
            player.setData(JetlytraAttachments.FUEL_TICK, counter);
            return true;
        }

        // Full unit elapsed — drain one unit
        player.setData(JetlytraAttachments.FUEL_TICK, 0);
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

    static void reset(ServerPlayer player, ItemStack stack) {
        player.setNoGravity(false);
        player.setData(JetlytraAttachments.FLIGHT_STATE, FlightState.JETPACK);
        player.setData(JetlytraAttachments.THRUST_ACTIVE, false);
        player.setData(JetlytraAttachments.FUEL_TICK, 0);
        if (stack.getItem() instanceof JetlytraItemBase) {
            stack.set(JetlytraItems.FLIGHT_STATE_COMPONENT, FlightState.JETPACK);
            stack.set(JetlytraItems.THRUST_ACTIVE_COMPONENT, false);
        }
        PacketDistributor.sendToPlayer(player, new S2CFlightStateSyncPacket(FlightState.JETPACK));
    }

    private static int getTargetTicksPerUnit(ServerPlayer player, FuelData fuel) {
        FlightState state = player.getData(JetlytraAttachments.FLIGHT_STATE);
        int ticksPerUnit = fuel.getDefinition()
                .map(FuelTypeDefinition::ticksPerUnit)
                .orElse(20);
        int ticksPerUnitHover = fuel.getDefinition()
                .map(FuelTypeDefinition::ticksPerUnitHover)
                .orElse(ticksPerUnit);
        return (state == FlightState.HOVERING ? ticksPerUnitHover : ticksPerUnit);
    }
}
