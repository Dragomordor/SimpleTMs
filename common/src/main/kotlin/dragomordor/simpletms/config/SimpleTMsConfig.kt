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
    var tmMovesLearnable: Boolean = true

    @NodeCategory(Category.Learnable)
    var eggMovesLearnable: Boolean = true

    @NodeCategory(Category.Learnable)
    var tutorMovesLearnable: Boolean = true

    @NodeCategory(Category.Learnable)
    var levelMovesLearnable: Boolean = false

    @NodeCategory(Category.Learnable)
    var primaryTypeMovesLearnable = false

    @NodeCategory(Category.Learnable)
    var secondaryTypeMovesLearnable = false

    @NodeCategory(Category.Learnable)
    var anyMovesLearnableTMs: Boolean = false

    @NodeCategory(Category.Learnable)
    var anyMovesLearnableTRs: Boolean = false

    @NodeCategory(Category.Usable)
    var blankTMsUsable: Boolean = true

    @NodeCategory(Category.Usable)
    var blankTRsUsable: Boolean = true

    @NodeCategory(Category.Usable)
    var tmsUsable: Boolean = true

    @NodeCategory(Category.Usable)
    var trsUsable: Boolean = true

    @NodeCategory(Category.Cooldown)
    @IntConstraint(min = 0, max = 51840000)
    var tmCoolDownTicks: Int = 0

    @NodeCategory(Category.Cooldown)
    @IntConstraint(min = 0, max = 51840000)
    var blankTMCooldownTicks: Int = 0

    @NodeCategory(Category.ItemProperties)
    @IntConstraint(min = 0, max = 1024)
    var blankTMBaseDurability: Int = 1

    @NodeCategory(Category.ItemProperties)
    @IntConstraint(min = 0, max = 1024)
    var tmBaseDurability: Int = 32

    @NodeCategory(Category.ItemProperties)
    var tmRepairable: Boolean = true

    @NodeCategory(Category.ItemProperties)
    @IntConstraint(min = 0, max = 64)
    var trStackSize: Int = 16

    @NodeCategory(Category.DropRate)
    var dropOutsideOfBattle: Boolean = true

    @NodeCategory(Category.DropRate)
    var dropInBattle: Boolean = true

    @NodeCategory(Category.DropRate)
    @DoubleConstraint(min = 0.0, max = 1.0)
    var dropRateInBattle: Double = 0.1

    @NodeCategory(Category.DropRate)
    @DoubleConstraint(min = 0.0, max = 1.0)
    var dropRateTMFractionInBattle: Double = 0.1

    @NodeCategory(Category.DropRate)
    @DoubleConstraint(min = 0.0, max = 1.0)
    var dropRateOutsideOfBattle: Double = 0.1

    @NodeCategory(Category.DropRate)
    @DoubleConstraint(min = 0.0, max = 1.0)
    var dropRateTMFractionOutsideOfBattle: Double = 0.1

    @NodeCategory(Category.DropRate)
    @IntConstraint(min = 1, max = 4)
    var numberOfMovesToChooseFromInBattle: Int = 4

    @NodeCategory(Category.DropRate)
    @IntConstraint(min = 1, max = 4)
    var numberOfMovesToChooseFromOutsideBattle: Int = 1

    @NodeCategory(Category.DropRate)
    var dropAnyMove: Boolean = false

    @NodeCategory(Category.DropRate)
    var dropPrimaryType: Boolean = false

    @NodeCategory(Category.DropRate)
    var dropSecondaryType: Boolean = false

    @NodeCategory(Category.DropRate)
    var dropFromLevelList: Boolean = true

    @NodeCategory(Category.DropRate)
    var dropAnyLevelMoveFromLevelList = false

    @NodeCategory(Category.DropRate)
    var dropFromTmMoveList: Boolean = true

    @NodeCategory(Category.DropRate)
    var dropFromTutorMoveList: Boolean = true

    @NodeCategory(Category.DropRate)
    var dropFromEggMoveList: Boolean = true

    @NodeCategory(Category.Experimental)
    var allowCustomMovesAndEditing: Boolean = false
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
    ItemProperties,
    Experimental,
}

annotation class DoubleConstraint(val min: Double, val max: Double)