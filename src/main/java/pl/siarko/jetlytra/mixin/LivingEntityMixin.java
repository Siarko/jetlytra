package pl.siarko.jetlytra.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pl.siarko.jetlytra.flight.FlightState;
import pl.siarko.jetlytra.item.JetlytraItems;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {

    @Inject(method = "isFallFlying", at = @At("HEAD"), cancellable = true)
    private void jetlytra_elytraFlight(CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof ServerPlayer player) {
            FlightState state = player.getItemBySlot(EquipmentSlot.CHEST)
                    .getOrDefault(JetlytraItems.FLIGHT_STATE_COMPONENT, FlightState.JETPACK);
            if (state == FlightState.ELYTRA) {
                cir.setReturnValue(true);
            }
        }
    }
}
