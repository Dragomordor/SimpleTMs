package dragomordor.simpletms.forge

import com.cobblemon.mod.forge.event.ForgePlatformEventHandler
import dragomordor.simpletms.SimpleTMs
import dragomordor.simpletms.loot.LootInjector
import net.minecraft.core.registries.Registries
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.LootTableLoadEvent
import net.minecraftforge.fml.DistExecutor
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent
import net.minecraftforge.registries.DeferredRegister
import thedarkcolour.kotlinforforge.forge.MOD_BUS


@Mod(SimpleTMs.MOD_ID)
class SimpleTMs {
    private val commandArgumentTypes = DeferredRegister.create(Registries.COMMAND_ARGUMENT_TYPE, SimpleTMs.MOD_ID)
    private val queuedWork = arrayListOf<() -> Unit>()

    init {
        with(MOD_BUS) {
            this@SimpleTMs.commandArgumentTypes.register(this)
            addListener(this@SimpleTMs::preinit)
            addListener(this@SimpleTMs::init)
        }
        with(MinecraftForge.EVENT_BUS) {
            addListener(::onLootTableLoad)
        }
        ForgePlatformEventHandler.register()
        DistExecutor.safeRunWhenOn(Dist.CLIENT) {
            DistExecutor.SafeRunnable(SimpleTMsClient::init)
        }
    }

    private fun preinit(any: Any) {
        SimpleTMs.preinit()
    }

    private fun init(event: FMLCommonSetupEvent) {
        event.enqueueWork{
            this.queuedWork.forEach {it.invoke()}
        }
        SimpleTMs.init()
    }

    private fun onLootTableLoad(e: LootTableLoadEvent) {
        LootInjector.attemptInjection(e.name) { builder -> e.table.addPool(builder.build()) }
    }



}



//@Mod(MOD_ID)
//object SimpleTMs {
//
//    init {
//        with(MOD_BUS) {
//            addListener(this@SimpleTMs::preinit)
//            addListener(this@SimpleTMs::init)
//            registerItemGroups(MOD_BUS)
//        }
//        with(MinecraftForge.EVENT_BUS) {
//            addListener(::onLootTableLoad)
//        }
////        if (FMLEnvironment.dist == Dist.CLIENT) {
////            SimpleTMsClient.init()
////        }
//        DistExecutor.safeRunWhenOn(Dist.CLIENT) {
//            DistExecutor.SafeRunnable { SimpleTMsClient.init() }
//        }
//    }
//
//    private fun preinit(any: Any) {
//        SimpleTMs.preinit()
//
//    }
//
//    private fun init(any: Any) {
//        SimpleTMs.init()
//    }
//
//    // ------------------------------------------------------------
//    // Event Handlers
//    // ------------------------------------------------------------
//
//    private fun onLootTableLoad(e: LootTableLoadEvent) {
//        LootInjector.attemptInjection(e.name) { builder -> e.table.addPool(builder.build()) }
//    }
//
////    private fun registerItemGroups() {
////
////        with (MOD_BUS) {
////            addListener<RegisterEvent> { event ->
////                event.register(Registries.CREATIVE_MODE_TAB) { helper ->
////                    SimpleTMsItemGroups.register { holder ->
////                        val itemGroup = CreativeModeTab.builder()
////                            .title(holder.displayName)
////                            .icon(holder.displayIconProvider)
////                            .displayItems(holder.entryCollector)
////                            .build()
////                        helper.register(holder.key, itemGroup)
////                        itemGroup
////                    }
////                }
////            }
////        }
////    }
//
//    private fun registerItemGroups(modBus: IEventBus) {
//        modBus.addListener<RegisterEvent> { event ->
//            event.register(Registries.CREATIVE_MODE_TAB) { helper ->
//                SimpleTMsItemGroups.register { holder ->
//                    val itemGroup = CreativeModeTab.builder()
//                        .title(holder.displayName)
//                        .icon(holder.displayIconProvider)
//                        .displayItems(holder.entryCollector)
//                        .build()
//                    helper.register(holder.key, itemGroup)
//                    itemGroup
//                }
//            }
//        }
//    }
//
//}