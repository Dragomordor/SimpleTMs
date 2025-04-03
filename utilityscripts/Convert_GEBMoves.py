# Convert the AllCobblemonMoves.csv file to a JSON file format

# Import the necessary libraries
import pandas as pd
import json
import os

# Import csv
csv_path = r"GEBMoves.csv"
data = pd.read_csv(csv_path)
modid = "simpletms"

# Format of csv file:
# Internal Name, Type, Category


# ----------------------------------------------------------
## Modify data to remove unwanted move entries (delete entire row)
# ----------------------------------------------------------

# Move_data -- Only the internal Name column
move_data = data[['Internal Name']].copy()
move_type_cat_data = data[['Internal Name', 'Type', 'Category']].copy()
move_name_data = data[['Internal Name', 'Move']]

# ----------------------------------------------------------
## Json Files
# ----------------------------------------------------------

# Ensure the directory exists
os.makedirs(f"resources/{modid}/movelearnitems", exist_ok=True)

# Create a JSON file movelearnitems/geb_custom_moves.json
default_json_path = f"resources/{modid}/movelearnitems/geb_custom_moves.json"
with open(default_json_path, 'w') as json_file:
    json.dump(move_data.to_dict(orient='records'), json_file, indent=4)

print("SUCCESS: (1/5) MoveLearnItems geb_custom_moves.json JSON file created successfully!")


# --------------------------------
# Lang File
# --------------------------------

# in resources/assets/simpletms/lang/en_us.json
# Create the lang file path
os.makedirs(f"resources/assets/{modid}/lang", exist_ok=True)
lang_data = {}
lang_file_path = f"resources/assets/{modid}/lang/en_us.json"

#Each line in file should be:
    # "simpletms.item.{tm/tr}_{Internal Name}": "{TM/TR}: {Move Name}"

# Populate the lang file with move names
for index, row in move_name_data.iterrows():
    lang_data[f"{modid}.item.tm_{row['Internal Name']}"] = f"TM: {row['Move']}"
    lang_data[f"{modid}.item.tr_{row['Internal Name']}"] = f"TR: {row['Move']}"

# Write the lang file
with open(lang_file_path, 'w') as lang_file:
    json.dump(lang_data, lang_file, indent=4)

print("SUCCESS: (2/5) Lang file en_us.json created successfully!")

##### models/item json files
# --------------------------------
# Create a dictionary to store the data
model_data = {}
# Ensure the directory exists
os.makedirs(f"resources/assets/{modid}/models/item", exist_ok=True)

# Create the model files
# --------------------------------

# All custom moves
for index, row in move_type_cat_data.iterrows():
    tm_file_path = f"resources/assets/{modid}/models/item/tm_{row['Internal Name']}.json"
    tr_file_path = f"resources/assets/{modid}/models/item/tr_{row['Internal Name']}.json"
    
    tm_data = {
        "parent": "item/generated",
        "textures": {
            "layer0": f"{modid}:item/tm/{str(row['Type']).lower()}"
        }
    }
    
    tr_data = {
        "parent": "item/generated",
        "textures": {
            "layer0": f"{modid}:item/tr/{str(row['Type']).lower()}"
        }
    }
    
    with open(tm_file_path, 'w') as tm_file:
        json.dump(tm_data, tm_file, indent=4)
    
    with open(tr_file_path, 'w') as tr_file:
        json.dump(tr_data, tr_file, indent=4)


print("SUCCESS: (3/5) Models JSON files created successfully!")

##### item tags
# --------------------------------


# Create the tag files
# --------------------------------
# Overall
tm_items_data = {
    "replace": False,
    "values": [f"{modid}:tm_{row['Internal Name']}" for index, row in move_type_cat_data.iterrows()]
}
tr_items_data = {
    "replace": False,
    "values": [f"{modid}:tr_{row['Internal Name']}" for index, row in move_type_cat_data.iterrows()]
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
for Type in move_type_cat_data['Type'].unique():
    Type = str(Type)

    tm_type_data = {
        "replace": False,
        "values": [f"{modid}:tm_{row['Internal Name']}" for index, row in move_type_cat_data.iterrows() if row['Type'] == Type]
    }
    
    tr_type_data = {
        "replace": False,
        "values": [f"{modid}:tr_{row['Internal Name']}" for index, row in move_type_cat_data.iterrows() if row['Type'] == Type]
    }
    
    tm_type_path = f"resources/data/{modid}/tags/item/type_{Type.lower()}_tm.json"
    tr_type_path = f"resources/data/{modid}/tags/item/type_{Type.lower()}_tr.json"
    
    with open(tm_type_path, 'w') as tm_type_file:
        json.dump(tm_type_data, tm_type_file, indent=4)
    
    with open(tr_type_path, 'w') as tr_type_file:
        json.dump(tr_type_data, tr_type_file, indent=4)

# For each category
for Category in move_type_cat_data['Category'].unique():
    Category = str(Category)
    tm_category_data = {
        "replace": False,
        "values": [f"{modid}:tm_{row['Internal Name']}" for index, row in move_type_cat_data.iterrows() if row['Category'] == Category]
    }
    
    tr_category_data = {
        "replace": False,
        "values": [f"{modid}:tr_{row['Internal Name']}" for index, row in move_type_cat_data.iterrows() if row['Category'] == Category]
    }
    
    tm_category_path = f"resources/data/{modid}/tags/item/category_{Category.lower()}_tm.json"
    tr_category_path = f"resources/data/{modid}/tags/item/category_{Category.lower()}_tr.json"
    
    with open(tm_category_path, 'w') as tm_category_file:
        json.dump(tm_category_data, tm_category_file, indent=4)
    
    with open(tr_category_path, 'w') as tr_category_file:
        json.dump(tr_category_data, tr_category_file, indent=4)


print("SUCCESS: (4/5) Item Tags JSON files created successfully!")

print("SUCCESS: (5/5) All JSON files created successfully!")



