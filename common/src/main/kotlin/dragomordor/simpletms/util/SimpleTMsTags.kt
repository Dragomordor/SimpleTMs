package dragomordor.simpletms.util

import dragomordor.simpletms.SimpleTMs
import dragomordor.simpletms.SimpleTMs.MOD_ID
import net.minecraft.core.registries.Registries
import net.minecraft.tags.TagKey
import java.io.File

@Suppress("unused")
object SimpleTMsTags {

    // For all MoveLearnItems, with TM and TR items separated (excludes blank TM/TR items):
    @JvmField val TM_ITEMS = create("tm_items")
    @JvmField val TR_ITEMS = create("tr_items")
    // For pokemon types:
    @JvmField val TYPE_NORMAL_TM = create("type_normal_tm")
    @JvmField val TYPE_NORMAL_TR = create("type_normal_tr")
    @JvmField val TYPE_FIGHTING_TM = create("type_fighting_tm")
    @JvmField val TYPE_FIGHTING_TR = create("type_fighting_tr")
    @JvmField val TYPE_FLYING_TM = create("type_flying_tm")
    @JvmField val TYPE_FLYING_TR = create("type_flying_tr")
    @JvmField val TYPE_POISON_TM = create("type_poison_tm")
    @JvmField val TYPE_POISON_TR = create("type_poison_tr")
    @JvmField val TYPE_GROUND_TM = create("type_ground_tm")
    @JvmField val TYPE_GROUND_TR = create("type_ground_tr")
    @JvmField val TYPE_ROCK_TM = create("type_rock_tm")
    @JvmField val TYPE_ROCK_TR = create("type_rock_tr")
    @JvmField val TYPE_BUG_TM = create("type_bug_tm")
    @JvmField val TYPE_BUG_TR = create("type_bug_tr")
    @JvmField val TYPE_GHOST_TM = create("type_ghost_tm")
    @JvmField val TYPE_GHOST_TR = create("type_ghost_tr")
    @JvmField val TYPE_STEEL_TM = create("type_steel_tm")
    @JvmField val TYPE_STEEL_TR = create("type_steel_tr")
    @JvmField val TYPE_FIRE_TM = create("type_fire_tm")
    @JvmField val TYPE_FIRE_TR = create("type_fire_tr")
    @JvmField val TYPE_WATER_TM = create("type_water_tm")
    @JvmField val TYPE_WATER_TR = create("type_water_tr")
    @JvmField val TYPE_GRASS_TM = create("type_grass_tm")
    @JvmField val TYPE_GRASS_TR = create("type_grass_tr")
    @JvmField val TYPE_ELECTRIC_TM = create("type_electric_tm")
    @JvmField val TYPE_ELECTRIC_TR = create("type_electric_tr")
    @JvmField val TYPE_PSYCHIC_TM = create("type_psychic_tm")
    @JvmField val TYPE_PSYCHIC_TR = create("type_psychic_tr")
    @JvmField val TYPE_ICE_TM = create("type_ice_tm")
    @JvmField val TYPE_ICE_TR = create("type_ice_tr")
    @JvmField val TYPE_DRAGON_TM = create("type_dragon_tm")
    @JvmField val TYPE_DRAGON_TR = create("type_dragon_tr")
    @JvmField val TYPE_DARK_TM = create("type_dark_tm")
    @JvmField val TYPE_DARK_TR = create("type_dark_tr")
    @JvmField val TYPE_FAIRY_TM = create("type_fairy_tm")
    @JvmField val TYPE_FAIRY_TR = create("type_fairy_tr")
    // TODO: Custom types for GEB here
    @JvmField val TYPE_COSMIC_TM = create("type_cosmic_tm")
    @JvmField val TYPE_COSMIC_TR = create("type_cosmic_tr")
    @JvmField val TYPE_CRYSTAL_TM = create("type_crystal_tm")
    @JvmField val TYPE_CRYSTAL_TR = create("type_crystal_tr")
    @JvmField val TYPE_DIGITAL_TM = create("type_digital_tm")
    @JvmField val TYPE_DIGITAL_TR = create("type_digital_tr")
    @JvmField val TYPE_LIGHT_TM = create("type_light_tm")
    @JvmField val TYPE_LIGHT_TR = create("type_light_tr")
    @JvmField val TYPE_NUCLEAR_TM = create("type_nuclear_tm")
    @JvmField val TYPE_NUCLEAR_TR = create("type_nuclear_tr")
    @JvmField val TYPE_PLASTIC_TM = create("type_plastic_tm")
    @JvmField val TYPE_PLASTIC_TR = create("type_plastic_tr")
    @JvmField val TYPE_UNKNOWN_TM = create("type_unknown_tm")
    @JvmField val TYPE_UNKNOWN_TR = create("type_unknown_tr")
    @JvmField val TYPE_SHADOW_TM = create("type_shadow_tm")
    @JvmField val TYPE_SHADOW_TR = create("type_shadow_tr")
    @JvmField val TYPE_SLIME_TM = create("type_slime_tm")
    @JvmField val TYPE_SLIME_TR = create("type_slime_tr")
    @JvmField val TYPE_SOUND_TM = create("type_sound_tm")
    @JvmField val TYPE_SOUND_TR = create("type_sound_tr")
    @JvmField val TYPE_WIND_TM = create("type_wind_tm")
    @JvmField val TYPE_WIND_TR = create("type_wind_tr")

    // For move categories
    @JvmField val CATEGORY_PHYSICAL_TM = create("category_physical_tm")
    @JvmField val CATEGORY_PHYSICAL_TR = create("category_physical_tr")
    @JvmField val CATEGORY_STATUS_TM = create("category_status_tm")
    @JvmField val CATEGORY_STATUS_TR = create("category_status_tr")
    @JvmField val CATEGORY_SPECIAL_TM = create("category_special_tm")
    @JvmField val CATEGORY_SPECIAL_TR = create("category_special_tr")

    // Helper function to create a tag key
    private fun create(path: String) = TagKey.create(Registries.ITEM, simpletmsResource(path))

}