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
    var TMCoolDownTicks: Int = 600

    @NodeCategory(Category.Cooldown)
    @IntConstraint(min = 0, max = 51840000)
    var blankTMCooldownTicks: Int = 0

    @NodeCategory(Category.ItemProperties)
    @IntConstraint(min = 0, max = 1024)
    var blankTMBaseDurability: Int = 1

    @NodeCategory(Category.ItemProperties)
    @IntConstraint(min = 0, max = 1024)
    var blankTRBaseDurability: Int = 1

    @NodeCategory(Category.ItemProperties)
    @IntConstraint(min = 0, max = 1024)
    var TMBaseDurability: Int = 32

    @NodeCategory(Category.DropRate)
    var DropOutsideOfBattle: Boolean = true

    @NodeCategory(Category.ItemProperties)
    @IntConstraint(min = 0, max = 64)
    var TRStackSize: Int = 16

    @NodeCategory(Category.DropRate)
    @DoubleConstraint(min = 0.0, max = 1.0)
    var DropRateTRInBattle: Double = 0.1

    @NodeCategory(Category.DropRate)
    @DoubleConstraint(min = 0.0, max = 1.0)
    var DropRateTMtoTRRatioInBattle: Double = 0.1

    @NodeCategory(Category.DropRate)
    @DoubleConstraint(min = 0.0, max = 1.0)
    var DropRateTROutsideOfBattle: Double = 0.1

    @NodeCategory(Category.DropRate)
    @DoubleConstraint(min = 0.0, max = 1.0)
    var DropRateTMtoTRRatioOutsideOfBattle: Double = 0.1

    @NodeCategory(Category.DropRate)
    @IntConstraint(min = 1, max = 4)
    var NumberOfMovesToChooseFromInBattle: Int = 4

    @NodeCategory(Category.DropRate)
    @IntConstraint(min = 1, max = 4)
    var NumberOfMovesToChooseFromoutsideBattle: Int = 1

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
    ItemProperties
}

annotation class DoubleConstraint(val min: Double, val max: Double)
