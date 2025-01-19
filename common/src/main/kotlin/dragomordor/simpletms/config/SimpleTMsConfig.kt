package dragomordor.simpletms.config

import com.cobblemon.mod.common.config.constraint.IntConstraint
import com.cobblemon.mod.common.util.adapters.IntRangeAdapter
import com.google.gson.GsonBuilder

class SimpleTMsConfig {

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
    var TMCoolDownTicks: Int = 500

    @NodeCategory(Category.Cooldown)
    @IntConstraint(min = 0, max = 51840000)
    var blankTMCooldownTicks: Int = 500

    @NodeCategory(Category.Stacks)
    @IntConstraint(min = 0, max = 64)
    var TMStackSize: Int = 1

    @NodeCategory(Category.Stacks)
    @IntConstraint(min = 0, max = 64)
    var TRStackSize: Int = 16

    @NodeCategory(Category.DropRate)
    @DoubleConstraint(min = 0.0, max = 1.0)
    var DropRateTR: Double = 0.1

    @NodeCategory(Category.DropRate)
    @DoubleConstraint(min = 0.0, max = 1.0)
    var DropRateTMtoTRRatio: Double = 0.1

    @NodeCategory(Category.DropRate)
    var DropAny: Boolean = false

    @NodeCategory(Category.DropRate)
    var DropPrimaryType: Boolean = false

    @NodeCategory(Category.DropRate)
    var DropSecondaryType: Boolean = false

    @NodeCategory(Category.DropRate)
    var DropFromLevelList: Boolean = true

    @NodeCategory(Category.DropRate)
    var DropAnyLevelMoveFromLevelList = false

    @NodeCategory(Category.DropRate)
    var DropFromTmMoveList: Boolean = true

    @NodeCategory(Category.DropRate)
    var DropFromTutorMoveList: Boolean = true

    @NodeCategory(Category.DropRate)
    var DropFromEggMoveList: Boolean = true
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

annotation class DoubleConstraint(val min: Double, val max: Double)
