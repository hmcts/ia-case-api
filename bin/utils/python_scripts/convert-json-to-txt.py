import json

# Your JSON data (example) 1677498210980054-data-annotated.json

# Read JSON data from the input file
with open("4919_latest_data_class.json", "r") as json_file:
    json_data = json.load(json_file)

# Convert JSON data to a compact text representation
text_data = json.dumps(json_data, separators=(',', ':'))

# Write the compact text data to a text file
with open("4919_latest_data_class.txt", "w") as text_file:
    text_file.write(text_data)
