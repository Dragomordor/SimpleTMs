package dragomordor.simpletms.network

import com.cobblemon.mod.common.Cobblemon
import dev.architectury.networking.NetworkManager
import dragomordor.simpletms.SimpleTMs
import dragomordor.simpletms.api.MoveCaseStorage
import dragomordor.simpletms.block.entity.TMMachineBlockEntity
import dragomordor.simpletms.item.custom.MoveCaseItem
import dragomordor.simpletms.ui.MoveCaseMenu
import dragomordor.simpletms.ui.MoveCaseScreen
import dragomordor.simpletms.ui.PokemonFilterData
import dragomordor.simpletms.ui.TMMachineMenu
import dragomordor.simpletms.ui.TMMachineScreen
import dragomordor.simpletms.util.MoveHelper
import net.minecraft.client.Minecraft
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * Network handler for SimpleTMs packets.
 * Handles TM Machine interactions and party selection.
 */
object SimpleTMsNetwork {

    // Packet IDs
    private val MACHINE_WITHDRAW_ID = ResourceLocation.fromNamespaceAndPath(SimpleTMs.MOD_ID, "machine_withdraw")
    private val MACHINE_DEPOSIT_ID = ResourceLocation.fromNamespaceAndPath(SimpleTMs.MOD_ID, "machine_deposit")
    private val REQUEST_PARTY_SELECTION_ID = ResourceLocation.fromNamespaceAndPath(SimpleTMs.MOD_ID, "request_party_selection")
    private val PARTY_SELECTION_RESPONSE_ID = ResourceLocation.fromNamespaceAndPath(SimpleTMs.MOD_ID, "party_selection_response")
    private val POKEMON_FILTER_DATA_ID = ResourceLocation.fromNamespaceAndPath(SimpleTMs.MOD_ID, "pokemon_filter_data")
    private val MACHINE_SYNC_ID = ResourceLocation.fromNamespaceAndPath(SimpleTMs.MOD_ID, "machine_sync")
    private val CASE_SLOT_CLICK_ID = ResourceLocation.fromNamespaceAndPath(SimpleTMs.MOD_ID, "case_slot_click")
    private val MACHINE_PARTY_SELECT_ID = ResourceLocation.fromNamespaceAndPath(SimpleTMs.MOD_ID, "machine_party_select")
    private val CASE_PARTY_SELECT_ID = ResourceLocation.fromNamespaceAndPath(SimpleTMs.MOD_ID, "case_party_select")
    private val CASE_POKEMON_FILTER_ID = ResourceLocation.fromNamespaceAndPath(SimpleTMs.MOD_ID, "case_pokemon_filter")

    // ========================================
    // Server-Side Pending Filter Storage
    // ========================================

    // Server-side storage for pending TM Machine filters (per-player)
    // This solves the race condition where the filter packet might arrive after the menu opens
    private val pendingMachineFilters: ConcurrentHashMap<UUID, PokemonFilterData> = ConcurrentHashMap()

    /**
     * Store a pending filter for a player's TM Machine (server-side)
     */
    fun setPendingMachineFilter(playerUUID: UUID, filter: PokemonFilterData) {
        pendingMachineFilters[playerUUID] = filter
    }

    /**
     * Consume and return the pending filter for a player's TM Machine (server-side)
     */
    fun consumePendingMachineFilter(playerUUID: UUID): PokemonFilterData? {
        return pendingMachineFilters.remove(playerUUID)
    }

    // ========================================
    // Packet Payloads
    // ========================================

