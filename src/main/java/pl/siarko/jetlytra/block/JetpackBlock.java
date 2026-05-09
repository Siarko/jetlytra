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
import pl.siarko.jetlytra.flight.FuelData;
import pl.siarko.jetlytra.flight.FuelType;
import pl.siarko.jetlytra.item.JetlytraItems;

import javax.annotation.Nullable;

public class JetpackBlock extends BaseEntityBlock {

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    private static final VoxelShape SHAPE = Block.box(2, 0, 2, 14, 16, 14);

    private static final MapCodec<JetpackBlock> CODEC = simpleCodec(p -> new JetpackBlock());

    public JetpackBlock() {
        super(Properties.of()
                .mapColor(MapColor.METAL)
                .strength(1.5f)
                .noOcclusion());
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    public MapCodec<? extends BaseEntityBlock> codec() {
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
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new JetpackBlockEntity(pos, state);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (player.isShiftKeyDown()) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        if (stack.getItem() instanceof ElytraItem) {
            if (level.isClientSide) return ItemInteractionResult.SUCCESS;
            if (!(level.getBlockEntity(pos) instanceof JetpackBlockEntity be)) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
            if (!be.getElytraItem().isEmpty()) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
            be.setElytraItem(stack.copyWithCount(1));
            if (!player.isCreative()) stack.shrink(1);
            return ItemInteractionResult.SUCCESS;
        }

        var fuelType = FuelType.fromItem(stack.getItem());
        if (fuelType.isEmpty()) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        if (level.isClientSide) return ItemInteractionResult.SUCCESS;

        if (!(level.getBlockEntity(pos) instanceof JetpackBlockEntity be)) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        FuelData current = be.getFuelData();
        FuelType type = fuelType.get();

        if (current != null && current.type() != type) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

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

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (player.isShiftKeyDown()) {
            if (level.isClientSide) return InteractionResult.SUCCESS;
            if (!(level.getBlockEntity(pos) instanceof JetpackBlockEntity be)) return InteractionResult.PASS;
            ItemStack stack = new ItemStack(JetlytraItems.JETPACK.get());
            be.writeToItem(stack);
            level.removeBlock(pos, false);
            if (!player.getInventory().add(stack)) {
                player.drop(stack, false);
            }
            return InteractionResult.SUCCESS;
        }

        // Non-sneak empty-hand: retrieve stored elytra
        if (!player.getMainHandItem().isEmpty()) return InteractionResult.PASS;
        if (level.isClientSide) return InteractionResult.SUCCESS;
        if (!(level.getBlockEntity(pos) instanceof JetpackBlockEntity be)) return InteractionResult.PASS;
        ItemStack elytra = be.getElytraItem();
        if (elytra.isEmpty()) return InteractionResult.PASS;
        be.setElytraItem(ItemStack.EMPTY);
        if (!player.getInventory().add(elytra)) {
            player.drop(elytra, false);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide && !player.isCreative()) {
            if (level.getBlockEntity(pos) instanceof JetpackBlockEntity be) {
                ItemStack drop = new ItemStack(JetlytraItems.JETPACK.get());
                be.writeToItem(drop);
                popResource(level, pos, drop);
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }
}
