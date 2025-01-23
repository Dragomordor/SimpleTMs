# SimpleTMs: TMs and TRs for Cobblemon

This Minecraft mod is an expansion to the Cobblemon mod, integrating TMs (Technical Machines) and TRs (Technical Records) from the Pokémon universe. The mod allows players to teach moves to their Pokémon, with either a TM or TR.

## Features

### TM and TR Usage
- **TMs:** Teach a move associated with that TM to a Pokémon. TMs by default have 32 uses, and can optionally be enchanted with Unbreaking and Mending.
- **TRs:** Also teach a move to a Pokémon, but TRs break after a single use.
- TM and TR items with moves associated to them (so not Blank TMs and TRs) are used similar to most cobblemon items, with a screen appearing that allows the player to choose a Pokémon in their party if the Pokémon can learn that move.
- TMs and TRs have tooltips that show information about the move, such at the description, type etc.
- TMs and TRs also have tooltips that can display all Pokémon that can learn that move, which can be scrolled through while hovering on the item. 

### Blank TM and TR Usage
- Blank TMs and TRs are used the same way as other cobblemon items which can select moves. This mean, when the player uses them, they can select a Pokémon in their party and a move in their move set. 
- Blank TMs dro the respective TM of the selected move, whilst Blank TRs drop the respective TRs of the selected move.

### Configuration Options
- Enable Egg Moves and Tutor Moves: TMs and TRs can be configured to teach Egg Moves and Tutor Moves to Pokémon.
- Allow Any Move Transfer: Configure the mod to enable any move to be transferred to any Pokémon.
- **Cooldown Functionality**
  - Configurable cooldown duration for TMs after use.
  - Tooltip to indicate the remaining cooldown time for a TM.

### Item Functionality
- Blank TM or TR functionality: Transfer the first move in a Pokémon's equipped moves to a blank TM or TR, granting the associated TM or TR to the player.

### Obtaining Items
- TMs and TRs are currently only obtainable in Creative Mode.

## Configuration

Modify the mod's configuration file (`config/simpletms_config.properties`) to customize the following settings:

- **AnyMoveAnyPokemon**: Allows any move to be taught to any Pokémon by a TM or TR if true. *Defaults to false.*
- **EggMovesLearnable**: Allows moves in a Pokémon's egg moves list to be taught via TM or TR. *Defaults to false.*
- **TutorMovesLearnable**: Allows moves in a Pokémon's tutor moves list to be taught via TM or TR. *Defaults to false.*
- **ImprintableBlankTMs**: Blank TMs and TRs can be used on a Pokémon, and their first equipped move imprinted onto it. *Defaults to true.*
- **TMCooldownTicks**: Adjust the cooldown duration (in ticks) for TMs after use. *Defaults to 100 ticks (5 seconds).*

## Support and Feedback

If you encounter any issues or have suggestions for improvement, feel free to create an issue or pull request on the GitHub repository -> [Fabric](https://github.com/Dragomordor/SimpleTMsFabric) & [Forge](https://github.com/Dragomordor/SimpleTMsForge)

**Note:** This mod is a side addition to the Cobblemon mod and requires Cobblemon 1.4.0 or 1.4.1.

## Credits

- Simple TMs mod developed by Dragomordor.

## License

This mod is licensed under the MIT liscense. Refer to the LICENSE file for more information.

Enjoy enhancing your Cobblemon experience with the added TMs and TRs!

## Other mods
Check out my other mods!
- **Cobblemizer**: Allow players to manipulate various stats and aesthetic characteristics of Pokémon within the game. [Modrinth](https://modrinth.com/mod/cobblemizer) & [Curseforge](https://www.curseforge.com/minecraft/mc-mods/cobblemizer)

## Future development

- I would like to add the ability to imprint tms as held items on Pokémon when a move is used, but I have to figure out how to do that first.
- Additionally, I would like a config option to introduce TMs as loot drops from Pokémon and in dungeons chests. 
- Lastly, I want to add a config option to bring a recipe into the game