    /**
     * Client -> Server: Click on a TM Machine slot
     * Supports both normal click (pick up to cursor) and shift-click (transfer to inventory)
     */
    data class MachineSlotClickPacket(
        val moveName: String,
        val isTR: Boolean,
        val isShiftClick: Boolean
    ) : CustomPacketPayload {
        override fun type(): CustomPacketPayload.Type<MachineSlotClickPacket> = TYPE

        companion object {
            val TYPE = CustomPacketPayload.Type<MachineSlotClickPacket>(MACHINE_WITHDRAW_ID)
            val CODEC: StreamCodec<FriendlyByteBuf, MachineSlotClickPacket> = StreamCodec.of(
                { buf, packet ->
                    buf.writeUtf(packet.moveName)
                    buf.writeBoolean(packet.isTR)
                    buf.writeBoolean(packet.isShiftClick)
                },
                { buf ->
                    MachineSlotClickPacket(buf.readUtf(), buf.readBoolean(), buf.readBoolean())
                }
            )
        }
    }

    /**
     * Client -> Server: Request to open party selection for Pokémon filter
     */
    data class RequestPartySelectionPacket(
        val dummy: Boolean = true // Payload needs at least one field
    ) : CustomPacketPayload {
        override fun type(): CustomPacketPayload.Type<RequestPartySelectionPacket> = TYPE

        companion object {
            val TYPE = CustomPacketPayload.Type<RequestPartySelectionPacket>(REQUEST_PARTY_SELECTION_ID)
            val CODEC: StreamCodec<FriendlyByteBuf, RequestPartySelectionPacket> = StreamCodec.of(
                { buf, _ -> buf.writeBoolean(true) },
                { _ -> RequestPartySelectionPacket() }
            )
        }
    }

    /**
     * Client -> Server: Player selected a Pokémon from their party
     */
    data class PartySelectionResponsePacket(
        val partySlot: Int // 0-5 for party slots, -1 for cancelled
    ) : CustomPacketPayload {
        override fun type(): CustomPacketPayload.Type<PartySelectionResponsePacket> = TYPE

        companion object {
            val TYPE = CustomPacketPayload.Type<PartySelectionResponsePacket>(PARTY_SELECTION_RESPONSE_ID)
            val CODEC: StreamCodec<FriendlyByteBuf, PartySelectionResponsePacket> = StreamCodec.of(
                { buf, packet -> buf.writeVarInt(packet.partySlot) },
                { buf -> PartySelectionResponsePacket(buf.readVarInt()) }
            )
        }
    }

    /**
     * Server -> Client: Send Pokémon filter data after party selection
     */
    data class PokemonFilterDataPacket(
        val speciesId: String,
        val formName: String,
        val displayName: String,
        val learnableMoves: Set<String>
    ) : CustomPacketPayload {
        override fun type(): CustomPacketPayload.Type<PokemonFilterDataPacket> = TYPE

        companion object {
            val TYPE = CustomPacketPayload.Type<PokemonFilterDataPacket>(POKEMON_FILTER_DATA_ID)
            val CODEC: StreamCodec<FriendlyByteBuf, PokemonFilterDataPacket> = StreamCodec.of(
                { buf, packet ->
                    buf.writeUtf(packet.speciesId)
                    buf.writeUtf(packet.formName)
                    buf.writeUtf(packet.displayName)
                    buf.writeVarInt(packet.learnableMoves.size)
                    packet.learnableMoves.forEach { buf.writeUtf(it) }
                },
                { buf ->
                    val speciesId = buf.readUtf()
                    val formName = buf.readUtf()
                    val displayName = buf.readUtf()
                    val moveCount = buf.readVarInt()
                    val moves = mutableSetOf<String>()
                    repeat(moveCount) { moves.add(buf.readUtf()) }
                    PokemonFilterDataPacket(speciesId, formName, displayName, moves)
                }
            )
        }
    }

