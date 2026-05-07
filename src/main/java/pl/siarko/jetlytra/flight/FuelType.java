package pl.siarko.jetlytra.flight;

import com.mojang.serialization.Codec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.Locale;
import java.util.Optional;
import java.util.function.Supplier;

public enum FuelType {
    BLAZE_ROD("Blaze Rod", () -> Items.BLAZE_ROD, 20),
    BREEZE_ROD("Breeze Rod", () -> Items.BREEZE_ROD, 20);

    public static final Codec<FuelType> CODEC = Codec.STRING.xmap(
            s -> FuelType.valueOf(s.toUpperCase(Locale.ROOT)),
            t -> t.name().toLowerCase(Locale.ROOT)
    );

    public final String displayName;
    private final Supplier<Item> itemSupplier;
    public final int ticksPerUnit;

    FuelType(String displayName, Supplier<Item> itemSupplier, int ticksPerUnit) {
        this.displayName = displayName;
        this.itemSupplier = itemSupplier;
        this.ticksPerUnit = ticksPerUnit;
    }

    public Item getItem() {
        return itemSupplier.get();
    }

    public static Optional<FuelType> fromItem(Item item) {
        for (FuelType type : values()) {
            if (type.getItem() == item) return Optional.of(type);
        }
        return Optional.empty();
    }
}
