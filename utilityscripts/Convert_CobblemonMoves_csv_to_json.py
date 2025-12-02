# Convert the AllCobblemonMoves.csv file to a JSON file format

# Import the necessary libraries
import pandas as pd
import json
import os

# Import csv
csv_path = r"AllMoves.csv"
data = pd.read_csv(csv_path)
modid = "simpletms"

# Format of csv file:
# Name,Type,Category,Gen,PP,Power,Accuracy
# Pound,Normal,Physical,I,35,40,100%
# Karate Chop,Fighting,Physical,I,25,50,100%
# Double Slap,Normal,Physical,I,10,15,85%
# Comet Punch,Normal,Physical,I,15,18,85%
# Mega Punch,Normal,Physical,I,20,80,85%
# ....

# ----------------------------------------------------------
## Modify data to remove unwanted move entries (delete entire row)
# ----------------------------------------------------------

# The following moves are not needed:
    # Z-type moves should be removed

z_type_moves = [
    # Table 1 on https://bulbapedia.bulbagarden.net/wiki/Z-Move
    "Acid Downpour",
    "All-Out Pummeling",
    "Black Hole Eclipse",
    "Bloom Doom",
    "Breakneck Blitz",
    "Continental Crush",
    "Corkscrew Crash",
    "Devastating Drake",
    "Gigavolt Havoc",
    "Hydro Vortex",
    "Inferno Overdrive",
    "Never-Ending Nightmare",
    "Savage Spin-Out",
    "Shattered Psyche",
    "Subzero Slammer",
    "Supersonic Skystrike",
    "Tectonic Rage",
    "Twinkle Tackle",
    # Table 2 on https://bulbapedia.bulbagarden.net/wiki/Z-Move
    "10,000,000 Volt Thunderbolt",
    "Catastropika",
    "Clangorous Soulblaze",
    "Extreme Evoboost",
    "Genesis Supernova",
    "Guardian of Alola",
    "Let's Snuggle Forever",
    "Light That Burns the Sky",
    "Malicious Moonsault",
    "Menacing Moonraze Maelstrom",
    "Oceanic Operetta",
    "Pulverizing Pancake",
    "Searing Sunraze Smash",
    "Sinister Arrow Raid",
    "Soul-Stealing 7-Star Strike",
    "Splintered Stormshards",
    "Stoked Sparksurfer",
]

max_type_moves = [
    # From https://bulbapedia.bulbagarden.net/wiki/Max_Move
    "Max Airstream",
    "Max Darkness",
    "Max Flare",
    "Max Flutterby",
    "Max Geyser",
    "Max Guard",
    "Max Hailstorm",
    "Max Knuckle",
    "Max Lightning",
    "Max Mindstorm",
    "Max Ooze",
    "Max Overgrowth",
    "Max Phantasm",
    "Max Quake",
    "Max Rockfall",
    "Max Starfall",
    "Max Steelspike",
    "Max Strike",
    "Max Wyrmwind",
]

