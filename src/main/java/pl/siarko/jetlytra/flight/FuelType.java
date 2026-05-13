package pl.siarko.jetlytra.flight;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.Locale;
import java.util.Optional;
import java.util.function.Supplier;

public enum FuelType {
    BLAZE_ROD("Blaze Rod", () -> Items.BLAZE_ROD, 20, 1.0f,
            ParticleTypes.FLAME, ParticleTypes.CAMPFIRE_COSY_SMOKE, ParticleTypes.FLAME),
    BREEZE_ROD("Breeze Rod", () -> Items.BREEZE_ROD, 20, 1.3f,
            ParticleTypes.SMALL_GUST, ParticleTypes.WHITE_SMOKE, ParticleTypes.GUST);

    public static final Codec<FuelType> CODEC = Codec.STRING.xmap(
            s -> FuelType.valueOf(s.toUpperCase(Locale.ROOT)),
            t -> t.name().toLowerCase(Locale.ROOT)
    );

    public static final StreamCodec<ByteBuf, FuelType> STREAM_CODEC =
            ByteBufCodecs.VAR_INT.map(i -> FuelType.values()[i], Enum::ordinal);

    public final String displayName;
    private final Supplier<Item> itemSupplier;
    public final int ticksPerUnit;
    public final float accelerationMultiplier;
    public final SimpleParticleType exhaustParticle;
    public final SimpleParticleType trailParticle;
    public final SimpleParticleType boostParticle;

    FuelType(String displayName, Supplier<Item> itemSupplier, int ticksPerUnit, float accelerationMultiplier,
             SimpleParticleType exhaustParticle, SimpleParticleType trailParticle, SimpleParticleType boostParticle) {
        this.displayName = displayName;
        this.itemSupplier = itemSupplier;
        this.ticksPerUnit = ticksPerUnit;
        this.accelerationMultiplier = accelerationMultiplier;
        this.exhaustParticle = exhaustParticle;
        this.trailParticle = trailParticle;
        this.boostParticle = boostParticle;
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
