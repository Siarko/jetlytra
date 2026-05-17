package pl.siarko.jetlytra.block;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import pl.siarko.jetlytra.Jetlytra;

public class JetlytraBlocks {

    public static final String BLOCK_JETPACK = "jetpack";

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registries.BLOCK, Jetlytra.MODID);

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(
            Registries.BLOCK_ENTITY_TYPE,
            Jetlytra.MODID
    );

    public static final DeferredHolder<Block, JetpackBlock> JETPACK = BLOCKS.register(BLOCK_JETPACK, JetpackBlock::new);

    @SuppressWarnings("unchecked")
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<JetpackBlockEntity>> JETPACK_BE =
            BLOCK_ENTITY_TYPES.register(BLOCK_JETPACK, () -> (BlockEntityType<JetpackBlockEntity>) (
                    FMLEnvironment.dist.isClient()
                            ? BlockEntityType.Builder.of(JetpackBlockEntity::new, JETPACK.get()).build(null)
                            : BlockEntityType.Builder.of(JetpackBlockEntityBase::new, JETPACK.get()).build(null)
                    )
            );
}
