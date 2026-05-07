package pl.siarko.jetlytra.flight;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record FuelData(FuelType type, int count) {

    public static final int MAX_COUNT = 128;

    public static final Codec<FuelData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    FuelType.CODEC.fieldOf("type").forGetter(FuelData::type),
                    Codec.INT.fieldOf("count").forGetter(FuelData::count)
            ).apply(instance, FuelData::new)
    );
}