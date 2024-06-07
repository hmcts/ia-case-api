import json


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
    print(f'file converted {output_file}')


convert_json_to_txt("/Users/jacobcohensolirius/HMCTS/IA/ia-case-api/bin/utils/python_scripts/output_jsons/SNI-5686/importing.json")
