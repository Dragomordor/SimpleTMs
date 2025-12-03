package dragomordor.simpletms.block.custom

import com.mojang.serialization.MapCodec
import dragomordor.simpletms.block.entity.SimpleTMsBlockEntities
import dragomordor.simpletms.block.entity.TMMachineBlockEntity
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.Containers
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.LevelAccessor
import net.minecraft.world.level.block.BaseEntityBlock
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.HorizontalDirectionalBlock
import net.minecraft.world.level.block.RenderShape
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf
import net.minecraft.world.level.block.state.properties.EnumProperty
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.Shapes
import net.minecraft.world.phys.shapes.VoxelShape

/**
 * TM Machine block - a 2-block-high block that stores TMs and TRs.
 *
 * Features:
 * - Faces player when placed (horizontal directional)
 * - 2 blocks high (like a door)
 * - Contains a block entity for inventory storage
 * - Opens a GUI when right-clicked
 */
class TMMachineBlock(properties: Properties) : BaseEntityBlock(properties) {

    companion object {
        val CODEC: MapCodec<TMMachineBlock> = simpleCodec(::TMMachineBlock)

        val FACING: EnumProperty<Direction> = HorizontalDirectionalBlock.FACING
        val HALF: EnumProperty<DoubleBlockHalf> = BlockStateProperties.DOUBLE_BLOCK_HALF

        // Voxel shapes for collision/selection (full block for now, can be refined later)
        private val SHAPE_LOWER: VoxelShape = Shapes.block()
        private val SHAPE_UPPER: VoxelShape = Shapes.block()
    }

    init {
        registerDefaultState(
            stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(HALF, DoubleBlockHalf.LOWER)
        )
    }

    override fun codec(): MapCodec<out BaseEntityBlock> = CODEC

    // ========================================
    // Block State
    // ========================================

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        builder.add(FACING, HALF)
    }

    override fun getStateForPlacement(context: BlockPlaceContext): BlockState? {
        val pos = context.clickedPos
        val level = context.level

        // Check if there's room for the upper block
        if (pos.y < level.maxBuildHeight - 1 && level.getBlockState(pos.above()).canBeReplaced(context)) {
            return defaultBlockState()
                .setValue(FACING, context.horizontalDirection.opposite) // Face toward player
                .setValue(HALF, DoubleBlockHalf.LOWER)
        }
        return null
    }

    override fun setPlacedBy(
        level: Level,
        pos: BlockPos,
        state: BlockState,
        placer: LivingEntity?,
        stack: ItemStack
    ) {
        // Place the upper half
        level.setBlock(
            pos.above(),
            state.setValue(HALF, DoubleBlockHalf.UPPER),
            3
        )
    }

    // ========================================
    // Block Breaking / Updates
    // ========================================

    override fun updateShape(
        state: BlockState,
        direction: Direction,
        neighborState: BlockState,
        level: LevelAccessor,
        pos: BlockPos,
        neighborPos: BlockPos
    ): BlockState {
        val half = state.getValue(HALF)

        // Check if the other half is still there
        if (direction.axis == Direction.Axis.Y) {
            val isLower = half == DoubleBlockHalf.LOWER
            val shouldCheckAbove = isLower && direction == Direction.UP
            val shouldCheckBelow = !isLower && direction == Direction.DOWN

            if (shouldCheckAbove || shouldCheckBelow) {
                // If the other half is gone or not this block, break this half
                if (!neighborState.`is`(this) || neighborState.getValue(HALF) == half) {
                    return Blocks.AIR.defaultBlockState()
                }
            }
        }

        return super.updateShape(state, direction, neighborState, level, pos, neighborPos)
    }

//    override fun playerWillDestroy(level: Level, pos: BlockPos, state: BlockState, player: Player): BlockState {
//        if (!level.isClientSide && player.isCreative) {
//            val half = state.getValue(HALF)
//
//            val otherPos = if (half == DoubleBlockHalf.LOWER) pos.above() else pos.below()
//            val otherState = level.getBlockState(otherPos)
//
//            if (otherState.`is`(this) && otherState.getValue(HALF) != half) {
//                val lowerPos = if (half == DoubleBlockHalf.LOWER) pos else otherPos
//                val be = level.getBlockEntity(lowerPos)
//                if (be is TMMachineBlockEntity) {
//                    be.markCreativeDestruction()
//                }
//
//                level.setBlock(otherPos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL or Block.UPDATE_SUPPRESS_DROPS)
//                level.levelEvent(player, 2001, otherPos, getId(otherState))
//            }
//        }
//        return super.playerWillDestroy(level, pos, state, player)
//    }

    override fun playerDestroy(
        level: Level,
        player: Player,
        pos: BlockPos,
        state: BlockState,
        blockEntity: BlockEntity?,
        itemStack: ItemStack
    ) {
        if (!level.isClientSide && player.isCreative) {
            val half = state.getValue(HALF)

            val otherPos = if (half == DoubleBlockHalf.LOWER) pos.above() else pos.below()
            val otherState = level.getBlockState(otherPos)

            if (otherState.`is`(this) && otherState.getValue(HALF) != half) {
                val lowerPos = if (half == DoubleBlockHalf.LOWER) pos else otherPos
                val be = level.getBlockEntity(lowerPos)
                if (be is TMMachineBlockEntity) {
                    be.markCreativeDestruction()
                }

                level.setBlock(otherPos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL or Block.UPDATE_SUPPRESS_DROPS)
                level.levelEvent(player, 2001, otherPos, getId(otherState))
            }
        }
        super.playerDestroy(level, player, pos, state, blockEntity, itemStack)
    }

    override fun onRemove(state: BlockState, world: Level, pos: BlockPos, newState: BlockState, moved: Boolean) {
        if (!state.`is`(newState.block)) super.onRemove(state, world, pos, newState, moved)
    }

    // ========================================
    // Block Entity
    // ========================================

    override fun newBlockEntity(pos: BlockPos, state: BlockState): BlockEntity? {
        // Only the lower half has the block entity
        return if (state.getValue(HALF) == DoubleBlockHalf.LOWER) {
            TMMachineBlockEntity(pos, state)
        } else {
            null
        }
    }

    override fun getRenderShape(state: BlockState): RenderShape = RenderShape.MODEL

    // ========================================
    // Interaction
    // ========================================

    override fun useWithoutItem(
        state: BlockState,
        level: Level,
        pos: BlockPos,
        player: Player,
        hitResult: BlockHitResult
    ): InteractionResult {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS
        }

        // Get the lower block position (where the block entity is)
        val lowerPos = if (state.getValue(HALF) == DoubleBlockHalf.LOWER) pos else pos.below()
        val blockEntity = level.getBlockEntity(lowerPos)

        if (blockEntity is TMMachineBlockEntity && player is ServerPlayer) {
            blockEntity.openMenu(player)
            return InteractionResult.CONSUME
        }

        return InteractionResult.PASS
    }

    // ========================================
    // Shape / Rendering
    // ========================================

    override fun getShape(
        state: BlockState,
        level: BlockGetter,
        pos: BlockPos,
        context: CollisionContext
    ): VoxelShape {
        return if (state.getValue(HALF) == DoubleBlockHalf.LOWER) SHAPE_LOWER else SHAPE_UPPER
    }
}