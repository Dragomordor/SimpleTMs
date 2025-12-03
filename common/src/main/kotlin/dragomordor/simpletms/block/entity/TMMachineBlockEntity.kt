package dragomordor.simpletms.block.entity

import dragomordor.simpletms.SimpleTMs
import dragomordor.simpletms.item.custom.MoveLearnItem
import dragomordor.simpletms.ui.TMMachineMenu
import net.minecraft.core.BlockPos
import net.minecraft.core.HolderLookup
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.StringTag
import net.minecraft.nbt.Tag
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.Containers
import net.minecraft.world.MenuProvider
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import dev.architectury.registry.menu.MenuRegistry
import dragomordor.simpletms.SimpleTMsItems

/**
 * Block entity for the TM Machine.
 *
 * Stores TMs and TRs in a combined inventory:
 * - TMs: 1 per move (same as TM Case)
 * - TRs: Up to stack size per move (same as TR Case)
 *
 * The storage is public (not player-specific), but filtering is per-player.
 */
class TMMachineBlockEntity(
    pos: BlockPos,
    state: BlockState
) : BlockEntity(SimpleTMsBlockEntities.TM_MACHINE.get(), pos, state) {

    // Storage: move name -> StoredMoveData (tmCount, trCount)
    private val storedMoves: MutableMap<String, StoredMoveData> = mutableMapOf()

    /**
     * Data class to track both TM and TR quantities for a single move
     */
    data class StoredMoveData(
        var tmCount: Int = 0,
        var trCount: Int = 0
    ) {
        fun isEmpty(): Boolean = tmCount <= 0 && trCount <= 0
    }

    // Track if this block is being destroyed by a creative player
    private var creativeDestruction = false

    /**
     * Mark this block entity as being destroyed by a creative player.
     * This prevents inventory drops.
     */
    fun markCreativeDestruction() {
        creativeDestruction = true
    }

    /**
     * Check if this block entity was destroyed by a creative player.
     */
    fun wasCreativeDestruction(): Boolean = creativeDestruction

    // ========================================
    // Inventory Access
    // ========================================

    /**
     * Get the maximum stack size for TRs
     */
    val maxTRStackSize: Int get() = SimpleTMs.config.trStackSize

    /**
     * Get quantity of a specific move
     */
    fun getMoveQuantity(moveName: String, isTR: Boolean): Int {
        val data = storedMoves[moveName] ?: return 0
        return if (isTR) data.trCount else data.tmCount
    }

    /**
     * Check if a move is stored (either TM or TR)
     */
    fun hasMove(moveName: String, isTR: Boolean): Boolean {
        return getMoveQuantity(moveName, isTR) > 0
    }

    /**
     * Check if we can add more of this move
     */
    fun canAddMore(moveName: String, isTR: Boolean): Boolean {
        val current = getMoveQuantity(moveName, isTR)
        val max = if (isTR) maxTRStackSize else 1
        return current < max
    }

    /**
     * Add a move to storage
     * @return The number actually added
     */
    fun addMove(moveName: String, isTR: Boolean, amount: Int = 1): Int {
        val data = storedMoves.getOrPut(moveName) { StoredMoveData() }
        val current = if (isTR) data.trCount else data.tmCount
        val max = if (isTR) maxTRStackSize else 1
        val canAdd = (max - current).coerceAtLeast(0)
        val toAdd = amount.coerceAtMost(canAdd)

        if (toAdd > 0) {
            if (isTR) {
                data.trCount += toAdd
            } else {
                data.tmCount += toAdd
            }
            setChanged()
            syncToClients()
        }
        return toAdd
    }

    /**
     * Remove a move from storage
     * @return The number actually removed
     */
    fun removeMove(moveName: String, isTR: Boolean, amount: Int = 1): Int {
        val data = storedMoves[moveName] ?: return 0
        val current = if (isTR) data.trCount else data.tmCount
        val toRemove = amount.coerceAtMost(current)

        if (toRemove > 0) {
            if (isTR) {
                data.trCount -= toRemove
            } else {
                data.tmCount -= toRemove
            }

            // Clean up empty entries
            if (data.isEmpty()) {
                storedMoves.remove(moveName)
            }

            setChanged()
            syncToClients()
        }
        return toRemove
    }

    /**
     * Get all stored moves with quantities for syncing
     */
    fun getStoredQuantities(): Map<String, StoredMoveData> {
        return storedMoves.toMap()
    }

    /**
     * Get count of unique moves stored (for display)
     */
    fun getUniqueMoveCount(): Int = storedMoves.count { !it.value.isEmpty() }

    /**
     * Get total items stored (for display)
     */
    fun getTotalItemCount(): Int = storedMoves.values.sumOf { it.tmCount + it.trCount }

    // ========================================
    // Item Handling
    // ========================================

    /**
     * Try to insert an item stack into the machine
     * @return The remaining items that couldn't be inserted
     */
    fun tryInsert(stack: ItemStack): ItemStack {
        val item = stack.item
        if (item !is MoveLearnItem) return stack

        val moveName = item.moveName
        val isTR = item.isTR

        if (!canAddMore(moveName, isTR)) return stack

        val added = addMove(moveName, isTR, stack.count)
        if (added > 0) {
            val remaining = stack.copy()
            remaining.shrink(added)
            return remaining
        }
        return stack
    }

    /**
     * Drop all contents when block is broken
     */
    fun dropContents(level: Level, pos: BlockPos) {
        for ((moveName, data) in storedMoves) {
            // Drop TMs
            if (data.tmCount > 0) {
                val tmStack = SimpleTMsItems.getItemStackFromName("tm_$moveName")
                tmStack.count = data.tmCount
                Containers.dropItemStack(level, pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble(), tmStack)
            }
            // Drop TRs
            if (data.trCount > 0) {
                val trStack = SimpleTMsItems.getItemStackFromName("tr_$moveName")
                trStack.count = data.trCount
                Containers.dropItemStack(level, pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble(), trStack)
            }
        }
        storedMoves.clear()
    }

    // ========================================
    // Menu
    // ========================================

    fun openMenu(player: ServerPlayer) {
        val menuProvider = object : MenuProvider {
            override fun getDisplayName(): Component {
                return Component.translatable("container.simpletms.tm_machine")
            }

            override fun createMenu(
                containerId: Int,
                playerInventory: Inventory,
                player: Player
            ): AbstractContainerMenu {
                return TMMachineMenu(
                    containerId,
                    playerInventory,
                    this@TMMachineBlockEntity
                )
            }
        }

        MenuRegistry.openExtendedMenu(player, menuProvider) { buf ->
            // Write block position for client to find the block entity
            buf.writeBlockPos(worldPosition)

            // Write stored moves data
            buf.writeVarInt(storedMoves.size)
            for ((moveName, data) in storedMoves) {
                buf.writeUtf(moveName)
                buf.writeVarInt(data.tmCount)
                buf.writeVarInt(data.trCount)
            }
        }
    }

    // ========================================
    // NBT Serialization
    // ========================================

    override fun saveAdditional(tag: CompoundTag, registries: HolderLookup.Provider) {
        super.saveAdditional(tag, registries)

        val movesTag = CompoundTag()
        for ((moveName, data) in storedMoves) {
            val moveTag = CompoundTag()
            moveTag.putInt("tm", data.tmCount)
            moveTag.putInt("tr", data.trCount)
            movesTag.put(moveName, moveTag)
        }
        tag.put("stored_moves", movesTag)
    }

    override fun loadAdditional(tag: CompoundTag, registries: HolderLookup.Provider) {
        super.loadAdditional(tag, registries)

        storedMoves.clear()
        if (tag.contains("stored_moves", Tag.TAG_COMPOUND.toInt())) {
            val movesTag = tag.getCompound("stored_moves")
            for (moveName in movesTag.allKeys) {
                val moveTag = movesTag.getCompound(moveName)
                val tmCount = moveTag.getInt("tm")
                val trCount = moveTag.getInt("tr")
                if (tmCount > 0 || trCount > 0) {
                    storedMoves[moveName] = StoredMoveData(tmCount, trCount)
                }
            }
        }
    }

    // ========================================
    // Client Sync
    // ========================================

    override fun getUpdateTag(registries: HolderLookup.Provider): CompoundTag {
        val tag = CompoundTag()
        saveAdditional(tag, registries)
        return tag
    }

    override fun getUpdatePacket(): Packet<ClientGamePacketListener>? {
        return ClientboundBlockEntityDataPacket.create(this)
    }

    private fun syncToClients() {
        level?.let { lvl ->
            if (!lvl.isClientSide) {
                // Send block update for NBT sync
                lvl.sendBlockUpdated(worldPosition, blockState, blockState, 3)

                // Also sync via custom packets to all viewers
                dragomordor.simpletms.network.SimpleTMsNetwork.syncMachineToViewers(this)
            }
        }
    }
}