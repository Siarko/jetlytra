package pl.siarko.jetlytra.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;
import pl.siarko.jetlytra.Jetlytra;
import pl.siarko.jetlytra.config.ClientServerConfig;
import pl.siarko.jetlytra.config.JetlytraServerConfig;

public record S2CServerConfigSyncPacket(
        double thrustAccel,
        double maxThrustVel,
        double thrustAccelDown,
        double hoverThrustAccel,
        double hoverThrustMax,
        double elytraBoostAccel,
        double elytraBoostMax,
        double swimBoostMax,
        double sprintBoostAccel,
        double sprintBoostMax
) implements CustomPacketPayload {

    public static final Type<S2CServerConfigSyncPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Jetlytra.MODID, "server_config_sync"));

    public static final StreamCodec<FriendlyByteBuf, S2CServerConfigSyncPacket> CODEC = StreamCodec.of(
            (buf, p) -> {
                buf.writeDouble(p.thrustAccel());
                buf.writeDouble(p.maxThrustVel());
                buf.writeDouble(p.thrustAccelDown());
                buf.writeDouble(p.hoverThrustAccel());
                buf.writeDouble(p.hoverThrustMax());
                buf.writeDouble(p.elytraBoostAccel());
                buf.writeDouble(p.elytraBoostMax());
                buf.writeDouble(p.swimBoostMax());
                buf.writeDouble(p.sprintBoostAccel());
                buf.writeDouble(p.sprintBoostMax());
            },
            buf -> new S2CServerConfigSyncPacket(
                    buf.readDouble(), buf.readDouble(), buf.readDouble(),
                    buf.readDouble(), buf.readDouble(), buf.readDouble(),
                    buf.readDouble(), buf.readDouble(), buf.readDouble(),
                    buf.readDouble()
            )
    );

    public static void handle(S2CServerConfigSyncPacket p, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ClientServerConfig.thrustAccel    = p.thrustAccel();
            ClientServerConfig.maxThrustVel   = p.maxThrustVel();
            ClientServerConfig.thrustAccelDown = p.thrustAccelDown();
            ClientServerConfig.hoverThrustAccel = p.hoverThrustAccel();
            ClientServerConfig.hoverThrustMax  = p.hoverThrustMax();
            ClientServerConfig.elytraBoostAccel = p.elytraBoostAccel();
            ClientServerConfig.elytraBoostMax  = p.elytraBoostMax();
            ClientServerConfig.swimBoostMax    = p.swimBoostMax();
            ClientServerConfig.sprintBoostAccel = p.sprintBoostAccel();
            ClientServerConfig.sprintBoostMax  = p.sprintBoostMax();
        });
    }

    public static S2CServerConfigSyncPacket fromServerConfig() {
        return new S2CServerConfigSyncPacket(
                JetlytraServerConfig.THRUST_ACCEL.get(),
                JetlytraServerConfig.MAX_THRUST_VEL.get(),
                JetlytraServerConfig.THRUST_ACCEL_DOWN.get(),
                JetlytraServerConfig.HOVER_THRUST_ACCEL.get(),
                JetlytraServerConfig.HOVER_THRUST_MAX.get(),
                JetlytraServerConfig.ELYTRA_BOOST_ACCEL.get(),
                JetlytraServerConfig.ELYTRA_BOOST_MAX.get(),
                JetlytraServerConfig.SWIM_BOOST_MAX.get(),
                JetlytraServerConfig.SPRINT_BOOST_ACCEL.get(),
                JetlytraServerConfig.SPRINT_BOOST_MAX.get()
        );
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
