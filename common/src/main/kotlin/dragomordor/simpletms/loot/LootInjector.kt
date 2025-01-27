package dragomordor.simpletms.loot

import dragomordor.simpletms.SimpleTMs
import dragomordor.simpletms.util.simpletmsResource
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.storage.loot.BuiltInLootTables
import net.minecraft.world.level.storage.loot.LootPool
import net.minecraft.world.level.storage.loot.LootTable
import net.minecraft.world.level.storage.loot.entries.LootTableReference
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator


// Based off of the Cobblemon mod's loot injector
object LootInjector {

    private const val PREFIX = "injection/"

    private val injections = hashSetOf(

        // Chest related
        BuiltInLootTables.ABANDONED_MINESHAFT,
        BuiltInLootTables.ANCIENT_CITY,
        BuiltInLootTables.BASTION_BRIDGE,
        BuiltInLootTables.BASTION_OTHER,
        BuiltInLootTables.BASTION_TREASURE,
        BuiltInLootTables.DESERT_PYRAMID,
        BuiltInLootTables.END_CITY_TREASURE,
        BuiltInLootTables.IGLOO_CHEST,
        BuiltInLootTables.JUNGLE_TEMPLE,
        BuiltInLootTables.NETHER_BRIDGE,
        BuiltInLootTables.PILLAGER_OUTPOST,
        BuiltInLootTables.RUINED_PORTAL,
        BuiltInLootTables.SHIPWRECK_TREASURE,
        BuiltInLootTables.SIMPLE_DUNGEON,
        BuiltInLootTables.SPAWN_BONUS_CHEST,
        BuiltInLootTables.STRONGHOLD_CORRIDOR,
        BuiltInLootTables.STRONGHOLD_CROSSING,
        BuiltInLootTables.STRONGHOLD_LIBRARY,
        BuiltInLootTables.UNDERWATER_RUIN_BIG,
        BuiltInLootTables.UNDERWATER_RUIN_SMALL,
        BuiltInLootTables.VILLAGE_DESERT_HOUSE,
        BuiltInLootTables.VILLAGE_PLAINS_HOUSE,
        BuiltInLootTables.VILLAGE_SAVANNA_HOUSE,
        BuiltInLootTables.VILLAGE_SNOWY_HOUSE,
        BuiltInLootTables.VILLAGE_TAIGA_HOUSE,
        BuiltInLootTables.VILLAGE_TEMPLE,
        BuiltInLootTables.WOODLAND_MANSION,


        // Put all the loot tables here that are unused
//        BuiltInLootTables.ARMORER_GIFT,
//        BuiltInLootTables.ANCIENT_CITY_ICE_BOX,
//        BuiltInLootTables.DESERT_PYRAMID_ARCHAEOLOGY,
//        BuiltInLootTables.DESERT_WELL_ARCHAEOLOGY,
//        BuiltInLootTables.EQUIPMENT_TRIAL_CHAMBER,
//        BuiltInLootTables.EQUIPMENT_TRIAL_CHAMBER_MELEE,
//        BuiltInLootTables.EQUIPMENT_TRIAL_CHAMBER_RANGED,
//        BuiltInLootTables.FISHING,
//        BuiltInLootTables.FISHING_JUNK,
//        BuiltInLootTables.FISHING_FISH,
//        BuiltInLootTables.FISHING_TREASURE,
//        BuiltInLootTables.FISHERMAN_GIFT,
//        BuiltInLootTables.FLETCHER_GIFT,
//        BuiltInLootTables.JUNGLE_TEMPLE_DISPENSER,
//        BuiltInLootTables.LIBRARIAN_GIFT,
//        BuiltInLootTables.LEATHERWORKER_GIFT,
//        BuiltInLootTables.MASON_GIFT,
//        BuiltInLootTables.OCEAN_RUIN_COLD_ARCHAEOLOGY,
//        BuiltInLootTables.OCEAN_RUIN_WARM_ARCHAEOLOGY,
//        BuiltInLootTables.PANDA_SNEEZE,
//        BuiltInLootTables.PIGLIN_BARTERING,

//        BuiltInLootTables.SHEPHERD_GIFT,
//        BuiltInLootTables.SHIPWRECK_MAP,
//        BuiltInLootTables.SHIPWRECK_SUPPLY,
//        BuiltInLootTables.SNIFFER_DIGGING,
//        BuiltInLootTables.SPAWNER_OMINOUS_TRIAL_CHAMBER_CONSUMABLES,
//        BuiltInLootTables.SPAWNER_OMINOUS_TRIAL_CHAMBER_KEY,
//        BuiltInLootTables.SPAWNER_TRIAL_CHAMBER_KEY,
//        BuiltInLootTables.SPAWNER_TRIAL_CHAMBER_CONSUMABLES,
//        BuiltInLootTables.SPAWNER_TRIAL_ITEMS_TO_DROP_WHEN_OMINOUS,
//        BuiltInLootTables.STRONGHOLD_CROSSING,
//        BuiltInLootTables.STRONGHOLD_LIBRARY,
//        BuiltInLootTables.TOOLSMITH_GIFT,
//        BuiltInLootTables.TRIAL_CHAMBERS_REWARD,
//        BuiltInLootTables.TRIAL_CHAMBERS_SUPPLY,
//        BuiltInLootTables.TRIAL_CHAMBERS_CORRIDOR,
//        BuiltInLootTables.TRIAL_CHAMBERS_ENTRANCE,
//        BuiltInLootTables.TRIAL_CHAMBERS_INTERSECTION,
//        BuiltInLootTables.TRAIL_RUINS_ARCHAEOLOGY_COMMON,
//        BuiltInLootTables.TRAIL_RUINS_ARCHAEOLOGY_RARE,
//        BuiltInLootTables.TRIAL_CHAMBERS_CHAMBER_DISPENSER,
//        BuiltInLootTables.TRIAL_CHAMBERS_CORRIDOR_DISPENSER,
//        BuiltInLootTables.TRIAL_CHAMBERS_CORRIDOR_POT,
//        BuiltInLootTables.TRIAL_CHAMBERS_INTERSECTION_BARREL,
//        BuiltInLootTables.TRIAL_CHAMBERS_REWARD_COMMON,
//        BuiltInLootTables.TRIAL_CHAMBERS_REWARD_RARE,
//        BuiltInLootTables.TRIAL_CHAMBERS_REWARD_UNIQUE,
//        BuiltInLootTables.TRIAL_CHAMBERS_REWARD_OMINOUS,
//        BuiltInLootTables.TRIAL_CHAMBERS_REWARD_OMINOUS_COMMON,
//        BuiltInLootTables.TRIAL_CHAMBERS_REWARD_OMINOUS_RARE,
//        BuiltInLootTables.TRIAL_CHAMBERS_REWARD_OMINOUS_UNIQUE,
//        BuiltInLootTables.TRIAL_CHAMBERS_WATER_DISPENSER,

//        BuiltInLootTables.VILLAGE_ARMORER,
//        BuiltInLootTables.VILLAGE_BUTCHER,
//        BuiltInLootTables.VILLAGE_CARTOGRAPHER,
//        BuiltInLootTables.VILLAGE_FISHER,
//        BuiltInLootTables.VILLAGE_FLETCHER,
//        BuiltInLootTables.VILLAGE_MASON,
//        BuiltInLootTables.VILLAGE_SHEPHERD,
//        BuiltInLootTables.VILLAGE_TANNERY,
//        BuiltInLootTables.VILLAGE_TOOLSMITH,
//        BuiltInLootTables.VILLAGE_WEAPONSMITH,
//        BuiltInLootTables.WEAPONSMITH_GIFT,
//        BuiltInLootTables.PANDA_SNEEZE

    )

//    private val injectionIds = injections.map {it.location()}.toSet()
    private val injectionIds = injections.map {it.path}.toSet()

