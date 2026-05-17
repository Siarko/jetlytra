package pl.siarko.jetlytra.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ElytraItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import net.minecraft.resources.ResourceLocation;
import pl.siarko.jetlytra.flight.FuelData;
import pl.siarko.jetlytra.flight.FuelTypeRegistry;

import javax.annotation.Nullable;

public class JetpackBlock extends BaseEntityBlock {

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    private static final VoxelShape SHAPE = Block.box(2, 0, 3, 14, 13, 12);

    private static final MapCodec<JetpackBlock> CODEC = simpleCodec(p -> new JetpackBlock());

    public JetpackBlock() {
        super(Properties.of()
                .mapColor(MapColor.METAL)
                .strength(1.5f)
                .noOcclusion());

        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    public @NotNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public @NotNull VoxelShape getShape(
            @NotNull BlockState state,
            @NotNull BlockGetter level,
            @NotNull BlockPos pos,
            @NotNull CollisionContext context
    ) {
        return SHAPE;
    }

    @Override
    public @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return JetlytraBlocks.JETPACK_BE.get().create(pos, state);
    }

    @Override
    protected @NotNull ItemInteractionResult useItemOn(
            @NotNull ItemStack stack,
            @NotNull BlockState state,
            @NotNull Level level,
            @NotNull BlockPos pos,
            Player player,
            @NotNull InteractionHand hand,
            @NotNull BlockHitResult hit
    ) {
        if (player.isShiftKeyDown()) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        if (stack.getItem() instanceof ElytraItem) {
            return tryAddingElytra(level, player, pos, stack);
        }

        return tryAddingFuel(level, player, pos, stack);
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(
            @NotNull BlockState state,
            @NotNull Level level,
            @NotNull BlockPos pos,
            Player player,
            @NotNull BlockHitResult hit
    ) {
        if (player.isShiftKeyDown()) {
            return tryJetlyrtaBlockPickup(level, player, pos);
        }

        return tryStoredElytraPickup(level, player, pos);
    }

    @Override
    public boolean hasAnalogOutputSignal(@NotNull BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(@NotNull BlockState state, Level level, @NotNull BlockPos pos) {
        if (!(level.getBlockEntity(pos) instanceof JetpackBlockEntityBase be)) return 0;
        FuelData fuel = be.getFuelData();
        if (fuel == null || fuel.count() == 0) return 0;
        if (fuel.count() >= FuelData.MAX_COUNT) return 15;
        return (int) Math.ceil(fuel.count() * 14.0 / FuelData.MAX_COUNT);
    }

    @Override
    public @NotNull BlockState playerWillDestroy(
            Level level,
            @NotNull BlockPos pos,
            @NotNull BlockState state,
            @NotNull Player player
    ) {
        if (!level.isClientSide && !player.isCreative()) {
            if (level.getBlockEntity(pos) instanceof JetpackBlockEntityBase be) {
                ItemStack drop = be.createBaseStack();
                be.writeToItem(drop);
                popResource(level, pos, drop);
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    private ItemInteractionResult tryAddingElytra(Level level, Player player, BlockPos pos, ItemStack stack) {
        if (level.isClientSide) return ItemInteractionResult.SUCCESS;
        if (!(level.getBlockEntity(pos) instanceof JetpackBlockEntityBase be)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (!be.getElytraItem().isEmpty()) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        be.setElytraItem(stack.copyWithCount(1));
        if (!player.isCreative()) stack.shrink(1);
        return ItemInteractionResult.SUCCESS;
    }

    private ItemInteractionResult tryAddingFuel(Level level, Player player, BlockPos pos, ItemStack stack) {
        var typeId = FuelTypeRegistry.idFromItem(stack.getItem());
        if (typeId.isEmpty()) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        if (level.isClientSide) return ItemInteractionResult.SUCCESS;

        if (!(level.getBlockEntity(pos) instanceof JetpackBlockEntityBase be))
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        FuelData current = be.getFuelData();
        ResourceLocation type = typeId.get();

        if (current != null && !current.typeId().equals(type))
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        int currentCount = current != null ? current.count() : 0;
        int canAdd = FuelData.MAX_COUNT - currentCount;
        if (canAdd <= 0) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        int toAdd = Math.min(canAdd, stack.getCount());
        be.setFuelData(new FuelData(type, currentCount + toAdd));
        if (!player.isCreative()) {
            stack.shrink(toAdd);
        }
        return ItemInteractionResult.SUCCESS;
    }

    private InteractionResult tryJetlyrtaBlockPickup(Level level, Player player, BlockPos pos) {
        if (level.isClientSide) return InteractionResult.SUCCESS;
        if (!(level.getBlockEntity(pos) instanceof JetpackBlockEntityBase be)) return InteractionResult.PASS;
        ItemStack stack = be.createBaseStack();
        be.writeToItem(stack);
        level.removeBlock(pos, false);
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
        return InteractionResult.SUCCESS;
    }

    private InteractionResult tryStoredElytraPickup(Level level, Player player, BlockPos pos) {
        if (!player.getMainHandItem().isEmpty()) return InteractionResult.PASS;
        if (level.isClientSide) return InteractionResult.SUCCESS;
        if (!(level.getBlockEntity(pos) instanceof JetpackBlockEntityBase be)) return InteractionResult.PASS;
        ItemStack elytra = be.getElytraItem();
        if (elytra.isEmpty()) return InteractionResult.PASS;
        be.setElytraItem(ItemStack.EMPTY);
        if (!player.getInventory().add(elytra)) {
            player.drop(elytra, false);
        }
        return InteractionResult.SUCCESS;
    }
}
