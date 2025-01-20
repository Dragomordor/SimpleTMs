# Convert the AllCobblemonMoves.csv file to a JSON file format

# Import the necessary libraries
import pandas as pd
import json
import os

# Import csv
csv_path = r"AllMoves.csv"
data = pd.read_csv(csv_path)

# Sort the data by moveName
data = data.sort_values(by=['moveName'])

# Format of csv file:
# moveName,moveType,Name
# 10000000voltthunderbolt,Electric
# absorb,Grass,Absorb
# accelerock,Rock,Accelerock

# ----------------------------------------------------------
## Modify data to remove unwanted move entries (delete entire row)
# ----------------------------------------------------------

# The following moves are not needed:
    # All moves that start with 'hiddenpower'
data = data[~data['moveName'].str.startswith('hiddenpower')]
    # All moves that start with 'gmax'
data = data[~data['moveName'].str.startswith('gmax')]

# All Moves in ZTypeMoves.csv (same format as AllMoves.csv)
ztype_csv_path = r"ZTypeMoves.csv"
ztype_data = pd.read_csv(ztype_csv_path)
data = data[~data['moveName'].isin(ztype_data['moveName'])]

# ----------------------------------------------------------
## Extract data into dictionary
# ----------------------------------------------------------

##### All Moves dictionary
# --------------------------------

# Create a list to store the data
default_moves = []

# Loop through the data and store it in the dictionary
for index, row in data.iterrows():
    default_moves.append({
        "moveName": row['moveName'],
        "moveType": row['moveType'],
        "Name": row['Name'],
        "Category": row['Category'],
    })


# ----------------------------------------------------------
## Json Files
# ----------------------------------------------------------



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



##### Create a JSON file movelearnitems/default.json
# ----------------------------------------------------------

# Ensure the directory exists
os.makedirs("resources/simpletms/movelearnitems", exist_ok=True)
default_json_path = r"resources/simpletms/movelearnitems/default.json"
with open(default_json_path, 'w') as json_file:
    json.dump(movelearnitems, json_file, indent=4)

# Create custom.json file with empty array []
# Ensure the directory exists
custom_json_path = r"resources/simpletms/movelearnitems/custom.json"
with open(custom_json_path, 'w') as json_file:
    json.dump([], json_file, indent=4)

print("SUCCESS: (1/5) MoveLearnItems default.json JSON file created successfully!")

##### lang file
# --------------------------------

# Create a JSON file lang/en_us.json
# Create a dictionary to store the data
lang_data = {}

# Add Standard keys to top of the file
# --------------------------------
    # Display messages
lang_data["simpletms.success.learned"] = "%1$s learned %2$s!"
lang_data["simpletms.error.not_learnable.not_valid_move"] = "Not a valid move -- move does not exist!"
lang_data["simpletms.error.not_learnable.already_knows_move"] = "%1$s already knows %2$s"
lang_data["simpletms.error.not_learnable.not_in_learnable_moves"] = "%1$s can't learn %2$s"
lang_data["simpletms.error.not_usable.tms_disabled"] ="TM's have been disabled in this world"
lang_data["simpletms.error.not_usable.trs_disabled"] ="TR's have been disabled in this world"
lang_data["simpletms.error.not_usable.blank_trs_disabled"] = "Blank TR's have been disabled in this world"
lang_data["simpletms.error.not_usable.blank_tms_disabled"] = "Blank TM's have been disabled in this world"
lang_data["simpletms.error.not_learnable.on_cooldown"] = "%1$s is on cooldown for %2$s"
    # blank TM and TR
lang_data["item.simpletms.tm_blank"] = "Blank TM"
lang_data["item.simpletms.tr_blank"] = "Blank TR"
    # item groups
lang_data["itemGroup.simpletms.tm_items"] = "TM's"
lang_data["itemGroup.simpletms.tr_items"] = "TR's"

# Add the move display names
# --------------------------------
for move in default_moves:
    lang_data[f"item.simpletms.tm_{move['moveName']}"] = f"TM: {move['Name']}"
    lang_data[f"item.simpletms.tr_{move['moveName']}"] = f"TR: {move['Name']}"

# Create the json file
# --------------------------------

# Create a JSON file lang/en_us.json
os.makedirs("resources/assets/simpletms/lang", exist_ok=True)
lang_json_path = r"resources/assets/simpletms/lang/en_us.json"
with open(lang_json_path, 'w') as json_file:
    json.dump(lang_data, json_file, indent=4)

