# Configuration

The `main.json` configuration file is located in the `config/simpletms/` folder. Below is a list of key options available for customization:


| Option                                   | Description                                                                                         | Default Value | Range/Notes                                                                 |
|------------------------------------------|-----------------------------------------------------------------------------------------------------|---------------|-----------------------------------------------------------------------------|
| `tmMovesLearnable`                       | Allows Pokémon to learn moves from their TM move learnset.                                          | true          | true/false                                                                  |
| `eggMovesLearnable`                      | Allows Pokémon to learn moves from their egg move learnset.                                         | true          | true/false                                                                  |
| `tutorMovesLearnable`                    | Allows Pokémon to learn moves from their tutor move learnset.                                       | true          | true/false                                                                  |
| `levelMovesLearnable`                    | Allows Pokémon to learn moves gained from levelling up early using TMs/TRs.                         | false         | true/false                                                                  |
| `primaryTypeMovesLearnable`              | Allows Pokémon to learn moves that match their primary elemental type (e.g. Fire type).             | false         | true/false                                                                  |
| `secondaryTypeMovesLearnable`            | Allows Pokémon to learn moves that match their secondary elemental type (e.g. Flying).              | false         | true/false                                                                  |
| `legacyMovesLearnable`                   | Allows Pokémon to learn moves from Cobblemon’s **legacy** learnset category.                        | false         | true/false                                                                  |
| `specialMovesLearnable`                  | Allows Pokémon to learn moves from Cobblemon’s **special** learnset category.                       | false         | true/false                                                                  |
| `anyMovesLearnableTMs`                   | Allows any Pokémon to learn any move when using **TMs** (ignores normal learnset restrictions).     | false         | true/false                                                                  |
| `anyMovesLearnableTRs`                   | Allows any Pokémon to learn any move when using **TRs** (ignores normal learnset restrictions).     | false         | true/false                                                                  |
| `blankTMsUsable`                         | Allows blank TMs to be used.                                                                        | true          | true/false                                                                  |
| `blankTRsUsable`                         | Allows blank TRs to be used.                                                                        | true          | true/false                                                                  |
| `tmsUsable`                              | Allows TMs to be used (does not affect blank TMs).                                                  | true          | true/false                                                                  |
| `trsUsable`                              | Allows TRs to be used (does not affect blank TRs).                                                  | true          | true/false                                                                  |
| `tmCoolDownTicks`                        | The number of Minecraft ticks a TM will be on cooldown after use.                                   | 0             | Integer (0 to 51 840 000) – 30 days max                                     |
| `blankTMCooldownTicks`                   | The number of Minecraft ticks a blank TM will be on cooldown after use.                             | 0             | Integer (0 to 51 840 000) – 30 days max                                     |
| `blankTMBaseDurability`                  | Durability (uses) for blank TMs, based on Minecraft’s base durability scale.                        | 1             | Integer (1 to 1 024)                                                        |
| `tmBaseDurability`                       | Durability (uses) for TMs, based on Minecraft’s base durability scale.                              | 32            | Integer (1 to 1 024)                                                        |
| `tmRepairable`                           | Allows TMs to be repaired using the configured repair item (currently Diamond Block).               | true          | true/false                                                                  |
| `trStackSize`                            | Stack size for TRs and blank TRs.                                                                   | 16            | Integer (1 to 64)                                                           |
| `dropOutsideOfBattle`                    | Allows Pokémon to drop TMs and TRs when defeated **outside** of battle.                             | true          | true/false                                                                  |
| `dropInBattle`                           | Allows Pokémon to drop TMs and TRs when defeated **in battle**.                                     | true          | true/false                                                                  |
| `dropRateInBattle`                       | Drop rate of TMs/TRs in battle (0.1 = 10%).                                                         | 0.1           | 0.0–1.0 (percentage as decimal)                                             |
| `dropRateTMFractionInBattle`             | Fraction of drops that are TMs instead of TRs in battle (0.1 = 10%).                                | 0.1           | 0.0–1.0 (percentage as decimal)                                             |
| `dropRateOutsideOfBattle`                | Drop rate of TMs/TRs outside of battle.                                                             | 0.1           | 0.0–1.0 (percentage as decimal)                                             |
| `dropRateTMFractionOutsideOfBattle`      | Fraction of drops that are TMs instead of TRs outside of battle.                                    | 0.1           | 0.0–1.0 (percentage as decimal)                                             |
| `numberOfMovesToChooseFromInBattle`      | Number of random moves to choose from after defeating a Pokémon in battle.                          | 4             | Integer (1–4)                                                               |
| `numberOfMovesToChooseFromOutsideBattle` | Number of random moves to choose from after defeating a Pokémon outside of battle.                  | 1             | Integer (1–4)                                                               |
| `dropAnyMove`                            | Pokémon can drop **any** move as a TM or TR, regardless of their own learnset.                      | false         | true/false                                                                  |
| `dropPrimaryType`                        | Pokémon can drop moves that match their primary elemental type.                                     | false         | true/false                                                                  |
| `dropSecondaryType`                      | Pokémon can drop moves that match their secondary elemental type.                                   | false         | true/false                                                                  |
| `dropFromLevelList`                      | Pokémon can drop moves they learn from levelling up, up to their current level.                     | true          | true/false                                                                  |
| `dropAnyLevelMoveFromLevelList`          | Pokémon can drop any move from their level-up list, even moves learned at a higher level.           | false         | true/false                                                                  |
| `dropFromTmMoveList`                     | Pokémon can drop moves from their TM move learnset.                                                 | true          | true/false                                                                  |
| `dropFromTutorMoveList`                  | Pokémon can drop moves from their tutor move learnset.                                              | true          | true/false                                                                  |
| `dropFromEggMoveList`                    | Pokémon can drop moves from their egg move learnset.                                                | true          | true/false                                                                  |
| `dropFromLegacyMoveList`                 | Pokémon can drop moves from Cobblemon’s **legacy** learnset category.                               | false         | true/false                                                                  |
| `dropFromSpecialMoveList`                | Pokémon can drop moves from Cobblemon’s **special** learnset category.                              | false         | true/false                                                                  |
| `showPokemonThatCanLearnMove`            | Shows a scrollable tooltip on TMs and TRs listing all Pokémon that can learn the move.              | true          | true/false                                                                  |
| `allowCustomMovesAndEditing`             | Allows adding custom moves and editing/removing TM/TR entries via config and JSON files. See *Note. | false         | true/false                                                                  |

