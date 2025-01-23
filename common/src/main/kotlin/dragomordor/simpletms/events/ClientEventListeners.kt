package dragomordor.simpletms.events

import dev.architectury.event.EventResult
import dev.architectury.event.events.client.ClientScreenInputEvent
import dragomordor.simpletms.util.Pages
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen

object ClientEventListeners: EventHandler {
    override fun registerListeners() {
        ClientScreenInputEvent.MOUSE_SCROLLED_PRE.register(ClientEventListeners::onMouseScrolled)
    }

    private fun onMouseScrolled(minecraft: Minecraft?, screen: Screen?, d: Double, d1: Double, d2: Double, d3: Double): EventResult? {

        if (screen is AbstractContainerScreen<*> && screen.menu != null && minecraft != null) {
            // Allow scrolling of MoveLearnItem tooltip
            scrollUpOrDownTooltip(d3)
        }
        // Normal event handling
        return EventResult.pass()
    }


    // ------------------------------------------------------
    // Helper methods
    // ------------------------------------------------------

    private fun scrollUpOrDownTooltip(direction: Double) {
        Pages.CURRENT_PAGE = when {
            direction < 0 && Pages.CURRENT_PAGE == Pages.TOTAL_PAGES - 1 -> 0
            direction < 0 -> Pages.CURRENT_PAGE + 1
            direction > 0 && Pages.CURRENT_PAGE == 0 -> Pages.TOTAL_PAGES - 1
            direction > 0 -> Pages.CURRENT_PAGE - 1
            else -> Pages.CURRENT_PAGE
        }
    }
}