# Signature moves
signature_moves = [
    # From https://bulbapedia.bulbagarden.net/wiki/Category:Signature_moves
    "Accelerock",
    "Aeroblast",
    "Anchor Shot",
    "Apple Acid",
    "Aqua Step",
    "Armor Cannon",
    "Astral Barrage",
    "Attack Order",
    "Aura Wheel",
    "Baddy Bad",
    "Baneful Bunker",
    "Barb Barrage",
    "Barrage",
    "Beak Blast",
    "Behemoth Bash",
    "Behemoth Blade",
    "Bitter Blade",
    "Bitter Malice",
    "Bleakwind Storm",
    "Blue Flare",
    "Bolt Strike",
    "Bone Club",
    "Bonemerang",
    "Bouncy Bubble",
    "Burning Bulwark",
    "Buzzy Buzz",
    "Ceaseless Edge",
    "Chatter",
    "Chilly Reception",
    "Chloroblast",
    "Clanging Scales",
    "Clangorous Soul",
    "Collision Course",
    "Conversion",
    "Conversion 2",
    "Core Enforcer",
    "Court Change",
    "Crush Grip",
    "Dark Void",
    "Decorate",
    "Defend Order",
    "Diamond Storm",
    "Dire Claw",
    "Doom Desire",
    "Double Iron Bash",
    "Double Shock",
    "Dragon Ascent",
    "Dragon Darts",
    "Dragon Energy",
    "Drum Beating",
    "Dynamax Cannon",
    "Eerie Spell",
    "Electro Drift",
    "Electro Shot",
    "Esper Wing",
    "Eternabeam",
    "Fairy Lock",
    "False Surrender",
    "Fickle Beam",
    "Fiery Dance",
    "Fillet Away",
    "Fleur Cannon",
    "Floaty Fall",
    "Floral Healing",
    "Flower Trick",
    "Flying Press",
    "Forest's Curse",
    "Freeze Shock",
    "Freezing Glare",
    "Freezy Frost",
    "Fusion Bolt",
    "Fusion Flare",
    "Gear Grind",
    "Geomancy",
    "Gigaton Hammer",
    "Glacial Lance",
    "Glaciate",
    "Glaive Rush",
    "Glitzy Glow",
    "Grav Apple",
    "Head Charge",
    "Heal Order",
    "Hydro Steam",
    "Hyper Drill",
    "Hyperspace Fury",
    "Hyperspace Hole",
    "Ice Burn",
    "Infernal Parade",
    "Instruct",
    "Ivy Cudgel",
    "Judgment",
    "Jungle Healing",
    "Kinesis",
    "King's Shield",
    "Kowtow Cleave",
    "Land's Wrath",
    "Light of Ruin",
    "Lovely Kiss",
    "Lumina Crash",
    "Lunar Blessing",
    "Lunar Dance",
    "Luster Purge",
    "Magic Powder",
    "Magma Storm",
    "Make It Rain",
    "Malignant Chain",
    "Meteor Assault",
    "Mighty Cleave",
    "Mind Blown",
    "Mist Ball",
    "Moongeist Beam",
    "Mountain Gale",
    "Multi-Attack",
    "Mystical Power",
    "Nature's Madness",
    "Nihil Light",
    "No Retreat",
    "Oblivion Wing",
    "Obstruct",
    "Octolock",
    "Origin Pulse",
    "Overdrive",
    "Pika Papow",
    "Plasma Fists",
    "Population Bomb",
    "Powder",
    "Precipice Blades",
    "Present",
    "Prismatic Laser",
    "Psyblade",
    "Psycho Boost",
    "Psyshield Bash",
    "Psystrike",
    "Purify",
    "Pyro Ball",
    "Rage Fist",
    "Relic Song",
    "Revelation Dance",
    "Roar of Time",
    "Ruination",
    "Sandsear Storm",
    "Sappy Seed",
    "Searing Shot",
    "Secret Sword",
    "Seed Flare",
    "Shadow Blast",
    "Shadow Bolt",
    "Shadow Bone",
    "Shadow Chill",
    "Shadow Fire",
    "Shadow Force",
    "Shell Trap",
    "Shelter",
    "Shore Up",
    "Silk Trap",
    "Sizzly Slide",
    "Sketch",
    "Snap Trap",
    "Snipe Shot",
    "Soft-Boiled",
    "Spacial Rend",
    "Sparkling Aria",
    "Sparkly Swirl",
    "Spectral Thief",
    "Spicy Extract",
    "Spirit Break",
    "Spirit Shackle",
    "Splishy Splash",
    "Springtide Storm",
    "Steam Eruption",
    "Stone Axe",
    "Strange Steam",
    "Sunsteel Strike",
    "Tachyon Cutter",
    "Take Heart",
    "Tar Shot",
    "Teatime",
    "Techno Blast",
    "Tera Starstorm",
    "Thousand Arrows",
    "Thousand Waves",
    "Thunder Cage",
    "Thunderclap",
    "Torch Song",
    "Toxic Thread",
    "Trick-or-Treat",
    "Triple Arrows",
    "Triple Dive",
    "Trop Kick",
    "Veevee Volley",
    "Volt Tackle",
    "Water Shuriken",
    "Wildbolt Storm",
    "Zippy Zap",
]