* *Note: This option is only for those who know what they are doing. To add items, check the SimpleTMs_GEB_Compatability.zip for structure. To remove TM and TR items, set this to true and remove entries from the `config/simpletms/moves/default_tr_moves.json` and `config/simpletms/moves/default_tm_moves.json` files. This will prevent the items from being added to the game, but it will not remove existing items from the world or player inventories.This also prevents you from accessing servers with those items. For servers that remove entries, be aware tha players can still see the items in jei if they did not remove it from their configs. THey cannot get these items however, so it is purely visual*

## Excluding Moves

To exclude moves from either Pokémon drops, Blank TMs, or TM Learning, add the move to the relevant excluded moves json file located in the `config/simpletms/moves/` folder. This file contains a list of moves to exclude from the mod's functionality.
For example, to exclude the move Ember from pokemon drops, add the following to the `excluded_moves_from_pokemon_drops.json` file:

```json
[
  {
    "moveName": "ember"
  }
]
```

# GEB Compatibility

Download the `SimpleTMs_GEB_Compatability.zip` from the releases page or from its [CurseForge](link)/ [Modrinth](link) page. To use the GEB compatibility pack, follow these steps:
1) Add it as a resourcepack (drag the zip into your resourcepack folder) and activate it.
2) Add it as a datapack in your world (manually or using the Global Datapack mod*)
3) Copy the config files from the `SimpleTMs_GEB_Compatability.zip` into your `config/simpletms/` folder. 
4) Make sure you install GEB and Gravelmon (or any other GEB using pack I guess)
   *I recommend using the mod "Global Datapacks" to make sure the datapack is loaded into each world without needing to add it yourself. Then just make sure to actually activate the resourcepack!

If using this patch for Gravelmon, make sure you have the following installed:
- Midnight Lib
- Gravel's Extended Battles
- Gravelmon

Usage of this has NOT been explicitly developed for servers, and usage in servers should be seen as experimental. 
If you are using this on a server, make sure each client has the resourcepack installed and activated AND that their configs are set to the same as the server.