    /**
     * Server -> Client: Sync TM Machine contents to client
     */
    data class MachineSyncPacket(
        val storedMoves: Map<String, TMMachineBlockEntity.StoredMoveData>
    ) : CustomPacketPayload {
        override fun type(): CustomPacketPayload.Type<MachineSyncPacket> = TYPE

        companion object {
            val TYPE = CustomPacketPayload.Type<MachineSyncPacket>(MACHINE_SYNC_ID)
            val CODEC: StreamCodec<FriendlyByteBuf, MachineSyncPacket> = StreamCodec.of(
                { buf, packet ->
                    buf.writeVarInt(packet.storedMoves.size)
                    packet.storedMoves.forEach { (moveName, data) ->
                        buf.writeUtf(moveName)
                        buf.writeVarInt(data.tmCount)
                        buf.writeVarInt(data.trCount)
                    }
                },
                { buf ->
                    val count = buf.readVarInt()
                    val moves = mutableMapOf<String, TMMachineBlockEntity.StoredMoveData>()
                    repeat(count) {
                        val moveName = buf.readUtf()
                        val tmCount = buf.readVarInt()
                        val trCount = buf.readVarInt()
                        moves[moveName] = TMMachineBlockEntity.StoredMoveData(tmCount, trCount)
                    }
                    MachineSyncPacket(moves)
                }
            )
        }
    }

    /**
     * Client -> Server: Click on a case slot (for MoveCaseMenu)
     */
    data class CaseSlotClickPacket(
        val moveName: String,
        val isShiftClick: Boolean
    ) : CustomPacketPayload {
        override fun type(): CustomPacketPayload.Type<CaseSlotClickPacket> = TYPE

        companion object {
            val TYPE = CustomPacketPayload.Type<CaseSlotClickPacket>(CASE_SLOT_CLICK_ID)
            val CODEC: StreamCodec<FriendlyByteBuf, CaseSlotClickPacket> = StreamCodec.of(
                { buf, packet ->
                    buf.writeUtf(packet.moveName)
                    buf.writeBoolean(packet.isShiftClick)
                },
                { buf ->
                    CaseSlotClickPacket(buf.readUtf(), buf.readBoolean())
                }
            )
        }
    }

    /**
     * Client -> Server: Request party selection for TM Machine Pokémon filter
     */
    class MachinePartySelectPacket : CustomPacketPayload {
        override fun type(): CustomPacketPayload.Type<MachinePartySelectPacket> = TYPE

        companion object {
            val TYPE = CustomPacketPayload.Type<MachinePartySelectPacket>(MACHINE_PARTY_SELECT_ID)
            val CODEC: StreamCodec<FriendlyByteBuf, MachinePartySelectPacket> = StreamCodec.of(
                { _, _ -> },
                { _ -> MachinePartySelectPacket() }
            )
        }
    }

    /**
     * Client -> Server: Request party selection for Move Case Pokémon filter
     */
    data class CasePartySelectPacket(
        val isTR: Boolean
    ) : CustomPacketPayload {
        override fun type(): CustomPacketPayload.Type<CasePartySelectPacket> = TYPE

        companion object {
            val TYPE = CustomPacketPayload.Type<CasePartySelectPacket>(CASE_PARTY_SELECT_ID)
            val CODEC: StreamCodec<FriendlyByteBuf, CasePartySelectPacket> = StreamCodec.of(
                { buf, packet -> buf.writeBoolean(packet.isTR) },
                { buf -> CasePartySelectPacket(buf.readBoolean()) }
            )
        }
    }

