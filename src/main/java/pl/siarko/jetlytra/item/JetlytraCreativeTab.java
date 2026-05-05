package pl.siarko.jetlytra.item;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import pl.siarko.jetlytra.Jetlytra;

public class JetlytraCreativeTab {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Jetlytra.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> JETLYTRA_TAB = CREATIVE_TABS.register(
            "jetlytra_tab",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.jetlytra.main"))
                    .icon(() -> JetlytraItems.JETPACK.get().getDefaultInstance())
                    .displayItems((params, output) -> output.accept(JetlytraItems.JETPACK.get()))
                    .build()
    );
}
