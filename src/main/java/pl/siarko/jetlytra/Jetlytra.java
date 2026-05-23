package pl.siarko.jetlytra;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import pl.siarko.jetlytra.config.JetlytraClientConfig;
import pl.siarko.jetlytra.config.JetlytraServerConfig;
import pl.siarko.jetlytra.network.S2CServerConfigSyncPacket;
import org.slf4j.Logger;
import pl.siarko.jetlytra.block.JetlytraBlocks;
import pl.siarko.jetlytra.compat.curios.JetlytraCuriosCompat;
import pl.siarko.jetlytra.flight.FuelTypeRegistry;
import pl.siarko.jetlytra.flight.JetpackPhysicsHandler;
import pl.siarko.jetlytra.network.S2CFuelTypeSyncPacket;
import pl.siarko.jetlytra.item.JetlytraCreativeTab;
import pl.siarko.jetlytra.item.JetlytraItems;
import pl.siarko.jetlytra.network.JetpackPackets;

@Mod(Jetlytra.MODID)
public class Jetlytra {
    public static final String MODID = "jetlytra";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Jetlytra(IEventBus modEventBus, ModContainer modContainer) {
        JetlytraItems.ITEMS.register(modEventBus);
        JetlytraItems.DATA_COMPONENTS.register(modEventBus);
        JetlytraAttachments.ATTACHMENTS.register(modEventBus);
        JetlytraBlocks.BLOCKS.register(modEventBus);
        JetlytraBlocks.BLOCK_ENTITY_TYPES.register(modEventBus);

        JetlytraCreativeTab.CREATIVE_TABS.register(modEventBus);

        modEventBus.addListener(JetpackPackets::register);
        modEventBus.addListener(JetlytraCapabilities::register);
        NeoForge.EVENT_BUS.addListener((AddReloadListenerEvent e) -> e.addListener(new FuelTypeRegistry()));
        NeoForge.EVENT_BUS.addListener((OnDatapackSyncEvent e) -> {
            S2CFuelTypeSyncPacket packet = new S2CFuelTypeSyncPacket(FuelTypeRegistry.getDefinitions());
            if (e.getPlayer() != null) {
                PacketDistributor.sendToPlayer(e.getPlayer(), packet);
            } else {
                e.getPlayerList().getPlayers().forEach(p -> PacketDistributor.sendToPlayer(p, packet));
            }
        });
        NeoForge.EVENT_BUS.addListener(JetpackPlayerEvents::onPlayerLogin);
        NeoForge.EVENT_BUS.addListener(JetpackPlayerEvents::onEquipmentChange);
        NeoForge.EVENT_BUS.addListener(JetpackPhysicsHandler::onPlayerTick);
        NeoForge.EVENT_BUS.addListener(JetpackMendingHandler::onXpChange);
        modContainer.registerConfig(ModConfig.Type.CLIENT, JetlytraClientConfig.SPEC);
        modContainer.registerConfig(ModConfig.Type.SERVER, JetlytraServerConfig.SPEC);

        modEventBus.addListener((ModConfigEvent.Reloading e) -> {
            if (!e.getConfig().getSpec().equals(JetlytraServerConfig.SPEC)) return;
            JetpackPhysicsHandler.onServerConfigReload();
            var server = ServerLifecycleHooks.getCurrentServer();
            if (server == null) return;
            S2CServerConfigSyncPacket sync = S2CServerConfigSyncPacket.fromServerConfig();
            server.getPlayerList().getPlayers().forEach(p -> PacketDistributor.sendToPlayer(p, sync));
        });

        if (ModList.get().isLoaded("curios")) {
            JetlytraCuriosCompat.register(modEventBus);
        }
    }
}