    /**
     * Server -> Client: Send Pokémon filter data for Move Case after party selection
     */
    data class CasePokemonFilterPacket(
        val isTR: Boolean,
        val speciesId: String,
        val formName: String,
        val displayName: String,
        val learnableMoves: Set<String>
    ) : CustomPacketPayload {
        override fun type(): CustomPacketPayload.Type<CasePokemonFilterPacket> = TYPE

        companion object {
            val TYPE = CustomPacketPayload.Type<CasePokemonFilterPacket>(CASE_POKEMON_FILTER_ID)
            val CODEC: StreamCodec<FriendlyByteBuf, CasePokemonFilterPacket> = StreamCodec.of(
                { buf, packet ->
                    buf.writeBoolean(packet.isTR)
                    buf.writeUtf(packet.speciesId)
                    buf.writeUtf(packet.formName)
                    buf.writeUtf(packet.displayName)
                    buf.writeVarInt(packet.learnableMoves.size)
                    packet.learnableMoves.forEach { buf.writeUtf(it) }
                },
                { buf ->
                    val isTR = buf.readBoolean()
                    val speciesId = buf.readUtf()
                    val formName = buf.readUtf()
                    val displayName = buf.readUtf()
                    val moveCount = buf.readVarInt()
                    val moves = mutableSetOf<String>()
                    repeat(moveCount) { moves.add(buf.readUtf()) }
                    CasePokemonFilterPacket(isTR, speciesId, formName, displayName, moves)
                }
            )
        }
    }

    // ========================================
    // Registration
    // ========================================

    /**
     * Register C2S packets AND S2C packet types (server-side only for S2C).
     * Call from common init (runs on both sides).
     *
     * C2S packets: Server needs to receive these.
     * S2C packets: Server needs to know the type/codec to SEND these.
     *              On dedicated server, we register with registerS2CPayloadType.
     *              On client (including integrated server), registerClient() handles it via registerReceiver.
     */
    fun register() {
        // C2S: Client -> Server packets (server receives these)
        NetworkManager.registerReceiver(
            NetworkManager.Side.C2S,
            MachineSlotClickPacket.TYPE,
            MachineSlotClickPacket.CODEC,
            ::handleMachineSlotClick
        )

        NetworkManager.registerReceiver(
            NetworkManager.Side.C2S,
            RequestPartySelectionPacket.TYPE,
            RequestPartySelectionPacket.CODEC,
            ::handleRequestPartySelection
        )

        NetworkManager.registerReceiver(
            NetworkManager.Side.C2S,
            PartySelectionResponsePacket.TYPE,
            PartySelectionResponsePacket.CODEC,
            ::handlePartySelectionResponse
        )

        NetworkManager.registerReceiver(
            NetworkManager.Side.C2S,
            CaseSlotClickPacket.TYPE,
            CaseSlotClickPacket.CODEC,
            ::handleCaseSlotClick
        )

        NetworkManager.registerReceiver(
            NetworkManager.Side.C2S,
            MachinePartySelectPacket.TYPE,
            MachinePartySelectPacket.CODEC,
            ::handleMachinePartySelect
        )

        NetworkManager.registerReceiver(
            NetworkManager.Side.C2S,
            CasePartySelectPacket.TYPE,
            CasePartySelectPacket.CODEC,
            ::handleCasePartySelect
        )
    }

    /**
     * Register S2C packet types for dedicated server.
     * Call this ONLY on dedicated server (not on client).
     * This allows the server to send these packets without a receiver.
     */
    fun registerServer() {
        // S2C: Register packet types so dedicated server can SEND them
        // On client/integrated server, registerClient() handles this via registerReceiver
        NetworkManager.registerS2CPayloadType(
            PokemonFilterDataPacket.TYPE,
            PokemonFilterDataPacket.CODEC
        )

        NetworkManager.registerS2CPayloadType(
            MachineSyncPacket.TYPE,
            MachineSyncPacket.CODEC
        )

        NetworkManager.registerS2CPayloadType(
            CasePokemonFilterPacket.TYPE,
            CasePokemonFilterPacket.CODEC
        )
    }

    /**
     * Register S2C packet handlers - call from CLIENT init only.
     * The client needs to register handlers to RECEIVE these packets.
     * This also registers the packet types, so don't call registerServer() on client.
     */
    fun registerClient() {
        // S2C: Server -> Client packets (client receives these)
        NetworkManager.registerReceiver(
            NetworkManager.Side.S2C,
            PokemonFilterDataPacket.TYPE,
            PokemonFilterDataPacket.CODEC,
            ::handlePokemonFilterData
        )

        NetworkManager.registerReceiver(
            NetworkManager.Side.S2C,
            MachineSyncPacket.TYPE,
            MachineSyncPacket.CODEC,
            ::handleMachineSync
        )

        NetworkManager.registerReceiver(
            NetworkManager.Side.S2C,
            CasePokemonFilterPacket.TYPE,
            CasePokemonFilterPacket.CODEC,
            ::handleCasePokemonFilter
        )
    }

