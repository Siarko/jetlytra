package pl.siarko.jetlytra.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;
import pl.siarko.jetlytra.Jetlytra;
import pl.siarko.jetlytra.config.JetlytraServerConfig;

public record C2SServerConfigChangePacket(
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

    public static final Type<C2SServerConfigChangePacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Jetlytra.MODID, "server_config_change"));

    public static final StreamCodec<FriendlyByteBuf, C2SServerConfigChangePacket> CODEC = StreamCodec.of(
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
            buf -> new C2SServerConfigChangePacket(
                    buf.readDouble(), buf.readDouble(), buf.readDouble(),
                    buf.readDouble(), buf.readDouble(), buf.readDouble(),
                    buf.readDouble(), buf.readDouble(), buf.readDouble(),
                    buf.readDouble()
            )
    );

    public static void handle(C2SServerConfigChangePacket p, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player)) return;
            if (!player.hasPermissions(2)) return;

            JetlytraServerConfig.THRUST_ACCEL.set(p.thrustAccel());
            JetlytraServerConfig.MAX_THRUST_VEL.set(p.maxThrustVel());
            JetlytraServerConfig.THRUST_ACCEL_DOWN.set(p.thrustAccelDown());
            JetlytraServerConfig.HOVER_THRUST_ACCEL.set(p.hoverThrustAccel());
            JetlytraServerConfig.HOVER_THRUST_MAX.set(p.hoverThrustMax());
            JetlytraServerConfig.ELYTRA_BOOST_ACCEL.set(p.elytraBoostAccel());
            JetlytraServerConfig.ELYTRA_BOOST_MAX.set(p.elytraBoostMax());
            JetlytraServerConfig.SWIM_BOOST_MAX.set(p.swimBoostMax());
            JetlytraServerConfig.SPRINT_BOOST_ACCEL.set(p.sprintBoostAccel());
            JetlytraServerConfig.SPRINT_BOOST_MAX.set(p.sprintBoostMax());
            JetlytraServerConfig.SPEC.save();

            S2CServerConfigSyncPacket sync = S2CServerConfigSyncPacket.fromServerConfig();
            player.getServer().getPlayerList().getPlayers()
                    .forEach(pl -> PacketDistributor.sendToPlayer(pl, sync));
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
