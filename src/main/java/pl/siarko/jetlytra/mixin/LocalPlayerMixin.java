package pl.siarko.jetlytra.mixin;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pl.siarko.jetlytra.client.ClientJetpackState;
import pl.siarko.jetlytra.flight.FlightState;

@Mixin(LocalPlayer.class)
public class LocalPlayerMixin {

    @Inject(method = "isMovingSlowly", at = @At("HEAD"), cancellable = true)
    private void jetlytra_cancelSneakSpeedWhenHovering(CallbackInfoReturnable<Boolean> cir) {
        if (ClientJetpackState.getState() == FlightState.HOVERING) {
            cir.setReturnValue(false);
        }
    }

}
