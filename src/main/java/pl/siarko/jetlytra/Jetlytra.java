package pl.siarko.jetlytra;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import pl.siarko.jetlytra.config.JetlytraClientConfig;
import org.slf4j.Logger;
import pl.siarko.jetlytra.block.JetlytraBlocks;
import pl.siarko.jetlytra.flight.JetpackPhysicsHandler;
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
        JetlytraAttachments.ATTACHMENT_TYPES.register(modEventBus);
        JetlytraBlocks.BLOCKS.register(modEventBus);
        JetlytraBlocks.BLOCK_ENTITY_TYPES.register(modEventBus);

        JetlytraCreativeTab.CREATIVE_TABS.register(modEventBus);

        modEventBus.addListener(JetpackPackets::register);
        NeoForge.EVENT_BUS.addListener(JetpackPlayerEvents::onPlayerLogin);
        NeoForge.EVENT_BUS.addListener(JetpackPlayerEvents::onPlayerClone);
        NeoForge.EVENT_BUS.addListener(JetpackPlayerEvents::onEquipmentChange);
        NeoForge.EVENT_BUS.addListener(JetpackPhysicsHandler::onPlayerTick);
        NeoForge.EVENT_BUS.addListener(JetpackMendingHandler::onXpChange);
        modContainer.registerConfig(ModConfig.Type.CLIENT, JetlytraClientConfig.SPEC);
    }
}
