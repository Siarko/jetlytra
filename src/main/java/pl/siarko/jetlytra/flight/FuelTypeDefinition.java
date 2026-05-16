package pl.siarko.jetlytra.flight;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.logging.LogUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import org.slf4j.Logger;

public record FuelTypeDefinition(
        String displayName,
        Item item,
        int ticksPerUnit,
        float accelerationMultiplier,
        SimpleParticleType exhaustParticle,
        SimpleParticleType trailParticle,
        SimpleParticleType boostParticle
) {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final Codec<FuelTypeDefinition> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.STRING.fieldOf("display_name").forGetter(FuelTypeDefinition::displayName),
            BuiltInRegistries.ITEM.byNameCodec().fieldOf("item").forGetter(FuelTypeDefinition::item),
            Codec.INT.fieldOf("ticks_per_unit").forGetter(FuelTypeDefinition::ticksPerUnit),
            Codec.FLOAT.fieldOf("acceleration_multiplier").forGetter(FuelTypeDefinition::accelerationMultiplier),
            simpleParticleCodec()
                    .optionalFieldOf("exhaust_particle")
                    .forGetter(d -> java.util.Optional.ofNullable(d.exhaustParticle())),
            simpleParticleCodec()
                    .optionalFieldOf("trail_particle")
                    .forGetter(d -> java.util.Optional.ofNullable(d.trailParticle())),
            simpleParticleCodec()
                    .optionalFieldOf("boost_particle")
                    .forGetter(d -> java.util.Optional.ofNullable(d.boostParticle()))
    ).apply(i, (name, item, ticks, accel, exhaust, trail, boost) -> new FuelTypeDefinition(name, item, ticks, accel, exhaust.orElse(null), trail.orElse(null), boost.orElse(null))));

    public static final StreamCodec<FriendlyByteBuf, FuelTypeDefinition> STREAM_CODEC = StreamCodec.of(
            FuelTypeDefinition::encode,
            FuelTypeDefinition::decode
    );

    private static void encode(FriendlyByteBuf buf, FuelTypeDefinition def) {
        ByteBufCodecs.STRING_UTF8.encode(buf, def.displayName());
        ResourceLocation.STREAM_CODEC.encode(buf, requireRegistryKey(BuiltInRegistries.ITEM.getKey(def.item()), "item", def.item()));
        ByteBufCodecs.VAR_INT.encode(buf, def.ticksPerUnit());
        buf.writeFloat(def.accelerationMultiplier());
        encodeOptionalParticle(buf, def.exhaustParticle());
        encodeOptionalParticle(buf, def.trailParticle());
        encodeOptionalParticle(buf, def.boostParticle());
    }

    private static void encodeOptionalParticle(FriendlyByteBuf buf, SimpleParticleType pt) {
        buf.writeBoolean(pt != null);
        if (pt != null)
            ResourceLocation.STREAM_CODEC.encode(buf, requireRegistryKey(BuiltInRegistries.PARTICLE_TYPE.getKey(pt), "particle", pt));
    }

    private static ResourceLocation requireRegistryKey(ResourceLocation key, String kind, Object value) {
        if (key == null) throw new IllegalStateException("Fuel type " + kind + " is not registered: " + value);
        return key;
    }

    private static FuelTypeDefinition decode(FriendlyByteBuf buf) {
        String displayName = ByteBufCodecs.STRING_UTF8.decode(buf);
        Item item = BuiltInRegistries.ITEM.get(ResourceLocation.STREAM_CODEC.decode(buf));
        int ticksPerUnit = ByteBufCodecs.VAR_INT.decode(buf);
        float accel = buf.readFloat();
        SimpleParticleType exhaust = decodeOptionalParticle(buf);
        SimpleParticleType trail = decodeOptionalParticle(buf);
        SimpleParticleType boost = decodeOptionalParticle(buf);
        return new FuelTypeDefinition(displayName, item, ticksPerUnit, accel, exhaust, trail, boost);
    }

    private static SimpleParticleType decodeOptionalParticle(FriendlyByteBuf buf) {
        if (!buf.readBoolean()) return null;
        ResourceLocation id = ResourceLocation.STREAM_CODEC.decode(buf);
        ParticleType<?> pt = BuiltInRegistries.PARTICLE_TYPE.get(id);
        if (pt instanceof SimpleParticleType simple) return simple;
        LOGGER.error("Particle '{}' is not a SimpleParticleType, falling back to poof", id);
        return ParticleTypes.POOF;
    }

    private static Codec<SimpleParticleType> simpleParticleCodec() {
        return BuiltInRegistries.PARTICLE_TYPE.byNameCodec().flatXmap(
                pt -> pt instanceof SimpleParticleType s
                        ? DataResult.success(s)
                        : DataResult.error(() -> "Not a simple particle type: " + BuiltInRegistries.PARTICLE_TYPE.getKey(pt)),
                DataResult::success
        );
    }
}
