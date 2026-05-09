package pl.siarko.jetlytra.item;

import com.mojang.serialization.Codec;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;

public record StoredElytra(ItemStack stack) {

    public static final Codec<StoredElytra> CODEC =
            ItemStack.CODEC.xmap(StoredElytra::new, StoredElytra::stack);

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof StoredElytra(ItemStack stack1))) return false;
        return ItemStack.isSameItemSameComponents(this.stack, stack1)
                && this.stack.getCount() == stack1.getCount();
    }

    @Override
    public int hashCode() {
        return Objects.hash(stack.getItem(), stack.getCount(), stack.getComponentsPatch());
    }

    public boolean isEmpty() {
        return stack.isEmpty();
    }
}