    // ========================================
    // Server-Side Handlers
    // ========================================

    private fun handleMachineSlotClick(packet: MachineSlotClickPacket, context: NetworkManager.PacketContext) {
        context.queue {
            val player = context.player as? ServerPlayer ?: return@queue
            val menu = player.containerMenu as? TMMachineMenu ?: return@queue

            menu.handleSlotClickByName(packet.moveName, packet.isTR, packet.isShiftClick)
        }
    }

    private fun handleRequestPartySelection(packet: RequestPartySelectionPacket, context: NetworkManager.PacketContext) {
        context.queue {
            val player = context.player as? ServerPlayer ?: return@queue
            // This packet type is no longer used but kept for backwards compatibility
        }
    }

    private fun handlePartySelectionResponse(packet: PartySelectionResponsePacket, context: NetworkManager.PacketContext) {
        context.queue {
            val player = context.player as? ServerPlayer ?: return@queue
            // This packet type is no longer used but kept for backwards compatibility
        }
    }

    private fun handleCaseSlotClick(packet: CaseSlotClickPacket, context: NetworkManager.PacketContext) {
        context.queue {
            val player = context.player as? ServerPlayer ?: return@queue
            val menu = player.containerMenu as? MoveCaseMenu ?: return@queue

            menu.handleCaseSlotClickByName(packet.moveName, packet.isShiftClick)
        }
    }

    private fun handleMachinePartySelect(packet: MachinePartySelectPacket, context: NetworkManager.PacketContext) {
        context.queue {
            val player = context.player as? ServerPlayer ?: return@queue

            // Verify player has TM Machine menu open
            val menu = player.containerMenu as? TMMachineMenu ?: return@queue

            val party = Cobblemon.storage.getParty(player)
            val partyList = party.toList()

            if (partyList.isEmpty()) return@queue

            // Store the block entity reference so we can reopen the menu after selection
            val blockEntity = menu.blockEntity ?: return@queue

            // Open party selection GUI using Cobblemon's callback system
            // This will close the current menu, but we'll reopen it after selection
            com.cobblemon.mod.common.api.callback.PartySelectCallbacks.createFromPokemon(
                player = player,
                pokemon = partyList,
                canSelect = { true },
                handler = { pokemon ->
                    // When Pokémon is selected, calculate filter data
                    val speciesId = pokemon.species.resourceIdentifier.toString()
                    val formName = pokemon.form.name
                    val displayName = pokemon.getDisplayName().string

                    // Calculate learnable moves for both TM and TR
                    val learnableMoves = mutableSetOf<String>()
                    learnableMoves.addAll(MoveHelper.getLearnableTMMoves(pokemon).map { it.lowercase() })
                    learnableMoves.addAll(MoveHelper.getLearnableTRMoves(pokemon).map { it.lowercase() })

                    // Store the filter data server-side so it's included when menu reopens
                    val filterData = PokemonFilterData(speciesId, formName, displayName, learnableMoves)
                    setPendingMachineFilter(player.uuid, filterData)

                    // Reopen the TM Machine menu via the block entity
                    // The openMenu method will check for pending filter and include it
                    blockEntity.openMenu(player)
                }
            )
        }
    }

