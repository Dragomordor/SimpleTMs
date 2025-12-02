package dragomordor.simpletms.item.custom

import dev.architectury.registry.menu.MenuRegistry
import dragomordor.simpletms.api.MoveCaseHelper
import dragomordor.simpletms.api.MoveCaseStorage
import dragomordor.simpletms.item.SimpleTMsItem
import dragomordor.simpletms.ui.MoveCaseMenu
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.MenuProvider
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.level.Level

/**
 * The TM Case or TR Case item.
 *
 * When used (right-clicked), opens a player-specific inventory that stores
 * TMs or TRs (depending on the isTR flag). Each player has their own storage
 * that persists across sessions, similar to an Ender Chest.
 *
 * @param isTR If true, this is a TR Case. If false, this is a TM Case.
 * @param settings The item properties.
 */
class MoveCaseItem(val isTR: Boolean, settings: Properties) : SimpleTMsItem(settings) {

    /**
     * Called when the player right-clicks with this item.
     * Opens the Move Case menu.
     */
    override fun use(level: Level, player: Player, usedHand: InteractionHand): InteractionResultHolder<ItemStack> {
        val stack = player.getItemInHand(usedHand)

        if (!level.isClientSide && player is ServerPlayer) {
            // Open the menu on the server
            openMenu(player)
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide)
    }

    /**
     * Open the Move Case menu for the player.
     */
    private fun openMenu(player: ServerPlayer) {
        // Get the player's stored moves to send to client
        val storage = MoveCaseStorage.get(player)
        val storedMoves = storage.getStoredMoves(player.uuid, isTR)

        val menuProvider = object : MenuProvider {
            override fun getDisplayName(): Component {
                return if (isTR) {
                    Component.translatable("container.simpletms.tr_case")
                } else {
                    Component.translatable("container.simpletms.tm_case")
                }
            }

            override fun createMenu(
                containerId: Int,
                playerInventory: Inventory,
                player: Player
            ): AbstractContainerMenu {
                return MoveCaseMenu(containerId, playerInventory, isTR, storedMoves)
            }
        }

        // Use extended menu to pass the isTR flag and stored moves to the client
        MenuRegistry.openExtendedMenu(player, menuProvider) { buf ->
            buf.writeBoolean(isTR)
            // Write stored moves
            buf.writeVarInt(storedMoves.size)
            for (moveName in storedMoves) {
                buf.writeUtf(moveName)
            }
        }
    }

    /**
     * Add tooltip showing storage statistics for this player.
     */
    override fun appendHoverText(
        stack: ItemStack,
        context: Item.TooltipContext,
        tooltipComponents: MutableList<Component>,
        tooltipFlag: TooltipFlag
    ) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag)

        // Get total count of available moves
        val totalMoves = MoveCaseHelper.getSlotCount(isTR)

        // Try to get player-specific storage info
        // Note: On client, we can't easily access the storage, so we show a generic message
        // The actual count will be shown when hovering in-game with server context

        // Get the stored count if we have access to the player
        val storedCount = getStoredCountForTooltip()

        if (storedCount >= 0) {
            // Calculate percentage
            val percentage = if (totalMoves > 0) (storedCount * 100) / totalMoves else 0

            // Determine color based on fullness
            val color = when {
                percentage >= 100 -> ChatFormatting.GOLD      // Complete - gold
                percentage >= 75 -> ChatFormatting.GREEN      // 75%+ - green
                percentage >= 50 -> ChatFormatting.YELLOW     // 50%+ - yellow
                percentage >= 25 -> ChatFormatting.RED        // 25%+ - red
                else -> ChatFormatting.DARK_RED               // <25% - dark red
            }

            // Add storage info line
            val typeLabel = if (isTR) "TRs" else "TMs"
            tooltipComponents.add(
                Component.translatable(
                    "tooltip.simpletms.case.storage",
                    storedCount,
                    totalMoves,
                    percentage
                ).withStyle(color)
            )

            // Add completion status
            if (percentage >= 100) {
                tooltipComponents.add(
                    Component.translatable("tooltip.simpletms.case.complete")
                        .withStyle(ChatFormatting.GOLD, ChatFormatting.ITALIC)
                )
            }
        } else {
            // Can't determine storage - show total capacity
            val typeLabel = if (isTR) "TRs" else "TMs"
            tooltipComponents.add(
                Component.translatable("tooltip.simpletms.case.capacity", totalMoves)
                    .withStyle(ChatFormatting.GRAY)
            )
        }
    }

    /**
     * Try to get the stored count for the current player.
     * Returns -1 if we can't determine it (client-side without context).
     */
    private fun getStoredCountForTooltip(): Int {
        // Try to get the client player
        try {
            val minecraft = net.minecraft.client.Minecraft.getInstance()
            val player = minecraft.player ?: return -1

            // On integrated server, we might be able to get the storage
            val server = minecraft.singleplayerServer
            if (server != null) {
                val serverPlayer = server.playerList.getPlayer(player.uuid)
                if (serverPlayer != null) {
                    val storage = MoveCaseStorage.get(serverPlayer)
                    return storage.getStoredMoves(player.uuid, isTR).size
                }
            }

            // Can't access storage from client in multiplayer
            return -1
        } catch (e: Exception) {
            // Might fail on dedicated server or other edge cases
            return -1
        }
    }
}