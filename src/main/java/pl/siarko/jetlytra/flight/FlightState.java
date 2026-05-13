package pl.siarko.jetlytra.flight;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public enum FlightState {
    JETPACK,
    HOVERING,
    ELYTRA;

    public static final Codec<FlightState> CODEC = Codec.STRING.xmap(
            FlightState::valueOf,
            FlightState::name
    );

    public static final StreamCodec<ByteBuf, FlightState> STREAM_CODEC = ByteBufCodecs.idMapper(
            id -> FlightState.values()[id],
            FlightState::ordinal
    );
}
