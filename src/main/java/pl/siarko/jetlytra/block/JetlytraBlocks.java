package pl.siarko.jetlytra.block;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import pl.siarko.jetlytra.Jetlytra;

public class JetlytraBlocks {
    public static final DeferredRegister<net.minecraft.world.level.block.Block> BLOCKS = DeferredRegister.create(
            Registries.BLOCK,
            Jetlytra.MODID
    );

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(
            Registries.BLOCK_ENTITY_TYPE,
            Jetlytra.MODID
    );

    public static final DeferredHolder<net.minecraft.world.level.block.Block, JetpackBlock> JETPACK = BLOCKS.register(
            "jetpack",
            JetpackBlock::new
    );

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<JetpackBlockEntity>> JETPACK_BE =
            BLOCK_ENTITY_TYPES.register("jetpack", () ->
                    BlockEntityType.Builder.of(
                            JetpackBlockEntity::new,
                            JETPACK.get()
                    ).build(null)
            );
}
