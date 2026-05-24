package pl.siarko.jetlytra.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;
import pl.siarko.jetlytra.Jetlytra;
import pl.siarko.jetlytra.compat.jei.JeiIntegration;
import pl.siarko.jetlytra.flight.FuelTypeDefinition;
import pl.siarko.jetlytra.flight.FuelTypeRegistry;

import java.util.HashMap;
import java.util.Map;

public record S2CFuelTypeSyncPacket(Map<ResourceLocation, FuelTypeDefinition> definitions) implements CustomPacketPayload {

    public static final Type<S2CFuelTypeSyncPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Jetlytra.MODID, "fuel_type_sync"));

    public static final StreamCodec<FriendlyByteBuf, S2CFuelTypeSyncPacket> CODEC = StreamCodec.of(
            S2CFuelTypeSyncPacket::encode,
            S2CFuelTypeSyncPacket::decode
    );

    private static void encode(FriendlyByteBuf buf, S2CFuelTypeSyncPacket packet) {
        buf.writeVarInt(packet.definitions().size());
        for (Map.Entry<ResourceLocation, FuelTypeDefinition> entry : packet.definitions().entrySet()) {
            ResourceLocation.STREAM_CODEC.encode(buf, entry.getKey());
            FuelTypeDefinition.STREAM_CODEC.encode(buf, entry.getValue());
        }
    }

    private static S2CFuelTypeSyncPacket decode(FriendlyByteBuf buf) {
        int size = buf.readVarInt();
        Map<ResourceLocation, FuelTypeDefinition> map = new HashMap<>();
        for (int i = 0; i < size; i++) {
            ResourceLocation key = ResourceLocation.STREAM_CODEC.decode(buf);
            FuelTypeDefinition def = FuelTypeDefinition.STREAM_CODEC.decode(buf);
            map.put(key, def);
        }
        return new S2CFuelTypeSyncPacket(map);
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(S2CFuelTypeSyncPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            FuelTypeRegistry.applyClientSync(packet.definitions());
            JeiIntegration.notifyFuelTypesUpdated();
        });
    }
}
