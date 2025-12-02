package dragomordor.simpletms.api

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
 * Similar to how EnderChest data works - each player has their own storage
 * that persists across sessions and deaths.
 */
class MoveCaseStorage private constructor() : SavedData() {

    // Map of player UUID -> Set of move names they have stored
    private val tmStorage: MutableMap<UUID, MutableSet<String>> = mutableMapOf()
    private val trStorage: MutableMap<UUID, MutableSet<String>> = mutableMapOf()

    // ========================================
    // TM Storage Methods
    // ========================================

    /**
     * Get all TM move names stored by a player
     */
    fun getStoredTMs(playerUUID: UUID): Set<String> {
        return tmStorage[playerUUID]?.toSet() ?: emptySet()
    }

    /**
     * Check if a player has a specific TM stored
     */
    fun hasTM(playerUUID: UUID, moveName: String): Boolean {
        return tmStorage[playerUUID]?.contains(moveName) ?: false
    }

    /**
     * Add a TM to a player's storage
     * @return true if the TM was added, false if it was already present
     */
    fun addTM(playerUUID: UUID, moveName: String): Boolean {
        val playerTMs = tmStorage.getOrPut(playerUUID) { mutableSetOf() }
        val added = playerTMs.add(moveName)
        if (added) {
            setDirty()
        }
        return added
    }

    /**
     * Remove a TM from a player's storage
     * @return true if the TM was removed, false if it wasn't present
     */
    fun removeTM(playerUUID: UUID, moveName: String): Boolean {
        val playerTMs = tmStorage[playerUUID] ?: return false
        val removed = playerTMs.remove(moveName)
        if (removed) {
            setDirty()
        }
        return removed
    }

    // ========================================
    // TR Storage Methods
    // ========================================

    /**
     * Get all TR move names stored by a player
     */
    fun getStoredTRs(playerUUID: UUID): Set<String> {
        return trStorage[playerUUID]?.toSet() ?: emptySet()
    }

    /**
     * Check if a player has a specific TR stored
     */
    fun hasTR(playerUUID: UUID, moveName: String): Boolean {
        return trStorage[playerUUID]?.contains(moveName) ?: false
    }

    /**
     * Add a TR to a player's storage
     * @return true if the TR was added, false if it was already present
     */
    fun addTR(playerUUID: UUID, moveName: String): Boolean {
        val playerTRs = trStorage.getOrPut(playerUUID) { mutableSetOf() }
        val added = playerTRs.add(moveName)
        if (added) {
            setDirty()
        }
        return added
    }

    /**
     * Remove a TR from a player's storage
     * @return true if the TR was removed, false if it wasn't present
     */
    fun removeTR(playerUUID: UUID, moveName: String): Boolean {
        val playerTRs = trStorage[playerUUID] ?: return false
        val removed = playerTRs.remove(moveName)
        if (removed) {
            setDirty()
        }
        return removed
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
     * Check if a player has a specific move stored
     */
    fun hasMove(playerUUID: UUID, moveName: String, isTR: Boolean): Boolean {
        return if (isTR) hasTR(playerUUID, moveName) else hasTM(playerUUID, moveName)
    }

    /**
     * Add a move to a player's storage
     */
    fun addMove(playerUUID: UUID, moveName: String, isTR: Boolean): Boolean {
        return if (isTR) addTR(playerUUID, moveName) else addTM(playerUUID, moveName)
    }

    /**
     * Remove a move from a player's storage
     */
    fun removeMove(playerUUID: UUID, moveName: String, isTR: Boolean): Boolean {
        return if (isTR) removeTR(playerUUID, moveName) else removeTM(playerUUID, moveName)
    }

    // ========================================
    // NBT Serialization
    // ========================================

    override fun save(tag: CompoundTag, registries: HolderLookup.Provider): CompoundTag {
        // Save TM storage
        val tmCompound = CompoundTag()
        for ((uuid, moves) in tmStorage) {
            val moveList = ListTag()
            for (moveName in moves) {
                moveList.add(StringTag.valueOf(moveName))
            }
            tmCompound.put(uuid.toString(), moveList)
        }
        tag.put(TM_STORAGE_KEY, tmCompound)

        // Save TR storage
        val trCompound = CompoundTag()
        for ((uuid, moves) in trStorage) {
            val moveList = ListTag()
            for (moveName in moves) {
                moveList.add(StringTag.valueOf(moveName))
            }
            trCompound.put(uuid.toString(), moveList)
        }
        tag.put(TR_STORAGE_KEY, trCompound)

        return tag
    }

    companion object {
        private const val DATA_NAME = "simpletms_move_case_storage"
        private const val TM_STORAGE_KEY = "tm_storage"
        private const val TR_STORAGE_KEY = "tr_storage"

        /**
         * Factory for creating/loading MoveCaseStorage
         */
        private val factory = Factory(
            { MoveCaseStorage() },
            { tag, _ -> load(tag) },
            null
        )

        /**
         * Load MoveCaseStorage from NBT
         */
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
                        // Invalid UUID, skip this entry
                    }
                }
            }

            // Load TR storage
            if (tag.contains(TR_STORAGE_KEY, Tag.TAG_COMPOUND.toInt())) {
                val trCompound = tag.getCompound(TR_STORAGE_KEY)
                for (key in trCompound.allKeys) {
                    try {
                        val uuid = UUID.fromString(key)
                        val moveList = trCompound.getList(key, Tag.TAG_STRING.toInt())
                        val moves = mutableSetOf<String>()
                        for (i in 0 until moveList.size) {
                            moves.add(moveList.getString(i))
                        }
                        storage.trStorage[uuid] = moves
                    } catch (e: IllegalArgumentException) {
                        // Invalid UUID, skip this entry
                    }
                }
            }

            return storage
        }

        /**
         * Get the MoveCaseStorage for a server.
         * This retrieves or creates the storage from the overworld's saved data.
         */
        fun get(server: MinecraftServer): MoveCaseStorage {
            val overworld = server.getLevel(Level.OVERWORLD)
                ?: throw IllegalStateException("Overworld not available")

            return overworld.dataStorage.computeIfAbsent(factory, DATA_NAME)
        }

        /**
         * Convenience method to get storage from a ServerPlayer
         */
        fun get(player: ServerPlayer): MoveCaseStorage {
            return get(player.server)
        }
    }
}