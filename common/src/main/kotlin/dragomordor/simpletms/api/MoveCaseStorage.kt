package dragomordor.simpletms.api

import dragomordor.simpletms.SimpleTMs
import net.minecraft.core.HolderLookup
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.StringTag
import net.minecraft.nbt.Tag
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.Level
import net.minecraft.world.level.saveddata.SavedData
import java.util.UUID

/**
 * SavedData class that stores all players' TM and TR case contents.
 *
 * TMs are stored as a set (only 1 per move, since they have durability).
 * TRs are stored with quantities (up to stack size per move).
 */
class MoveCaseStorage private constructor() : SavedData() {

    // TMs: player UUID -> Set of move names (1 per move max)
    private val tmStorage: MutableMap<UUID, MutableSet<String>> = mutableMapOf()

    // TRs: player UUID -> Map of move name -> quantity
    private val trStorage: MutableMap<UUID, MutableMap<String, Int>> = mutableMapOf()

    // ========================================
    // TM Storage Methods (unchanged - still 1 per move)
    // ========================================

    fun getStoredTMs(playerUUID: UUID): Set<String> {
        return tmStorage[playerUUID]?.toSet() ?: emptySet()
    }

    fun hasTM(playerUUID: UUID, moveName: String): Boolean {
        return tmStorage[playerUUID]?.contains(moveName) ?: false
    }

    fun addTM(playerUUID: UUID, moveName: String): Boolean {
        val playerTMs = tmStorage.getOrPut(playerUUID) { mutableSetOf() }
        val added = playerTMs.add(moveName)
        if (added) setDirty()
        return added
    }

    fun removeTM(playerUUID: UUID, moveName: String): Boolean {
        val playerTMs = tmStorage[playerUUID] ?: return false
        val removed = playerTMs.remove(moveName)
        if (removed) setDirty()
        return removed
    }

    // ========================================
    // TR Storage Methods (with quantity support)
    // ========================================

    /**
     * Get all TR move names stored by a player (for compatibility)
     */
    fun getStoredTRs(playerUUID: UUID): Set<String> {
        return trStorage[playerUUID]?.keys?.toSet() ?: emptySet()
    }

    /**
     * Get TR quantities map for a player
     */
    fun getStoredTRQuantities(playerUUID: UUID): Map<String, Int> {
        return trStorage[playerUUID]?.toMap() ?: emptyMap()
    }

    /**
     * Check if a player has a specific TR stored (any quantity)
     */
    fun hasTR(playerUUID: UUID, moveName: String): Boolean {
        return (trStorage[playerUUID]?.get(moveName) ?: 0) > 0
    }

    /**
     * Get the quantity of a specific TR stored
     */
    fun getTRQuantity(playerUUID: UUID, moveName: String): Int {
        return trStorage[playerUUID]?.get(moveName) ?: 0
    }

    /**
     * Add TRs to a player's storage
     * @param amount Number to add (default 1)
     * @return The number actually added (may be less if hitting stack limit)
     */
    fun addTR(playerUUID: UUID, moveName: String, amount: Int = 1): Int {
        val maxStack = SimpleTMs.config.trStackSize
        val playerTRs = trStorage.getOrPut(playerUUID) { mutableMapOf() }
        val current = playerTRs[moveName] ?: 0
        val canAdd = (maxStack - current).coerceAtLeast(0)
        val toAdd = amount.coerceAtMost(canAdd)

        if (toAdd > 0) {
            playerTRs[moveName] = current + toAdd
            setDirty()
        }
        return toAdd
    }

    /**
     * Remove TRs from a player's storage
     * @param amount Number to remove (default 1)
     * @return The number actually removed
     */
    fun removeTR(playerUUID: UUID, moveName: String, amount: Int = 1): Int {
        val playerTRs = trStorage[playerUUID] ?: return 0
        val current = playerTRs[moveName] ?: return 0
        val toRemove = amount.coerceAtMost(current)

        if (toRemove > 0) {
            val newAmount = current - toRemove
            if (newAmount <= 0) {
                playerTRs.remove(moveName)
            } else {
                playerTRs[moveName] = newAmount
            }
            setDirty()
        }
        return toRemove
    }

    // ========================================
    // Generic Methods (for use with isTR flag)
    // ========================================

    /**
     * Get stored moves for a player based on whether it's TR or TM
     */
    fun getStoredMoves(playerUUID: UUID, isTR: Boolean): Set<String> {
        return if (isTR) getStoredTRs(playerUUID) else getStoredTMs(playerUUID)
    }

    /**
     * Get stored move quantities (TRs have quantities, TMs are always 1)
     */
    fun getStoredQuantities(playerUUID: UUID, isTR: Boolean): Map<String, Int> {
        return if (isTR) {
            getStoredTRQuantities(playerUUID)
        } else {
            // TMs are always quantity 1
            getStoredTMs(playerUUID).associateWith { 1 }
        }
    }