    /**
     * Attempts to inject a SimpleTMs injection loot table to a loot table being loaded.
     * This will automatically query the existence of an injection.
     *
     * @param id The [ResourceLocation] of the loot table being loaded.
     * @param provider The job invoked if the injection is possible, this is what the platform needs to do to append the loot table.
     * @return If the injection was made.
     */
    fun attemptInjection(id: ResourceLocation, provider: (LootPool.Builder) -> Unit): Boolean {
        if (!this.injections.contains(id)) {
            return false
        }
        val resulting = this.convertToPotentialInjected(id)
        SimpleTMs.LOGGER.debug("{}: Injected {} to {}", this::class.simpleName, resulting, id)
        provider(this.injectLootPool(resulting))
        return true
    }


    /**
     * Takes a source ID and converts it into the target injection.
     *
     * @param source The [ResourceLocation] of the base loot table.
     * @return The [ResourceLocation] for the expected Cobblemon injection.
     */
    private fun convertToPotentialInjected(source: ResourceLocation): ResourceLocation {
        return simpletmsResource("$PREFIX${source.path}")
    }

    /**
     * Creates a loot pool builder with our injection.
     *
     * @param resulting The [ResourceLocation] for our injection table.
     * @return A [LootPool.Builder] with the [resulting] table.
     */
//    private fun injectLootPool(resulting: ResourceLocation): LootPool.Builder {
//        return LootPool.lootPool()
//            .add(
//                LootTable.N
//                    .lootTableReference(ResourceKey.create(Registries.LOOT_TABLE, resulting))
//                    .setWeight(1)
//            )
//            .setBonusRolls(UniformGenerator.between(0F, 1F))
//    }

//    private fun injectLootPool(resulting: ResourceLocation): LootPool.Builder {
//        return LootPool.lootPool()
//            .add(
//                NestedLootTable
//                    .lootTableReference(ResourceKey.create(Registries.LOOT_TABLE, resulting))
//                    .setWeight(1)
//            )
//            .setBonusRolls(UniformGenerator.between(0F, 1F))
//    }

    private fun injectLootPool(resulting: ResourceLocation): LootPool.Builder {
        return LootPool.lootPool()
            .add(
                LootTableReference.lootTableReference(resulting)
                    .setWeight(1)
            )
            .setBonusRolls(UniformGenerator.between(0F, 1F))
    }


}