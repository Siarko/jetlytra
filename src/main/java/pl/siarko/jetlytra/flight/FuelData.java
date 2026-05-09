package pl.siarko.jetlytra.flight;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record FuelData(FuelType type, int count) {

    public static final int MAX_COUNT = 128;

    public static final Codec<FuelData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    FuelType.CODEC.fieldOf("type").forGetter(FuelData::type),
                    Codec.INT.fieldOf("count").forGetter(FuelData::count)
            ).apply(instance, FuelData::new)
    );

    public static final StreamCodec<ByteBuf, FuelData> STREAM_CODEC = StreamCodec.composite(
            FuelType.STREAM_CODEC, FuelData::type,
            ByteBufCodecs.VAR_INT, FuelData::count,
            FuelData::new
    );
}