package pl.siarko.jetlytra.mixin;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pl.siarko.jetlytra.flight.FlightState;
import pl.siarko.jetlytra.item.JetlytraItems;

@Mixin(LocalPlayer.class)
public class LocalPlayerMixin {

    @Inject(method = "isMovingSlowly", at = @At("HEAD"), cancellable = true)
    private void jetlytra_cancelSneakSpeedWhenHovering(CallbackInfoReturnable<Boolean> cir) {
        LocalPlayer self = (LocalPlayer) (Object) this;
        ItemStack chest = self.getItemBySlot(EquipmentSlot.CHEST);
        if (chest.getOrDefault(JetlytraItems.FLIGHT_STATE_COMPONENT, FlightState.JETPACK) == FlightState.HOVERING) {
            cir.setReturnValue(false);
        }
    }

}