    private fun handleCasePartySelect(packet: CasePartySelectPacket, context: NetworkManager.PacketContext) {
        context.queue {
            val player = context.player as? ServerPlayer ?: return@queue

            // Verify player has Move Case menu open
            val menu = player.containerMenu as? MoveCaseMenu ?: return@queue
            val isTR = packet.isTR

            val party = Cobblemon.storage.getParty(player)
            val partyList = party.toList()

            if (partyList.isEmpty()) return@queue

            // Store the item stack reference to reopen the case after selection
            // We need to find which hand has the case item
            val mainHandStack = player.mainHandItem
            val offHandStack = player.offhandItem

            val (caseStack, hand) = when {
                mainHandStack.item is MoveCaseItem && (mainHandStack.item as MoveCaseItem).isTR == isTR ->
                    mainHandStack to net.minecraft.world.InteractionHand.MAIN_HAND
                offHandStack.item is MoveCaseItem && (offHandStack.item as MoveCaseItem).isTR == isTR ->
                    offHandStack to net.minecraft.world.InteractionHand.OFF_HAND
                else -> return@queue // Case not found in hand
            }

            // Open party selection GUI using Cobblemon's callback system
            com.cobblemon.mod.common.api.callback.PartySelectCallbacks.createFromPokemon(
                player = player,
                pokemon = partyList,
                canSelect = { true },
                handler = { pokemon ->
                    // When Pokémon is selected, calculate filter data
                    val speciesId = pokemon.species.resourceIdentifier.toString()
                    val formName = pokemon.form.name
                    val displayName = pokemon.getDisplayName().string

                    // Calculate learnable moves for this case type only
                    val learnableMoves = MoveCaseItem.getLearnableMoves(pokemon, isTR)

                    // Save the selected Pokémon to server-side storage
                    // This way when the menu reopens, it will include this data in the packet
                    val storage = MoveCaseStorage.get(player)
                    storage.setSelectedPokemon(
                        player.uuid,
                        isTR,
                        MoveCaseStorage.SelectedPokemonInfo(speciesId, formName, displayName, learnableMoves)
                    )

                    // Reopen the case menu - use the item in the player's hand
                    // Use openMenuWithFreshSelection to auto-enable the filter
                    val currentStack = if (hand == net.minecraft.world.InteractionHand.MAIN_HAND)
                        player.mainHandItem else player.offhandItem

                    if (currentStack.item is MoveCaseItem) {
                        // Use the new method that marks this as a fresh selection
                        (currentStack.item as MoveCaseItem).openMenuWithFreshSelection(player)
                    }
                }
            )
        }
    }

    // ========================================
    // Client-Side Handlers
    // ========================================

    // Pending filter data for when the TM Machine menu reopens after party selection
    // Note: This is now mainly a fallback - the filter should come with the menu packet
    private var pendingPokemonFilter: PokemonFilterData? = null

    // Pending filter data for Move Case (separate for TM and TR cases)
    // Note: This is now mainly a fallback - the filter should come with the menu packet
    private var pendingCasePokemonFilter: PokemonFilterData? = null
    private var pendingCaseIsTR: Boolean = false

    /**
     * Get and clear the pending Pokémon filter (called when menu opens)
     */
    fun consumePendingPokemonFilter(): PokemonFilterData? {
        val filter = pendingPokemonFilter
        pendingPokemonFilter = null
        return filter
    }

    /**
     * Get and clear the pending Case Pokémon filter
     */
    fun consumePendingCasePokemonFilter(isTR: Boolean): PokemonFilterData? {
        if (pendingCasePokemonFilter != null && pendingCaseIsTR == isTR) {
            val filter = pendingCasePokemonFilter
            pendingCasePokemonFilter = null
            return filter
        }
        return null
    }

    private fun handlePokemonFilterData(packet: PokemonFilterDataPacket, context: NetworkManager.PacketContext) {
        context.queue {
            val filterData = PokemonFilterData(
                packet.speciesId,
                packet.formName,
                packet.displayName,
                packet.learnableMoves
            )

            // Store for when menu reopens (fallback)
            pendingPokemonFilter = filterData

            // Also try to apply directly if screen is still open
            val minecraft = Minecraft.getInstance()
            val screen = minecraft.screen as? TMMachineScreen
            screen?.onPokemonSelected(
                packet.speciesId,
                packet.formName,
                packet.displayName,
                packet.learnableMoves
            )
        }
    }

