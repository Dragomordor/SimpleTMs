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
 */
class MoveCaseItem(val isTR: Boolean, settings: Properties) : SimpleTMsItem(settings) {

    override fun use(level: Level, player: Player, usedHand: InteractionHand): InteractionResultHolder<ItemStack> {
        val stack = player.getItemInHand(usedHand)

        if (!level.isClientSide && player is ServerPlayer) {
            openMenu(player)
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide)
    }

    private fun openMenu(player: ServerPlayer) {
        val storage = MoveCaseStorage.get(player)
        // Get quantities map instead of just set of names
        val storedMoves = storage.getStoredQuantities(player.uuid, isTR)

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

        MenuRegistry.openExtendedMenu(player, menuProvider) { buf ->
            buf.writeBoolean(isTR)
            // Write stored moves with quantities
            buf.writeVarInt(storedMoves.size)
            for ((moveName, quantity) in storedMoves) {
                buf.writeUtf(moveName)
                buf.writeVarInt(quantity)
            }
        }
    }

    override fun appendHoverText(
        stack: ItemStack,
        context: Item.TooltipContext,
        tooltipComponents: MutableList<Component>,
        tooltipFlag: TooltipFlag
    ) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag)

        val totalMoves = MoveCaseHelper.getSlotCount(isTR)
        val storedCount = getStoredCountForTooltip()

        if (storedCount >= 0) {
            val percentage = if (totalMoves > 0) (storedCount * 100) / totalMoves else 0

            val color = when {
                percentage >= 100 -> ChatFormatting.GOLD
                percentage >= 75 -> ChatFormatting.GREEN
                percentage >= 50 -> ChatFormatting.YELLOW
                percentage >= 25 -> ChatFormatting.RED
                else -> ChatFormatting.DARK_RED
            }

            tooltipComponents.add(
                Component.translatable("tooltip.simpletms.case.storage", storedCount, totalMoves, percentage)
                    .withStyle(color)
            )

            if (percentage >= 100) {
                tooltipComponents.add(
                    Component.translatable("tooltip.simpletms.case.complete")
                        .withStyle(ChatFormatting.GOLD, ChatFormatting.ITALIC)
                )
            }
        } else {
            tooltipComponents.add(
                Component.translatable("tooltip.simpletms.case.capacity", totalMoves)
                    .withStyle(ChatFormatting.GRAY)
            )
        }
    }

    private fun getStoredCountForTooltip(): Int {
        try {
            val minecraft = net.minecraft.client.Minecraft.getInstance()
            val player = minecraft.player ?: return -1

            val server = minecraft.singleplayerServer
            if (server != null) {
                val serverPlayer = server.playerList.getPlayer(player.uuid)
                if (serverPlayer != null) {
                    val storage = MoveCaseStorage.get(serverPlayer)
                    return storage.getStoredMoves(player.uuid, isTR).size
                }
            }

            return -1
        } catch (e: Exception) {
            return -1
        }
    }
}