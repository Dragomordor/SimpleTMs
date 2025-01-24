# SimpleTMs: TMs and TRs for Cobblemon

![](https://raw.githubusercontent.com/Dragomordor/SimpleTMs/refs/heads/master/utilityscripts/images/SimpleTMs.png?token=GHSAT0AAAAAAC2QW7LDH5YDL4CNFQ5K5N2AZ4TSOFQ)

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

![](https://gitlab.com/cable-mc/cobblemon-assets/-/raw/master/graphics/cobblemon_divider.png?ref_type=heads)

##### Usage Preview
![](https://raw.githubusercontent.com/Dragomordor/SimpleTMs/refs/heads/master/utilityscripts/images/move_learn.png?token=GHSAT0AAAAAAC2QW7LDPZ4V2RRHAS6DJDEOZ4TTMCA)

#### Tooltip Preview
![](https://raw.githubusercontent.com/Dragomordor/SimpleTMs/refs/heads/master/utilityscripts/images/Tooltips.png?token=GHSAT0AAAAAAC2QW7LC3JO27QVQGFH7Q7U6Z4TTM3A)


![](https://gitlab.com/cable-mc/cobblemon-assets/-/raw/master/graphics/cobblemon_divider.png?ref_type=heads)


### Blank TM and TR Usage
- **Blank TMs and TRs** allow players to imprint a move onto them.
- When used, they let the player select a Pokémon from their party and a move from its moveset.
- **Blank TMs** create a TM of the selected move, while **Blank TRs** create a TR of the selected move.
- By default, **any move** can be imprinted onto a blank TM or TR.

#### Usage Preview
![](https://raw.githubusercontent.com/Dragomordor/SimpleTMs/refs/heads/master/utilityscripts/images/blank_learning.png?token=GHSAT0AAAAAAC2QW7LC3GCIGKMF3PKUH7H2Z4TTPTA)

![](https://gitlab.com/cable-mc/cobblemon-assets/-/raw/master/graphics/cobblemon_divider.png?ref_type=heads)

### Obtaining TMs and TRs
- **Crafting:**
  - Blank TMs and TRs can be crafted as shown below.
![](https://raw.githubusercontent.com/Dragomordor/SimpleTMs/refs/heads/master/utilityscripts/images/recipes.png?token=GHSAT0AAAAAAC2QW7LD7O5PH5MCHGZEWAPCZ4TVETA)
- **Loot Chests:**
  - TRs (and rarely TMs) can be found in chests in various Minecraft structures like villages, ruined portals, etc.
  - The type of TRs that drop depends on the loot location (e.g., Fire-type moves in Nether chests, Normal-type moves in village chests).
  - Additional integration with **Pokeloot** ([Modrinth](https://modrinth.com/datapack/cobblemon-pokeloot)) allows more ways to obtain TMs and TRs if that datapack is installed.
![](https://raw.githubusercontent.com/Dragomordor/SimpleTMs/refs/heads/master/utilityscripts/images/loot_tables.png?token=GHSAT0AAAAAAC2QW7LCOLFNWVLYKAP5ZEQIZ4TVFZA)
- **Pokémon Drops:**
  - TRs (and rarely TMs) can drop when a Pokémon faints. This is the main way to obtain TMs and TRs in Cobblemon.
  - **In Battle:** Players can choose **1 of 4 random moves** from the Pokémon’s learnset.
  - **Outside Battle:** A random move is chosen automatically.
  - This encourages battling Pokémon instead of simply defeating them outside of combat.
![](https://raw.githubusercontent.com/Dragomordor/SimpleTMs/refs/heads/master/utilityscripts/images/pokemon_drops.png?token=GHSAT0AAAAAAC2QW7LCWTBOX3KM2QIY4NVQZ4TVJWQ)
- **Drop Rates (Default):**
  - TRs: **10% chance** when a Pokémon faints.
  - TMs: **1% chance** when a Pokémon faints.

## ![](https://gitlab.com/cable-mc/cobblemon-assets/-/raw/master/graphics/cobblemon_icon.png?ref_type=heads) Configuration

(mention where config is located, and that a lot of options are included to tailor experience easy for servers or singleplayer worlds.)

(Add table here with all config options and descriptions, ranges etc.)

(experimental config mentions - this option is included but comes with drawback when client and server have different options. Recommended not to use, but included mainly for singleplayer users who know exactly what they want)

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

## ![](https://gitlab.com/cable-mc/cobblemon-assets/-/raw/master/graphics/cobblemon_icon.png?ref_type=heads) Support and Feedback
If you encounter any issues or have suggestions for improvement, feel free to create an issue on the GitHub repository.

There is also a support channel on the Cobblemon discord, where you can ask for help or discuss the mod with other players.

<p align="center">
  <a href="https://github.com/Dragomordor/SimpleTMs">
    <img src="https://raw.githubusercontent.com/Dragomordor/SimpleTMs/refs/heads/master/utilityscripts/images/github-mark-white.png?token=GHSAT0AAAAAAC2QW7LDQMYL7V3TUPPLCGUSZ4TVLCA" alt="GitHub" width="50">
  </a>
  <a href="https://discord.com/channels/934267676354834442/1193517940067291157">
    <img src="https://gitlab.com/cable-mc/cobblemon-assets/-/raw/master/graphics/discord_button.png?ref_type=heads" alt="Discord" width="200" style="margin-left: 20px;">
  </a>
</p>


## ![](https://gitlab.com/cable-mc/cobblemon-assets/-/raw/master/graphics/cobblemon_icon.png?ref_type=heads) Future development
- Nothing planned right now. Ideas are always welcome!


## ![](https://gitlab.com/cable-mc/cobblemon-assets/-/raw/master/graphics/cobblemon_icon.png?ref_type=heads) Known Issues 
- Nothing so far :)

---

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