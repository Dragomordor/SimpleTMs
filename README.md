# SimpleTMs: TMs and TRs for Cobblemon

<div align="center">
  <img src="https://raw.githubusercontent.com/Dragomordor/SimpleTMs/refs/heads/master/utilityscripts/images/SimpleTMs.png" width="80%">
</div>

SimpleTMs allows players to teach moves to their Pokémon in Cobblemon using TMs and TRs.

## ![](https://gitlab.com/cable-mc/cobblemon-assets/-/raw/master/graphics/cobblemon_icon.png?ref_type=heads) Features

### TM and TR Usage
- **TMs:** Teach a specific move to a Pokémon. By default, TMs have **32 uses** and can optionally be enchanted with **Unbreaking** and **Mending** using an anvil.
- **TRs:** Function similarly but break after a **single use**.
- TMs and TRs with assigned moves (not blank ones) are used like most Cobblemon items. A screen will appear, allowing the player to choose a Pokémon from their party if it can learn the move.
- TMs and TRs display **tooltips** with move details, including description, type, and more.
- Tooltips also list all Pokémon that can learn the move, scrollable while hovering over the item.
- By default, Pokémon can learn moves from their **TM**, **Tutor**, and **Egg** Move groups.
  - Check [Bulbapedia](https://bulbapedia.bulbagarden.net/wiki/Main_Page) for a detailed list of what moves a Pokémon can learn.


##### Usage Preview

<div align="center">
  <img src="https://raw.githubusercontent.com/Dragomordor/SimpleTMs/refs/heads/master/utilityscripts/images/blank_learning.png?ref_type=heads" width="80%">
</div>

#### Tooltip Preview

<div align="center">
  <img src="https://github.com/Dragomordor/SimpleTMs/blob/master/utilityscripts/images/Tooltips.png?raw=true" width="80%">
</div>

### Blank TM and TR Usage
- **Blank TMs and TRs** allow players to imprint a move onto them.
- When used, they let the player select a Pokémon from their party and a move from its moveset.
- **Blank TMs** create a TM of the selected move, while **Blank TRs** create a TR of the selected move.
- By default, **any move** can be imprinted onto a blank TM or TR.

#### Usage Preview

<div align="center">
  <img src="https://raw.githubusercontent.com/Dragomordor/SimpleTMs/refs/heads/master/utilityscripts/images/blank_learning.png?ref_type=heads" width="80%">
</div>

### Obtaining TMs and TRs
- **Crafting:**
  - Blank TMs and TRs can be crafted as shown below.

<div align="center">
  <img src="https://raw.githubusercontent.com/Dragomordor/SimpleTMs/refs/heads/master/utilityscripts/images/recipes.png?ref_type=heads" width="80%">
</div>

- **Loot Chests:**
  - TRs (and rarely TMs) can be found in chests in various Minecraft structures like villages, ruined portals, etc.
  - The type of TRs that drop depends on the loot location (e.g., Fire-type moves in Nether chests, Normal-type moves in village chests).
  - Additional integration with **Pokeloot** ([Modrinth](https://modrinth.com/datapack/cobblemon-pokeloot)) allows more ways to obtain TMs and TRs if that datapack is installed.

<div align="center">
  <img src="https://raw.githubusercontent.com/Dragomordor/SimpleTMs/refs/heads/master/utilityscripts/images/loot_tables.png?ref_type=heads" width="80%">
</div>

- **Pokémon Drops:**
  - TRs (and rarely TMs) can drop when a Pokémon faints. This is the main way to obtain TMs and TRs in Cobblemon.
  - **In Battle:** Players can choose **1 of 4 random moves** from the Pokémon’s learnset.
  - **Outside Battle:** A random move is chosen automatically.
  - This encourages battling Pokémon instead of simply defeating them outside of combat.

<div align="center">
  <img src="https://raw.githubusercontent.com/Dragomordor/SimpleTMs/refs/heads/master/utilityscripts/images/pokemon_drops.png?ref_type=heads" width="80%">
</div>

- **Drop Rates (Default):**
  - TRs: **10% chance** when a Pokémon faints.
  - TMs: **1% chance** when a Pokémon faints.


## ![](https://gitlab.com/cable-mc/cobblemon-assets/-/raw/master/graphics/cobblemon_icon.png?ref_type=heads) Requirements

#### For NeoForge and Fabric
- **Minecraft 1.21.1** is required for SimpleTMs v2.0.0.
- **Cobblemon v1.6** ([Curseforge](https://www.curseforge.com/minecraft/mc-mods/cobblemon) & [Modrinth](https://modrinth.com/mod/cobblemon)) is required to use SimpleTMs v2.0.0.
- **Architectury API v13.0.6 or higher** ([Curseforge](https://www.curseforge.com/minecraft/mc-mods/architectury-api) & [Modrinth](https://modrinth.com/mod/architectury-api)) is required for SimpleTMs v2.0.0.
#### For Fabric Only
- **Fabric API for Minecraft 1.21.1** ([Curseforge](https://www.curseforge.com/minecraft/mc-mods/fabric-api) & [Modrinth](https://modrinth.com/mod/fabric-api)).
#### NeoForge Only
- **NeoForge for Minecraft 1.21.1** ([NeoForge Site](https://projects.neoforged.net/neoforged/neoforge)).
- **Kotlin For Forge v5.5.0 or higher** ([Curseforge](https://www.curseforge.com/minecraft/mc-mods/kotlin-for-forge) & [Modrinth](https://modrinth.com/mod/kotlin-for-forge)).


## ![](https://gitlab.com/cable-mc/cobblemon-assets/-/raw/master/graphics/cobblemon_icon.png?ref_type=heads) Configuration

The `main.json` configuration file is located in the `config/simpletms/` folder. Below is a list of key options available for customization:

The `main.json` configuration file defines various settings for TMs and TRs. Below is a list of key options available for customization:

| Option                                   | Description                                                                               | Default Value      | Range/Notes                                                                     |
|------------------------------------------|-------------------------------------------------------------------------------------------|--------------------|---------------------------------------------------------------------------------|
| `tmMovesLearnable`                       | Allows Pokémon to learn moves from their TM move learnset.                                | true               | true/false                                                                      |
| `eggMovesLearnable`                      | Allows Pokémon to learn moves from their egg move learnset.                               | true               | true/false                                                                      |
| `tutorMovesLearnable`                    | Allows Pokémon to learn moves from their tutor move learnset.                             | true               | true/false                                                                      |
| `levelMovesLearnable`                    | Makes Pokémon able to learn moves gained from leveling up early using TMs.                | false              | true/false                                                                      |
| `anyMovesLearnable`                      | Allows any Pokémon to learn any move.                                                     | false              | true/false                                                                      |
| `blankTMsUsable`                         | Allows blank TMs to be used.                                                              | true               | true/false                                                                      |
| `blankTRsUsable`                         | Allows blank TRs to be used.                                                              | true               | true/false                                                                      |
| `tmsUsable`                              | Allows TMs to be used  (doesn't affect Blank TMs)                                         | true               | true/false                                                                      |
| `trsUsable`                              | Allows TRs to be used  (doesn't affect Blank TRs)                                         | true               | true/false                                                                      |
| `tmCoolDownTicks`                        | The number of Minecraft ticks the TM will be on cooldown after use.                       | 0                  | Integer (0 to 51840000) - 30 days max                                           |
| `blankTMCoolDownTicks`                   | The number of Minecraft ticks the blank TM will be on cooldown after use.                 | 0                  | Integer (0 to 51840000) - 30 days max                                           |
| `tmBaseDurability`                       | Specifies the durability (uses) for TMs based on Minecraft's base durability.             | 32                 | Integer (1 to 1024)                                                             |
| `blankTMBaseDurability`                  | Specifies the durability (uses) for blank TMs based on Minecraft's base durability.       | 1                  | Integer (1 to 1024)                                                             |
| `blankTRBaseDurability`                  | Specifies the durability (uses) for blank TRs based on Minecraft's base durability.       | 1                  | Integer (1 to 1024)                                                             |
| `trStackSize`                            | The stack size for TRs and blank TRs.                                                     | 16                 | Integer (1 to 64)                                                               |
| `dropOutsideOfBattle`                    | Allows Pokémon to drop TMs and TRs when defeated outside of battle.                       | true               | true/false                                                                      |
| `dropInBattle`                           | Allows Pokémon to drop TMs and TRs when defeated in battle.                               | true               | true/false                                                                      |
| `dropRateInBattle`                       | The drop rate (%) of TMs and TRs in battle (0.1 = 10%).                                   | 0.1                | 0.0-1.0 (Percentage as decimal)                                                 |
| `dropRateOutsideOfBattle`                | The drop rate (%) of TMs and TRs outside of battle.                                       | 0.1                | 0.0-1.0 (Percentage as decimal)                                                 |
| `dropRateTMFractionInBattle`             | The fraction of drops that are TMs instead of TRs in battle. (0.1 = 10%)                  | 0.1                | 0.0-1.0 (Percentage as decimal)                                                 |
| `dropRateTMFractionOutsideOfBattle`      | The fraction of drops that are TMs instead of TRs outside of battle.                      | 0.1                | 0.0-1.0 (Percentage as decimal)                                                 |
| `numberOfMovesToChooseFromInBattle`      | Number of random moves to choose from after defeating Pokémon in battle.                  | 4                  | Integer (1-4)                                                                   |
| `numberOfMovesToChooseFromOutsideBattle` | Number of random moves to choose from after defeating Pokémon outside of battle.          | 1                  | Integer (1-4)                                                                   |
| `dropAnyMove`                            | Pokémon can drop any move as a TM or TR, regardless of the Pokémon.                       | false              | true/false                                                                      |
| `dropPrimaryType`                        | Pokémon can drop moves from their primary elemental type (e.g. Fire type).                | false              | true/false                                                                      |
| `dropSecondaryType`                      | Pokémon can drop moves from their secondary elemental type (e.g. Flying).                 | false              | true/false                                                                      |
| `dropFromLevelList`                      | Pokémon drop moves they learn from leveling up (only up to their current level).          | true               | true/false                                                                      |
| `dropAnyLevelMoveFromLevelList`          | Pokémon can drop any move from their level-up list, even moves learned at a higher level. | false              | true/false                                                                      |
| `dropFromTmMoveList`                     | Pokémon drop moves from their TM move learnset.                                           | true               | true/false                                                                      |
| `dropFromEggMoveList`                    | Pokémon drop moves from their egg move learnset.                                          | true               | true/false                                                                      |
| `dropFromTutorMoveList`                  | Pokémon drop moves from their tutor move learnset.                                        | true               | true/false                                                                      |
| `showPokemonThatCanLearnMove`            | Show a scrollable tooltip on TMs and TRs with all Pokémon that can learn the move.        | true               | true/false                                                                      |
| `pokemonSortOrder`                       | The order to sort Pokémon in the tooltip for TMs and TRs.                                 | "ALPHABETICAL_ASC" | "ALPHABETICAL_DESC", "ALPHABETICAL_ASC", POKEMON_TYPE_DESC", "POKEMON_TYPE_ASC" |
 | `allowItemRemovalATOWNRISK`              | Allows players to remove TMs and TRs from the game. See *Note                             | false              | true/false                                                                      |
* *Note: This option is not encouraged. To remove TM and TR items, set this to true and remove entries from the `config/simpletms/moves/default_tr_moves.json` and `config/simpletms/moves/default_tm_moves.json` files. This will prevent the items from being added to the game, but it will not remove existing items from the world or player inventories.This also prevents you from accessing servers with those items. For servers that remove entries, be aware tha players can still see the items in jei if they did not remove it from their configs. THey cannot get these items however, so it is purely visual*

### Excluding Moves

To exclude moves from either Pokémon drops, Blank TMs, or TM Learning, add the move to the relevant excluded moves json file located in the `config/simpletms/moves/` folder. This file contains a list of moves to exclude from the mod's functionality.
For example, to exclude the move Ember from pokemon drops, add the following to the `excluded_moves_from_pokemon_drops.json` file:

```json
[
  {
    "moveName": "ember"
  }
]
```

## ![](https://gitlab.com/cable-mc/cobblemon-assets/-/raw/master/graphics/cobblemon_icon.png?ref_type=heads) Support and Feedback
If you encounter any issues or have suggestions for improvement, feel free to create an issue on the GitHub repository.

There is also a support channel on the Cobblemon discord, where you can ask for help or discuss the mod with other players.

<p align="center">
  <a href="https://github.com/Dragomordor/SimpleTMs">
    <img src="https://raw.githubusercontent.com/Dragomordor/SimpleTMs/refs/heads/master/utilityscripts/images/github-mark-white.png?ref_type=heads" alt="GitHub" width="50">
  </a>
  <a href="https://discord.com/channels/934267676354834442/1193517940067291157">
    <img src="https://gitlab.com/cable-mc/cobblemon-assets/-/raw/master/graphics/discord_button.png?ref_type=heads" alt="Discord" width="200" style="margin-left: 20px;">
  </a>
</p>


## ![](https://gitlab.com/cable-mc/cobblemon-assets/-/raw/master/graphics/cobblemon_icon.png?ref_type=heads) Future development
- Update japanese lang file to newest version provided by Nyankoro


## ![](https://gitlab.com/cable-mc/cobblemon-assets/-/raw/master/graphics/cobblemon_icon.png?ref_type=heads) Known Issues 
- Nothing so far :)

## ![](https://gitlab.com/cable-mc/cobblemon-assets/-/raw/master/graphics/cobblemon_icon.png?ref_type=heads) License
This mod is licensed under the Mozilla Public License Version 2.0 license. Refer to the LICENSE file on the Github for more information.


## ![](https://gitlab.com/cable-mc/cobblemon-assets/-/raw/master/graphics/cobblemon_icon.png?ref_type=heads) Credits

- SimpleTMs mod was developed by Dragomordor (me)
- Huge thanks to Dogto Bloo for allowing me to use his retextures of the original v1 of this mod, as they were a much better replacement to the initial textures I made.
- Big thanks to the Cobblemon discord for always having people on that help with testing, ideas and banter.
- Nyankoro for providing a japanese translation for the before release.
- Anyone and everyone that support me through kind words of how they enjoyed the original mod so much. That motivated me a lot to rework and modernize it.
- Anyone who supports me financially using my Kofi link. This is 100% not needed by anyone, and yet some people still want to give back. HUGE props to them for making my side hobby able to make me buy pizza every now and then.


## ![](https://gitlab.com/cable-mc/cobblemon-assets/-/raw/master/graphics/cobblemon_icon.png?ref_type=heads) Support Me

<a href='https://ko-fi.com/G2G119GOZS' target='_blank'><img height='36' style='border:0px;height:36px;' src='https://storage.ko-fi.com/cdn/kofi6.png?v=6' border='0' alt='Buy Me a Coffee at ko-fi.com' /></a>