# Extra moves to exclude from the TM/TR pool
tm_removed_moves = [
    # TMs to remove (signature / edge-case / very limited level-up)
    "Wicked Torque",
    "Blazing Torque",
    "Noxious Torque",
    "Magical Torque",
    "Combat Torque",
    "Freezing Glare",
    "Blood Moon",
    "Matcha Gotcha",
    "Raging Bull",
    "Mortal Spin",
    "Revival Blessing",
    "Jet Punch",
    "V-create",
    "V-Create", # account for different hyphenation
    "Salt Cure",
    "Twin Beam",
    "Victory Dance",
    "Branch Poke",
    "Bone Rush",
    "Doodle",
    "Gear Up",
    "Jaw Lock",
    "Surging Strikes",
    "Tail Glow",
    "Topsy-Turvy",
    "Wicked Blow",
    "Bolt Beak",
    "Fishious Rend",
    "Night Daze",
]

# Remove the Z-type moves from the data
data = data[~data["Name"].isin(z_type_moves)]
# Remove the Max-type moves from the data
data = data[~data["Name"].isin(max_type_moves)]
# Remove the signature moves from the data
data = data[~data["Name"].isin(signature_moves)]
# Remove the additional edge-case TM-removed moves from the data
data = data[~data["Name"].isin(tm_removed_moves)]

# ----------------------------------------------------------
## Modify data to add new columns
# ----------------------------------------------------------

# Add a new column moveName
# This column is the moveName in lowercase and without spaces and special characters
data['moveName'] = data['Name'].str.lower().str.replace(' ', '').str.replace("'", '').str.replace("-", '').str.replace(".", '').str.replace(":", '').str.replace("(", '').str.replace(")", '').str.replace("!", '').str.replace("?", '').str.replace(",", '').str.replace("é", 'e').str.replace("’", '').str.replace("♂", 'm').str.replace("♀", 'f')

# Sort the data by moveName
data = data.sort_values(by=['moveName'])

# Remove the following columns:
# Reorder the columns
data = data[['moveName', 'Name', 'Type', 'Category', 'Gen', 'PP', 'Power', 'Accuracy']]

# ----------------------------------------------------------
## Json Files
# ----------------------------------------------------------

# Ensure the directory exists
os.makedirs(f"resources/{modid}/movelearnitems", exist_ok=True)

# Create a JSON file movelearnitems/default.json
# Make sure to remove the Name column only for the default.json file
default_json_path = f"resources/{modid}/movelearnitems/default.json"
default_data = data.copy()
# remove all columns except moveName
default_data.drop(columns=['Name', 'Category', 'Gen', 'PP', 'Power', 'Accuracy', 'Type'], inplace=True)
with open(default_json_path, 'w') as json_file:
    json.dump(default_data.to_dict(orient='records'), json_file, indent=4)

print("SUCCESS: (1/5) MoveLearnItems default.json JSON file created successfully!")


##### models/item json files
# --------------------------------
# Create a dictionary to store the data
model_data = {}
# Ensure the directory exists
os.makedirs(f"resources/assets/{modid}/models/item", exist_ok=True)

# Create the model files
# --------------------------------

# All default moves
for index, row in data.iterrows():
    tm_file_path = f"resources/assets/{modid}/models/item/tm_{row['moveName']}.json"
    tr_file_path = f"resources/assets/{modid}/models/item/tr_{row['moveName']}.json"
    
    tm_data = {
        "parent": "item/generated",
        "textures": {
            "layer0": f"{modid}:item/tm/{row['Type'].lower()}"
        }
    }
    
    tr_data = {
        "parent": "item/generated",
        "textures": {
            "layer0": f"{modid}:item/tr/{row['Type'].lower()}"
        }
    }
    
    with open(tm_file_path, 'w') as tm_file:
        json.dump(tm_data, tm_file, indent=4)
    
    with open(tr_file_path, 'w') as tr_file:
        json.dump(tr_data, tr_file, indent=4)

