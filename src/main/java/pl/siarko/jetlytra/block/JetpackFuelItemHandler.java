package pl.siarko.jetlytra.block;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import pl.siarko.jetlytra.flight.FuelData;
import pl.siarko.jetlytra.flight.FuelTypeRegistry;

public class JetpackFuelItemHandler implements IItemHandler {

    private final JetpackBlockEntity be;

    public JetpackFuelItemHandler(JetpackBlockEntity be) {
        this.be = be;
    }

    @Override
    public int getSlots() { return 1; }

    @Override
    public @NotNull ItemStack getStackInSlot(int slot) {
        FuelData fuel = be.getFuelData();
        if (fuel == null) return ItemStack.EMPTY;
        Item item = fuel.getDefinition().map(d -> d.item()).orElse(null);
        if (item == null) return ItemStack.EMPTY;
        return new ItemStack(item, fuel.count());
    }

    @Override
    public @NotNull ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (stack.isEmpty()) return ItemStack.EMPTY;
        var typeId = FuelTypeRegistry.idFromItem(stack.getItem());
        if (typeId.isEmpty()) return stack;

        FuelData current = be.getFuelData();
        int currentCount = current != null ? current.count() : 0;
        if (current != null && !current.typeId().equals(typeId.get())) return stack;

        int canAdd = FuelData.MAX_COUNT - currentCount;
        if (canAdd <= 0) return stack;

        int toAdd = Math.min(stack.getCount(), canAdd);
        if (!simulate) be.setFuelData(new FuelData(typeId.get(), currentCount + toAdd));

        if (toAdd >= stack.getCount()) return ItemStack.EMPTY;
        ItemStack remainder = stack.copy();
        remainder.setCount(stack.getCount() - toAdd);
        return remainder;
    }

    @Override
    public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
        return ItemStack.EMPTY;
    }

    @Override
    public int getSlotLimit(int slot) { return FuelData.MAX_COUNT; }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return FuelTypeRegistry.idFromItem(stack.getItem()).isPresent();
    }
}
