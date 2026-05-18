package pl.siarko.jetlytra.compat.curios;

import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.items.IItemHandler;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.CuriosCapability;
import pl.siarko.jetlytra.compat.CuriosBridge;
import pl.siarko.jetlytra.item.JetlytraItemBase;
import pl.siarko.jetlytra.item.JetlytraItems;

public class JetlytraCuriosCompat {

    public static void register(IEventBus modBus) {
        CuriosBridge.setFinder(player ->
                CuriosApi.getCuriosInventory(player)
                        .flatMap(inv -> inv.getStacksHandler("back"))
                        .map(handler -> {
                            IItemHandler stacks = handler.getStacks();
                            for (int i = 0; i < stacks.getSlots(); i++) {
                                ItemStack s = stacks.getStackInSlot(i);
                                if (s.getItem() instanceof JetlytraItemBase) return s;
                            }
                            return ItemStack.EMPTY;
                        })
                        .orElse(ItemStack.EMPTY)
        );

        modBus.addListener(JetlytraCuriosCompat::onRegisterCapabilities);

        if (FMLEnvironment.dist.isClient()) {
            JetlytraCuriosClientCompat.register(modBus);
        }
    }

    private static void onRegisterCapabilities(RegisterCapabilitiesEvent evt) {
        evt.registerItem(
                CuriosCapability.ITEM,
                (stack, ctx) -> new JetlytraCurioCapability(stack),
                JetlytraItems.JETPACK.get()
        );
    }
}