# Blank TM and TR
tm_blank_data = {
    "parent": "minecraft:item/generated",
    "textures": {
        "layer0": f"{modid}:item/tm/blank"
    }
}
tr_blank_data = {
    "parent": "minecraft:item/generated",
    "textures": {
        "layer0": f"{modid}:item/tr/blank"
    }
}

tm_blank_file_path = f"resources/assets/{modid}/models/item/tm_blank.json"
tr_blank_file_path = f"resources/assets/{modid}/models/item/tr_blank.json"

with open(tm_blank_file_path, 'w') as tm_blank_file:
    json.dump(tm_blank_data, tm_blank_file, indent=4)
with open(tr_blank_file_path, 'w') as tr_blank_file:
    json.dump(tr_blank_data, tr_blank_file, indent=4)

print("SUCCESS: (3/5) Models JSON files created successfully!")

##### item tags
# --------------------------------


# Create the tag files
# --------------------------------
# Overall
tm_items_data = {
    "replace": False,
    "values": [f"{modid}:tm_{row['moveName']}" for index, row in data.iterrows()]
}
tr_items_data = {
    "replace": False,
    "values": [f"{modid}:tr_{row['moveName']}" for index, row in data.iterrows()]
}

# Write the tag files
os.makedirs(f"resources/data/{modid}/tags/item", exist_ok=True)

tm_items_path = f"resources/data/{modid}/tags/item/tm_items.json"
tr_items_path = f"resources/data/{modid}/tags/item/tr_items.json"

with open(tm_items_path, 'w') as tm_items_file:
    json.dump(tm_items_data, tm_items_file, indent=4)

with open(tr_items_path, 'w') as tr_items_file:
    json.dump(tr_items_data, tr_items_file, indent=4)

# For each type
for Type in data['Type'].unique():
    tm_type_data = {
        "replace": False,
        "values": [f"{modid}:tm_{row['moveName']}" for index, row in data.iterrows() if row['Type'] == Type]
    }
    
    tr_type_data = {
        "replace": False,
        "values": [f"{modid}:tr_{row['moveName']}" for index, row in data.iterrows() if row['Type'] == Type]
    }
    
    tm_type_path = f"resources/data/{modid}/tags/item/type_{Type.lower()}_tm.json"
    tr_type_path = f"resources/data/{modid}/tags/item/type_{Type.lower()}_tr.json"
    
    with open(tm_type_path, 'w') as tm_type_file:
        json.dump(tm_type_data, tm_type_file, indent=4)
    
    with open(tr_type_path, 'w') as tr_type_file:
        json.dump(tr_type_data, tr_type_file, indent=4)

# For each category
for Category in data['Category'].unique():
    tm_category_data = {
        "replace": False,
        "values": [f"{modid}:tm_{row['moveName']}" for index, row in data.iterrows() if row['Category'] == Category]
    }
    
    tr_category_data = {
        "replace": False,
        "values": [f"{modid}:tr_{row['moveName']}" for index, row in data.iterrows() if row['Category'] == Category]
    }
    
    tm_category_path = f"resources/data/{modid}/tags/item/category_{Category.lower()}_tm.json"
    tr_category_path = f"resources/data/{modid}/tags/item/category_{Category.lower()}_tr.json"
    
    with open(tm_category_path, 'w') as tm_category_file:
        json.dump(tm_category_data, tm_category_file, indent=4)
    
    with open(tr_category_path, 'w') as tr_category_file:
        json.dump(tr_category_data, tr_category_file, indent=4)


print("SUCCESS: (4/5) Item Tags JSON files created successfully!")

print("SUCCESS: (5/5) All JSON files created successfully!")



