package dragomordor.simpletms.config

import com.cobblemon.mod.common.config.constraint.IntConstraint
import com.cobblemon.mod.common.util.adapters.IntRangeAdapter
import com.google.gson.GsonBuilder

class SimpleTMsConfig {

    //TODO: Implement config options

    companion object {
        val GSON = GsonBuilder()
            .disableHtmlEscaping()
            .setPrettyPrinting()
            .registerTypeAdapter(IntRange::class.java, IntRangeAdapter)
            .create()
    }

    @NodeCategory(Category.Learnable)
    var TMMovesLearnable: Boolean = true

    @NodeCategory(Category.Learnable)
    var eggMovesLearnable: Boolean = true

    @NodeCategory(Category.Learnable)
    var tutorMovesLearnable: Boolean = true

    @NodeCategory(Category.Learnable)
    var levelMovesLearnable: Boolean = false

    @NodeCategory(Category.Learnable)
    var anyMovesLearnable: Boolean = false

    @NodeCategory(Category.Usable)
    var blankTMsUsable: Boolean = true

    @NodeCategory(Category.Usable)
    var blankTRsUsable: Boolean = true

    @NodeCategory(Category.Usable)
    var TMsUsable: Boolean = true

    @NodeCategory(Category.Usable)
    var TRsUsable: Boolean = true

    @NodeCategory(Category.Cooldown)
    @IntConstraint(min = 0, max = 51840000)
    var TMCoolDownTicks: Int = 6000

    @NodeCategory(Category.Cooldown)
    @IntConstraint(min = 0, max = 51840000)
    var blankTMCooldownTicks: Int = 6000

    @NodeCategory(Category.Stacks)
    @IntConstraint(min = 0, max = 64)
    var TMStackSize: Int = 1

    @NodeCategory(Category.Stacks)
    @IntConstraint(min = 0, max = 64)
    var TRStackSize: Int = 16

    @NodeCategory(Category.DropRate)
    @FloatConstraint(min = 0.0, max = 1.0)
    var TRDropRate: Float = 0.1f

    @NodeCategory(Category.DropRate)
    @FloatConstraint(min = 0.0, max = 1.0)
    var TMDropRate: Float = 0.001f

    @NodeCategory(Category.DropRate)
    var pokemonDropTMorTRofType: Boolean = true

    @NodeCategory(Category.DropRate)
    var pokemonDropTMorTRfromLevelList: Boolean = true

    @NodeCategory(Category.DropRate)
    var pokemonDropTMorTRfromTMMoveList: Boolean = true

    @NodeCategory(Category.DropRate)
    var pokemonDropTMorTRfromTutorMoveList: Boolean = true

    @NodeCategory(Category.DropRate)
    var pokemonDropTMorTRfromEggMoveList: Boolean = true
}


@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class NodeCategory(
    val category: Category
)

enum class Category {
    Learnable,
    Usable,
    DropRate,
    Cooldown,
    Stacks
}

annotation class FloatConstraint(val min: Double, val max: Double)
