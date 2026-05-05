package pl.siarko.jetlytra.capability;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import pl.siarko.jetlytra.Jetlytra;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class JetpackCapabilityAttacher {

    // Simple map-based storage since NeoForge 1.21.1 capability attachment for non-block entities
    // uses a different pattern than older versions
    private static final Map<UUID, JetpackCapabilityImpl> CAPABILITIES = new ConcurrentHashMap<>();

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(Jetlytra.MODID, "jetpack");

    public static JetpackCapabilityImpl get(Player player) {
        return CAPABILITIES.computeIfAbsent(player.getUUID(), uuid -> new JetpackCapabilityImpl());
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        Player original = event.getOriginal();
        Player clone = event.getEntity();
        JetpackCapabilityImpl originalCap = CAPABILITIES.get(original.getUUID());
        if (originalCap != null) {
            JetpackCapabilityImpl cloneCap = new JetpackCapabilityImpl();
            cloneCap.deserializeNBT(originalCap.serializeNBT());
            CAPABILITIES.put(clone.getUUID(), cloneCap);
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        // Clean up when player leaves to avoid memory leaks
        CAPABILITIES.remove(event.getEntity().getUUID());
    }
}
