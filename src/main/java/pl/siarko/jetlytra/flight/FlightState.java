package pl.siarko.jetlytra.flight;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public enum FlightState {
    JETPACK,
    HOVERING,
    ELYTRA;

    public static final Codec<FlightState> CODEC = Codec.STRING.comapFlatMap(
            name -> {
                try {
                    return DataResult.success(FlightState.valueOf(name));
                } catch (IllegalArgumentException e) {
                    return DataResult.error(() -> "Unknown FlightState: " + name);
                }
            },
            FlightState::name
    );

    public static final StreamCodec<ByteBuf, FlightState> STREAM_CODEC = ByteBufCodecs.idMapper(
            id -> FlightState.values()[id],
            FlightState::ordinal
    );
}