    /**
     * Check if a player has a specific move stored
     */
    fun hasMove(playerUUID: UUID, moveName: String, isTR: Boolean): Boolean {
        return if (isTR) hasTR(playerUUID, moveName) else hasTM(playerUUID, moveName)
    }

    /**
     * Get quantity of a specific move
     */
    fun getMoveQuantity(playerUUID: UUID, moveName: String, isTR: Boolean): Int {
        return if (isTR) getTRQuantity(playerUUID, moveName) else if (hasTM(playerUUID, moveName)) 1 else 0
    }

    /**
     * Add a move to a player's storage
     * @return The number actually added
     */
    fun addMove(playerUUID: UUID, moveName: String, isTR: Boolean, amount: Int = 1): Int {
        return if (isTR) {
            addTR(playerUUID, moveName, amount)
        } else {
            if (addTM(playerUUID, moveName)) 1 else 0
        }
    }

    /**
     * Remove a move from a player's storage
     * @return The number actually removed
     */
    fun removeMove(playerUUID: UUID, moveName: String, isTR: Boolean, amount: Int = 1): Int {
        return if (isTR) {
            removeTR(playerUUID, moveName, amount)
        } else {
            if (removeTM(playerUUID, moveName)) 1 else 0
        }
    }

    // ========================================
    // NBT Serialization
    // ========================================

    override fun save(tag: CompoundTag, registries: HolderLookup.Provider): CompoundTag {
        // Save TM storage (set of move names)
        val tmCompound = CompoundTag()
        for ((uuid, moves) in tmStorage) {
            val moveList = ListTag()
            for (moveName in moves) {
                moveList.add(StringTag.valueOf(moveName))
            }
            tmCompound.put(uuid.toString(), moveList)
        }
        tag.put(TM_STORAGE_KEY, tmCompound)

        // Save TR storage (map of move name -> quantity)
        val trCompound = CompoundTag()
        for ((uuid, moveMap) in trStorage) {
            val playerCompound = CompoundTag()
            for ((moveName, quantity) in moveMap) {
                playerCompound.putInt(moveName, quantity)
            }
            trCompound.put(uuid.toString(), playerCompound)
        }
        tag.put(TR_STORAGE_KEY, trCompound)

        return tag
    }

    companion object {
        private const val DATA_NAME = "simpletms_move_case_storage"
        private const val TM_STORAGE_KEY = "tm_storage"
        private const val TR_STORAGE_KEY = "tr_storage"

        private val factory = Factory(
            { MoveCaseStorage() },
            { tag, _ -> load(tag) },
            null
        )

        private fun load(tag: CompoundTag): MoveCaseStorage {
            val storage = MoveCaseStorage()

            // Load TM storage
            if (tag.contains(TM_STORAGE_KEY, Tag.TAG_COMPOUND.toInt())) {
                val tmCompound = tag.getCompound(TM_STORAGE_KEY)
                for (key in tmCompound.allKeys) {
                    try {
                        val uuid = UUID.fromString(key)
                        val moveList = tmCompound.getList(key, Tag.TAG_STRING.toInt())
                        val moves = mutableSetOf<String>()
                        for (i in 0 until moveList.size) {
                            moves.add(moveList.getString(i))
                        }
                        storage.tmStorage[uuid] = moves
                    } catch (e: IllegalArgumentException) {
                        // Invalid UUID, skip
                    }
                }
            }

            // Load TR storage - handle both old format (list) and new format (compound with quantities)
            if (tag.contains(TR_STORAGE_KEY, Tag.TAG_COMPOUND.toInt())) {
                val trCompound = tag.getCompound(TR_STORAGE_KEY)
                for (key in trCompound.allKeys) {
                    try {
                        val uuid = UUID.fromString(key)
                        val playerData = trCompound.get(key)
                        val moveMap = mutableMapOf<String, Int>()

                        when (playerData) {
                            is CompoundTag -> {
                                // New format: compound with move -> quantity
                                for (moveName in playerData.allKeys) {
                                    val quantity = playerData.getInt(moveName)
                                    if (quantity > 0) {
                                        moveMap[moveName] = quantity
                                    }
                                }
                            }
                            is ListTag -> {
                                // Old format: list of move names (migrate to quantity 1)
                                for (i in 0 until playerData.size) {
                                    moveMap[playerData.getString(i)] = 1
                                }
                            }
                        }

                        if (moveMap.isNotEmpty()) {
                            storage.trStorage[uuid] = moveMap
                        }
                    } catch (e: IllegalArgumentException) {
                        // Invalid UUID, skip
                    }
                }
            }

            return storage
        }

        fun get(server: MinecraftServer): MoveCaseStorage {
            val overworld = server.getLevel(Level.OVERWORLD)
                ?: throw IllegalStateException("Overworld not available")
            return overworld.dataStorage.computeIfAbsent(factory, DATA_NAME)
        }

        fun get(player: ServerPlayer): MoveCaseStorage {
            return get(player.server)
        }
    }
}