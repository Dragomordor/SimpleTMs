package dragomordor.simpletms.fabric

import dragomordor.simpletms.SimpleTMs
import dragomordor.simpletms.item.group.SimpleTMsItemGroups
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.item.CreativeModeTab

@Suppress("unused")
class SimpleTMs : ModInitializer {
    override fun onInitialize() {
        SimpleTMs.preinit()
        SimpleTMs.init()
        registerItemGroups()
    }

    private fun registerItemGroups() {
        SimpleTMsItemGroups.register { provider ->
            Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, provider.key, FabricItemGroup.builder()
                .title(provider.displayName)
                .icon(provider.displayIconProvider)
                .displayItems(provider.entryCollector)
                .build())
        }

    }


}