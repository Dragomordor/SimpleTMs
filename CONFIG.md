# Configuration

The `main.json` configuration file is located in the `config/simpletms/` folder. Below is a list of key options available for customization:

| Option                                   | Description                                                                               | Default Value | Range/Notes                                                               |
|------------------------------------------|-------------------------------------------------------------------------------------------|---------------|---------------------------------------------------------------------------|
| `tmMovesLearnable`                       | Allows Pokémon to learn moves from their TM move learnset.                                | true          | true/false                                                                |
| `eggMovesLearnable`                      | Allows Pokémon to learn moves from their egg move learnset.                               | true          | true/false                                                                |
| `tutorMovesLearnable`                    | Allows Pokémon to learn moves from their tutor move learnset.                             | true          | true/false                                                                |
| `levelMovesLearnable`                    | Makes Pokémon able to learn moves gained from leveling up early using TMs.                | false         | true/false                                                                |
| `primaryTypeMovesLearnable`              | Allows Pokémon to learn moves from their primary elemental type (e.g. Fire type).         | false         | true/false                                                                |
| `secondaryTypeMovesLearnable`            | Allows Pokémon to learn moves from their secondary elemental type (e.g. Flying).          | false         | true/false                                                                |
| `anyMovesLearnable`                      | Allows any Pokémon to learn any move.                                                     | false         | true/false                                                                |
| `blankTMsUsable`                         | Allows blank TMs to be used.                                                              | true          | true/false                                                                |
| `blankTRsUsable`                         | Allows blank TRs to be used.                                                              | true          | true/false                                                                |
| `tmsUsable`                              | Allows TMs to be used  (doesn't affect Blank TMs)                                         | true          | true/false                                                                |
| `trsUsable`                              | Allows TRs to be used  (doesn't affect Blank TRs)                                         | true          | true/false                                                                |
| `tmCoolDownTicks`                        | The number of Minecraft ticks the TM will be on cooldown after use.                       | 0             | Integer (0 to 51840000) - 30 days max                                     |
| `blankTMCoolDownTicks`                   | The number of Minecraft ticks the blank TM will be on cooldown after use.                 | 0             | Integer (0 to 51840000) - 30 days max                                     |
| `tmBaseDurability`                       | Specifies the durability (uses) for TMs based on Minecraft's base durability.             | 32            | Integer (1 to 1024)                                                       |
| `blankTMBaseDurability`                  | Specifies the durability (uses) for blank TMs based on Minecraft's base durability.       | 1             | Integer (1 to 1024)                                                       |
| `blankTRBaseDurability`                  | Specifies the durability (uses) for blank TRs based on Minecraft's base durability.       | 1             | Integer (1 to 1024)                                                       |
| `trStackSize`                            | The stack size for TRs and blank TRs.                                                     | 16            | Integer (1 to 64)                                                         |
| `tmStackSize`                            | The stack size for TMs and blank TMs.                                                     | 16            | Integer (1 to 64)                                                         |
| `dropOutsideOfBattle`                    | Allows Pokémon to drop TMs and TRs when defeated outside of battle.                       | true          | true/false                                                                |
| `dropInBattle`                           | Allows Pokémon to drop TMs and TRs when defeated in battle.                               | true          | true/false                                                                |
| `dropRateInBattle`                       | The drop rate (%) of TMs and TRs in battle (0.1 = 10%).                                   | 0.1           | 0.0-1.0 (Percentage as decimal)                                           |
| `dropRateOutsideOfBattle`                | The drop rate (%) of TMs and TRs outside of battle.                                       | 0.1           | 0.0-1.0 (Percentage as decimal)                                           |
| `dropRateTMFractionInBattle`             | The fraction of drops that are TMs instead of TRs in battle. (0.1 = 10%)                  | 0.1           | 0.0-1.0 (Percentage as decimal)                                           |
| `dropRateTMFractionOutsideOfBattle`      | The fraction of drops that are TMs instead of TRs outside of battle.                      | 0.1           | 0.0-1.0 (Percentage as decimal)                                           |
| `numberOfMovesToChooseFromInBattle`      | Number of random moves to choose from after defeating Pokémon in battle.                  | 4             | Integer (1-4)                                                             |
| `numberOfMovesToChooseFromOutsideBattle` | Number of random moves to choose from after defeating Pokémon outside of battle.          | 1             | Integer (1-4)                                                             |
| `dropAnyMove`                            | Pokémon can drop any move as a TM or TR, regardless of the Pokémon.                       | false         | true/false                                                                |
| `dropPrimaryType`                        | Pokémon can drop moves from their primary elemental type (e.g. Fire type).                | false         | true/false                                                                |
| `dropSecondaryType`                      | Pokémon can drop moves from their secondary elemental type (e.g. Flying).                 | false         | true/false                                                                |
| `dropFromLevelList`                      | Pokémon drop moves they learn from leveling up (only up to their current level).          | true          | true/false                                                                |
| `dropAnyLevelMoveFromLevelList`          | Pokémon can drop any move from their level-up list, even moves learned at a higher level. | false         | true/false                                                                |
| `dropFromTmMoveList`                     | Pokémon drop moves from their TM move learnset.                                           | true          | true/false                                                                |
| `dropFromEggMoveList`                    | Pokémon drop moves from their egg move learnset.                                          | true          | true/false                                                                |
| `dropFromTutorMoveList`                  | Pokémon drop moves from their tutor move learnset.                                        | true          | true/false                                                                |
| `showPokemonThatCanLearnMove`            | Show a scrollable tooltip on TMs and TRs with all Pokémon that can learn the move.        | true          | true/false                                                                |
| `allowCustomMoves`                       | Allows players to add new custom moves and remove TMs and TRs from the game. See *Note    | false         | true/false                                                                |

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