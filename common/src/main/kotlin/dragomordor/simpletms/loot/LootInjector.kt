package dragomordor.simpletms.loot

import dragomordor.simpletms.SimpleTMs
import dragomordor.simpletms.util.simpletmsResource
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.storage.loot.BuiltInLootTables
import net.minecraft.world.level.storage.loot.LootPool
import net.minecraft.world.level.storage.loot.LootTable
import net.minecraft.world.level.storage.loot.entries.NestedLootTable
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator

object LootInjector {

    private const val PREFIX = "injection/"

    // Helper function to create modded loot table keys
    private fun moddedLootTable(namespace: String, path: String): ResourceKey<LootTable> {
        return ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath(namespace, path))
    }

    // Vanilla loot tables
    private val vanillaInjections = hashSetOf(
        BuiltInLootTables.ABANDONED_MINESHAFT,
        BuiltInLootTables.ANCIENT_CITY,
        // ... rest of your vanilla tables
    )

    // Modded loot tables (Cobblemon, etc.)
    private val moddedInjections = hashSetOf(
        // Cobblemon loot tables
        moddedLootTable("cobblemon", "villages/village_pokecenters"),
        // Cobblemon Additions (bca) loot tables
        moddedLootTable("bca", "general/attic"),
        moddedLootTable("bca", "general/bedroom"),
        moddedLootTable("bca", "general/blacksmith"),
        moddedLootTable("bca", "general/easter_egg"),
        moddedLootTable("bca", "general/gardening"),
        moddedLootTable("bca", "general/ice_chest"),
        moddedLootTable("bca", "general/miner"),
        moddedLootTable("bca", "general/pokecenter"),
        moddedLootTable("bca", "general/workshop"),
        // Pokeloot
        moddedLootTable("pokeloot", "blocks/pokeball_loot"),
        moddedLootTable("pokeloot", "blocks/greatball_loot"),
        moddedLootTable("pokeloot", "blocks/ultraball_loot"),
        moddedLootTable("pokeloot", "blocks/masterball_loot"),
        // Add more modded tables as needed
        // moddedLootTable("othermod", "loot_table_path"),
    )

    // Combined set of all injection IDs
    private val injectionIds: Set<ResourceLocation> = buildSet {
        addAll(vanillaInjections.map { it.location() })
        addAll(moddedInjections.map { it.location() })
    }

    fun attemptInjection(id: ResourceLocation, provider: (LootPool.Builder) -> Unit): Boolean {
        if (!this.injectionIds.contains(id)) {
            return false
        }
        val resulting = this.convertToPotentialInjected(id)
        SimpleTMs.LOGGER.debug("{}: Injected {} to {}", this::class.simpleName, resulting, id)
        provider(this.injectLootPool(resulting))
        return true
    }

    private fun convertToPotentialInjected(source: ResourceLocation): ResourceLocation {
        // Include the namespace in the path to avoid collisions between mods
        return simpletmsResource("$PREFIX${source.namespace}/${source.path}")
    }

    private fun injectLootPool(resulting: ResourceLocation): LootPool.Builder {
        return LootPool.lootPool()
            .add(
                NestedLootTable
                    .lootTableReference(ResourceKey.create(Registries.LOOT_TABLE, resulting))
                    .setWeight(1)
            )
            .setBonusRolls(UniformGenerator.between(0F, 1F))
    }
}



//loot table files should now be placed at:
//```
//resources/data/simpletms/loot_table/injection/
//├── minecraft/
//│   └── chests/
//│       ├── abandoned_mineshaft.json
//│       ├── ancient_city.json
//│       └── ...
//├── cobblemon/
//│     └── villages/
//│         └── village_pokecenters.json
