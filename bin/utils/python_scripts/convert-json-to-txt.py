import json
import os

# Your JSON data (example) 1677498210980054-data-annotated.json
input_file = '/Users/jacobcohensolirius/HMCTS/IA/ia-case-api/bin/utils/python_scripts/SNi_tickets/SNI-5296/latest_data_class.json'
base_name, extension = os.path.splitext(input_file)
output_filename = f'{base_name}.txt'

# Read JSON data from the input file
with open(input_file, "r") as json_file:
    json_data = json.load(json_file)

# Convert JSON data to a compact text representation
text_data = json.dumps(json_data, separators=(',', ':'))

# Write the compact text data to a text file
with open(output_filename, "w") as text_file:
    text_file.write(text_data)
