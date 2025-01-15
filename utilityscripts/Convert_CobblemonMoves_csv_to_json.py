# Convert the AllCobblemonMoves.csv file to a JSON file format

# Import the necessary libraries
import pandas as pd
import json
import os

# Import csv
csv_path = r"AllCobblemonMoves.csv"
data = pd.read_csv(csv_path)

# Sort the data by moveName
data = data.sort_values(by=['moveName'])

# Format of csv file:
# moveName,moveType,Name,Description
# 10000000voltthunderbolt,Electric,"10,000,000 Volt Thunderbolt","The user, Pikachu wearing a cap, powers up a jolt of electricity using its Z-Power and unleashes it. Critical hits land more easily."
# absorb,Grass,Absorb,A nutrient-draining attack. The user�s HP is restored by half the damage taken by the target.
# accelerock,Rock,Accelerock,The user smashes into the target at high speed. This move always goes first.

# Create a list to store the data
cobblemon_moves = []

# Loop through the data and store it in the dictionary
for index, row in data.iterrows():
    cobblemon_moves.append({
        "moveName": row['moveName'],
        "moveType": row['moveType'],
        "Name": row['Name'],
    })


# ----------------------------------------------------------
## Json Files
# ----------------------------------------------------------

##### MoveLearnItems default.json
# --------------------------------

# Format of json file:
# [
#   {
#     "moveName": "10000000voltthunderbolt",
#     "moveType": "Electric",
#   },
#   {
#     "moveName": "absorb",
#     "moveType": "Grass",
#   },
#   {
#       "moveName": "accelerock",
#       "moveType": "Rock",
#   }
# ]

# Create a list to store the data
movelearnitems = []

# Loop through the data and store it in the dictionary
for index, row in data.iterrows():
    movelearnitems.append({
        "moveName": row['moveName'],
        "moveType": row['moveType'],
    })

# Create a JSON file movelearnitems/default.json

# Ensure the directory exists
os.makedirs("simpletms/movelearnitems", exist_ok=True)
default_json_path = r"simpletms/movelearnitems/default.json"
with open(default_json_path, 'w') as json_file:
    json.dump(movelearnitems, json_file, indent=4)

print("SUCCESS: (1/3) MoveLearnItems default.json JSON file created successfully!")

# Create custom.json file with empty array []
# Ensure the directory exists
custom_json_path = r"simpletms/movelearnitems/custom.json"
with open(custom_json_path, 'w') as json_file:
    json.dump([], json_file, indent=4)

##### lang file
# --------------------------------

# Create a JSON file lang/en_us.json
# Format of json file:

# {
#     "item.simpletms.tm_10000000voltthunderbolt":"TM-1: 10,000,000 Volt Thunderbolt",
#     "item.simpletms.tm_absorb":"TM-2: Absorb",
#     "item.simpletms.tm_accelerock":"TM-3: Accelerock"
#     ....
#     "item.simpletms.tr_10000000voltthunderbolt":"TR-1: 10,000,000 Volt Thunderbolt",
#     "item.simpletms.tr_absorb":"TR-2: Absorb",
#     "item.simpletms.tr_accelerock":"TR-3: Accelerock"
# }

# Add the following to the top of the lang/en_us.json file before the first key-value pair
#   "item.simpletms.tm_blank": "Blank TM",
#   "item.simpletms.tr_blank": "Blank TR",
#   "itemGroup.simpletms.tm_items": "TM's",
#   "itemGroup.simpletms.tr_items": "TR's"

# Create a dictionary to store the data
lang_data = {}

# Add the standard keys
lang_data["item.simpletms.tm_blank"] = "Blank TM"
lang_data["item.simpletms.tr_blank"] = "Blank TR"
lang_data["itemGroup.simpletms.tm_items"] = "TM's"
lang_data["itemGroup.simpletms.tr_items"] = "TR's"

# Add the move names
for i, move in enumerate(cobblemon_moves, start=1):
    lang_data[f"item.simpletms.tm_{move['moveName']}"] = f"TM-{i}: {move['Name']}"
    lang_data[f"item.simpletms.tr_{move['moveName']}"] = f"TR-{i}: {move['Name']}"

# Create a JSON file lang/en_us.json
os.makedirs("assets/simpletms/lang", exist_ok=True)
lang_json_path = r"assets/simpletms/lang/en_us.json"
with open(lang_json_path, 'w') as json_file:
    json.dump(lang_data, json_file, indent=4)

print("SUCCESS: (2/3) lang/en_us.json JSON file created successfully!")

##### models/item json files
# --------------------------------

# Create 2 json files per move - 1 for TM and 1 for TR
# Create all files in simpletms/models/item
# Format of json file:
# tm_10000000voltthunderbolt.json:
# {
#     "parent": "item/generated",
#     "textures": {
#         "layer0": "simpletms:item/tm/electric"
#     }
# }
# tr_10000000voltthunderbolt.json:
# {
#     "parent": "item/generated",
#     "textures": {
#     "layer0": "simpletms:item/tr/electric"
#     }
# }

# Therfore the format of the json file is:
# {tr/tm}_{moveName}.json:
# {
#     "parent": "item/generated",
#     "textures": {
#         "layer0": "simpletms:item/{tr/tm}/{moveType--lowercase}"
#     }
# }

# Additionally, the blank TM and TR files should be created
# tm_blank.json AND tr_blank.json
# {
#   "parent": "minecraft:item/generated",
#   "textures": {
#     "layer0": "simpletms:item/{tm/tr}/blank"
#   }


# Create a dictionary to store the data
model_data = {}

# Ensure the directory exists
os.makedirs("assets/simpletms/models/item", exist_ok=True)

# Create the model files

# All default moves
for move in cobblemon_moves:
    tm_file_path = f"assets/simpletms/models/item/tm_{move['moveName']}.json"
    tr_file_path = f"assets/simpletms/models/item/tr_{move['moveName']}.json"
    
    tm_data = {
        "parent": "item/generated",
        "textures": {
            "layer0": f"simpletms:item/tm/{move['moveType'].lower()}"
        }
    }
    
    tr_data = {
        "parent": "item/generated",
        "textures": {
            "layer0": f"simpletms:item/tr/{move['moveType'].lower()}"
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
        "layer0": "simpletms:item/tm/blank"
    }
}
tr_blank_data = {
    "parent": "minecraft:item/generated",
    "textures": {
        "layer0": "simpletms:item/tr/blank"
    }
}

tm_blank_file_path = f"assets/simpletms/models/item/tm_blank.json"
tr_blank_file_path = f"assets/simpletms/models/item/tr_blank.json"

with open(tm_blank_file_path, 'w') as tm_blank_file:
    json.dump(tm_blank_data, tm_blank_file, indent=4)
with open(tr_blank_file_path, 'w') as tr_blank_file:
    json.dump(tr_blank_data, tr_blank_file, indent=4)

print("SUCCESS: (3/3) All JSON files created successfully!")

