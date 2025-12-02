package dragomordor.simpletms.network

import dev.architectury.networking.NetworkManager
import dragomordor.simpletms.SimpleTMs
import dragomordor.simpletms.ui.MoveCaseMenu
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer

/**
 * Packet sent from client to server when clicking on a case slot.
 */
data class CaseSlotClickPacket(
    val visibleIndex: Int,
    val isShiftClick: Boolean
) : CustomPacketPayload {

    override fun type(): CustomPacketPayload.Type<CaseSlotClickPacket> = TYPE

    companion object {
        val ID: ResourceLocation = ResourceLocation.fromNamespaceAndPath(SimpleTMs.MOD_ID, "case_slot_click")

        val TYPE: CustomPacketPayload.Type<CaseSlotClickPacket> = CustomPacketPayload.Type(ID)

        val STREAM_CODEC: StreamCodec<FriendlyByteBuf, CaseSlotClickPacket> = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, CaseSlotClickPacket::visibleIndex,
            ByteBufCodecs.BOOL, CaseSlotClickPacket::isShiftClick,
            ::CaseSlotClickPacket
        )
    }
}

/**
 * Network registration for SimpleTMs packets.
 */
object SimpleTMsNetwork {

    /**
     * Register all packets. Call this during mod initialization.
     */
    fun register() {
        SimpleTMs.LOGGER.info("Registering SimpleTMs network packets")

        // Register client-to-server packet
        NetworkManager.registerReceiver(
            NetworkManager.Side.C2S,
            CaseSlotClickPacket.TYPE,
            CaseSlotClickPacket.STREAM_CODEC
        ) { packet, context ->
            context.queue {
                val player = context.player as? ServerPlayer ?: return@queue
                val menu = player.containerMenu as? MoveCaseMenu ?: return@queue

                SimpleTMs.LOGGER.debug("Received case slot click: index=${packet.visibleIndex}, shift=${packet.isShiftClick}")
                menu.handleCaseSlotClick(packet.visibleIndex, packet.isShiftClick)
            }
        }

        SimpleTMs.LOGGER.info("SimpleTMs network packets registered")
    }

    /**
     * Send a case slot click to the server.
     */
    fun sendCaseSlotClick(visibleIndex: Int, isShiftClick: Boolean) {
        NetworkManager.sendToServer(CaseSlotClickPacket(visibleIndex, isShiftClick))
    }
}