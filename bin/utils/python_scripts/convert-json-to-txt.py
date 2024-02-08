import json


# Your JSON data (example) 1677498210980054-data-annotated.json
def convert_json_to_txt(file_path):
    # Read JSON data from the input file
    with open(file_path, "r") as json_file:
        json_data = json.load(json_file)

    # Convert JSON data to a compact text representation
    text_data = json.dumps(json_data, separators=(',', ':'))

    output_file = file_path.split(".")[0] + ".txt"

    # Write the compact text data to a text file
    with open(output_file, "w") as text_file:
        text_file.write(text_data)


convert_json_to_txt("event_1.json")
