package pl.siarko.jetlytra.flight;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

public record FuelData(ResourceLocation typeId, int count) {

    public static final int MAX_COUNT = 64 * 9;

    public static final Codec<FuelData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ResourceLocation.CODEC.fieldOf("type").forGetter(FuelData::typeId),
                    Codec.INT.fieldOf("count").forGetter(FuelData::count)
            ).apply(instance, FuelData::new)
    );

    public static final StreamCodec<ByteBuf, FuelData> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, FuelData::typeId,
            net.minecraft.network.codec.ByteBufCodecs.VAR_INT, FuelData::count,
            FuelData::new
    );

    public Optional<FuelTypeDefinition> getDefinition() {
        return FuelTypeRegistry.byId(typeId);
    }
}