print("SUCCESS: (2/5) lang/en_us.json JSON file created successfully!")

##### models/item json files
# --------------------------------
# Create a dictionary to store the data
model_data = {}
# Ensure the directory exists
os.makedirs("resources/assets/simpletms/models/item", exist_ok=True)

# Create the model files
# --------------------------------
# All default moves
for move in default_moves:
    tm_file_path = f"resources/assets/simpletms/models/item/tm_{move['moveName']}.json"
    tr_file_path = f"resources/assets/simpletms/models/item/tr_{move['moveName']}.json"
    
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

tm_blank_file_path = f"resources/assets/simpletms/models/item/tm_blank.json"
tr_blank_file_path = f"resources/assets/simpletms/models/item/tr_blank.json"

with open(tm_blank_file_path, 'w') as tm_blank_file:
    json.dump(tm_blank_data, tm_blank_file, indent=4)
with open(tr_blank_file_path, 'w') as tr_blank_file:
    json.dump(tr_blank_data, tr_blank_file, indent=4)

print("SUCCESS: (3/5) Models JSON files created successfully!")

##### item tags
# --------------------------------

# Adding the TM and TR items to the item tags
# The following tag files are made in resources/data/minecraft/tags/item:
# Overall
    # tm_items.json
    # tr_items.json
# For each type
    # type_normal_tm
    # type_normal_tr
    # type_fighting_tm
    # type_fighting_tr
    # ... for all types
# For each category
    # category_physical_tm
    # category_physical_tr
    # category_status_tm
    # category_status_tr
    # category_special_tm
    # category_special_tr


# Format of each file:

# {
#     "replace": false,
#     "values": [
#         "simpletms:{tm/tr}_{movename}",
#         "simpletms:{tm/tr}_{movename}",
#         "simpletms:{tm/tr}_{movename}"
#     ]
# }


# Create the tag files
# --------------------------------
# Overall
tm_items_data = {
    "replace": False,
    "values": [f"simpletms:tm_{move['moveName']}" for move in default_moves]
}

tr_items_data = {
    "replace": False,
    "values": [f"simpletms:tr_{move['moveName']}" for move in default_moves]
}

# Write the tag files
os.makedirs("resources/data/minecraft/tags/item", exist_ok=True)

tm_items_path = r"resources/data/minecraft/tags/item/tm_items.json"
tr_items_path = r"resources/data/minecraft/tags/item/tr_items.json"

with open(tm_items_path, 'w') as tm_items_file:
    json.dump(tm_items_data, tm_items_file, indent=4)

with open(tr_items_path, 'w') as tr_items_file:
    json.dump(tr_items_data, tr_items_file, indent=4)
    # For each type
    for moveType in data['moveType'].unique():
        tm_type_data = {
            "replace": False,
            "values": [f"simpletms:tm_{move['moveName']}" for move in default_moves if move['moveType'] == moveType]
        }
        
        tr_type_data = {
            "replace": False,
            "values": [f"simpletms:tr_{move['moveName']}" for move in default_moves if move['moveType'] == moveType]
        }
        
        tm_type_path = f"resources/data/minecraft/tags/item/type_{moveType.lower()}_tm.json"
        tr_type_path = f"resources/data/minecraft/tags/item/type_{moveType.lower()}_tr.json"
        
        with open(tm_type_path, 'w') as tm_type_file:
            json.dump(tm_type_data, tm_type_file, indent=4)
        
        with open(tr_type_path, 'w') as tr_type_file:
            json.dump(tr_type_data, tr_type_file, indent=4)

    # For each category
    for category in data['Category'].unique():
        tm_category_data = {
            "replace": False,
            "values": [f"simpletms:tm_{move['moveName']}" for move in default_moves if move['Category'] == category]
        }
        
        tr_category_data = {
            "replace": False,
            "values": [f"simpletms:tr_{move['moveName']}" for move in default_moves if move['Category'] == category]
        }
        
        tm_category_path = f"resources/data/minecraft/tags/item/category_{category.lower()}_tm.json"
        tr_category_path = f"resources/data/minecraft/tags/item/category_{category.lower()}_tr.json"
        
        with open(tm_category_path, 'w') as tm_category_file:
            json.dump(tm_category_data, tm_category_file, indent=4)
        
        with open(tr_category_path, 'w') as tr_category_file:
            json.dump(tr_category_data, tr_category_file, indent=4)


print("SUCCESS: (4/5) Item Tags JSON files created successfully!")

print("SUCCESS: (5/5) All JSON files created successfully!")



