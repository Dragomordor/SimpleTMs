package dragomordor.simpletms.neoforge

import dragomordor.simpletms.SimpleTMs.MOD_ID
import dragomordor.simpletms.SimpleTMs
import dragomordor.simpletms.item.group.SimpleTMsItemGroups
import dragomordor.simpletms.loot.LootInjector
import net.minecraft.core.registries.Registries
import net.minecraft.world.item.CreativeModeTab
import net.neoforged.api.distmarker.Dist
import net.neoforged.fml.common.Mod
import net.neoforged.fml.loading.FMLEnvironment
import net.neoforged.neoforge.common.NeoForge
import net.neoforged.neoforge.event.LootTableLoadEvent
import net.neoforged.neoforge.registries.RegisterEvent
import thedarkcolour.kotlinforforge.neoforge.forge.MOD_BUS

@Mod(MOD_ID)
object SimpleTMs {

    init {
        SimpleTMs.preinit()
        SimpleTMs.init()
        with(MOD_BUS) {
        }
        with(NeoForge.EVENT_BUS) {
            addListener(::onLootTableLoad)
        }
        registerItemGroups()
        if (FMLEnvironment.dist == Dist.CLIENT) {
            SimpleTMsClient.init()
        }
    }

    // ------------------------------------------------------------
    // Event Handlers
    // ------------------------------------------------------------

    private fun onLootTableLoad(e: LootTableLoadEvent) {
        LootInjector.attemptInjection(e.name) { builder -> e.table.addPool(builder.build()) }
    }

    private fun registerItemGroups() {
        with(MOD_BUS) {
            addListener<RegisterEvent> { event ->
                event.register(Registries.CREATIVE_MODE_TAB) { helper ->
                    SimpleTMsItemGroups.register { holder ->
                        val itemGroup = CreativeModeTab.builder()
                            .title(holder.displayName)
                            .icon(holder.displayIconProvider)
                            .displayItems(holder.entryCollector)
                            .build()
                        helper.register(holder.key, itemGroup)
                        itemGroup
                    }
                }
            }
        }

    }
}