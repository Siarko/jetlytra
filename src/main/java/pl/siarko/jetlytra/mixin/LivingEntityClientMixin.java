package pl.siarko.jetlytra.mixin;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pl.siarko.jetlytra.client.ClientJetpackState;
import pl.siarko.jetlytra.flight.FlightState;

@Mixin(LivingEntity.class)
public class LivingEntityClientMixin {

    @Inject(method = "isFallFlying", at = @At("HEAD"), cancellable = true)
    private void jetlytra_elytraFlight(CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof LocalPlayer) {
            if (ClientJetpackState.getState() == FlightState.ELYTRA) {
                cir.setReturnValue(true);
            }
        }
    }
}