    private fun handleMachineSync(packet: MachineSyncPacket, context: NetworkManager.PacketContext) {
        context.queue {
            val minecraft = Minecraft.getInstance()
            val menu = minecraft.player?.containerMenu as? TMMachineMenu ?: return@queue

            menu.updateClientCache(packet.storedMoves)
            menu.markDirty()
        }
    }

    private fun handleCasePokemonFilter(packet: CasePokemonFilterPacket, context: NetworkManager.PacketContext) {
        context.queue {
            val filterData = PokemonFilterData(
                packet.speciesId,
                packet.formName,
                packet.displayName,
                packet.learnableMoves
            )

            // Store for potential use (fallback)
            pendingCasePokemonFilter = filterData
            pendingCaseIsTR = packet.isTR

            // Try to apply directly if the case screen is still open
            val minecraft = Minecraft.getInstance()
            val screen = minecraft.screen as? MoveCaseScreen
            screen?.onPokemonSelected(filterData)
        }
    }

    // ========================================
    // Client-Side Send Methods
    // ========================================

    /**
     * Send a slot click to the TM Machine server
     * @param moveName The name of the move
     * @param isTR Whether clicking on TR slot (vs TM)
     * @param isShiftClick Whether shift is held (transfer vs pick up)
     */
    fun sendMachineSlotClick(moveName: String, isTR: Boolean, isShiftClick: Boolean) {
        NetworkManager.sendToServer(MachineSlotClickPacket(moveName, isTR, isShiftClick))
    }

    /**
     * Request party selection for Pokémon filter
     */
    fun sendRequestPartySelection() {
        NetworkManager.sendToServer(RequestPartySelectionPacket())
    }

    /**
     * Send party selection response to server
     */
    fun sendPartySelectionResponse(partySlot: Int) {
        NetworkManager.sendToServer(PartySelectionResponsePacket(partySlot))
    }

    /**
     * Send a case slot click to the server (for MoveCaseMenu)
     */
    fun sendCaseSlotClick(moveName: String, isShiftClick: Boolean) {
        NetworkManager.sendToServer(CaseSlotClickPacket(moveName, isShiftClick))
    }

    /**
     * Request party selection for TM Machine Pokémon filter
     */
    fun sendMachinePartySelectRequest() {
        NetworkManager.sendToServer(MachinePartySelectPacket())
    }

    /**
     * Request party selection for Move Case Pokémon filter
     */
    fun sendCasePartySelectRequest(isTR: Boolean) {
        NetworkManager.sendToServer(CasePartySelectPacket(isTR))
    }

    // ========================================
    // Server-Side Send Methods
    // ========================================

    /**
     * Sync TM Machine contents to a specific player
     */
    fun syncMachineToPlayer(player: ServerPlayer, blockEntity: TMMachineBlockEntity) {
        val packet = MachineSyncPacket(blockEntity.getStoredQuantities())
        NetworkManager.sendToPlayer(player, packet)
    }

    /**
     * Sync TM Machine contents to all players viewing it
     */
    fun syncMachineToViewers(blockEntity: TMMachineBlockEntity) {
        val level = blockEntity.level ?: return
        val pos = blockEntity.blockPos

        level.players().forEach { player ->
            if (player is ServerPlayer) {
                val menu = player.containerMenu as? TMMachineMenu ?: return@forEach
                // Compare by block position instead of object reference for reliability on dedicated servers
                val menuBlockEntity = menu.blockEntity ?: return@forEach
                if (menuBlockEntity.blockPos == pos && menuBlockEntity.level == level) {
                    syncMachineToPlayer(player, blockEntity)
                }
            }
        }
    }
}