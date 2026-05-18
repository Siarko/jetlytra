package pl.siarko.jetlytra.compat.curios;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;

public class JetlytraCurioCapability implements ICurio {

    private final ItemStack stack;

    public JetlytraCurioCapability(ItemStack stack) {
        this.stack = stack;
    }

    @Override
    public ItemStack getStack() {
        return stack;
    }

    @Override
    public void onEquip(SlotContext ctx, ItemStack prevStack) {
    }

    @Override
    public void onUnequip(SlotContext ctx, ItemStack newStack) {
        if (ctx.entity() instanceof ServerPlayer player) {
            player.setNoGravity(false);
        }
    }
}
