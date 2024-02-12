import os
import json
import csv

from filepath_settings import settings

"""
Script for redacting data from a case data JSON or CSV.

Usage: 

Replace filepath with absolute filepath of JSON/CSV requiring redacting at the bottom of this file where 
desired function is called.

Script will output redacted JSON/CSV file in the same directory as original
with '_redacted' suffix.

Notes:

Fields to be redacted are hardcoded in replace mapping dict in filepath_settings.py, so use with caution and double 
check redacted JSON (I suggest comparing two files in IDE) as it's only been tested on a few cases' data.

Add any additionally required fields to be redacted in replace_mapping_dict with their corresponding
replace data

Replaces all document filenames so if testing any specifics you may want to manually change the filenames
"""

replace_mapping_keys = [x.lower() for x in list(settings.replace_mapping_dict.keys())]
replace_csv_mapping_keys = [x.lower() for x in list(settings.csv_rows_to_redact.keys())]


def redact_values_from_json(file_path, keys_to_redact):
    with open(file_path, 'r') as file:
        json_data = json.load(file)

    redact_values(json_data, keys_to_redact)

    output_file_path = get_redacted_file_path(file_path)

    with open(output_file_path, 'w') as output_file:
        json.dump(json_data, output_file, indent=2)


def redact_values(json_data, keys_to_redact):
    if isinstance(json_data, dict):
        for key, value in json_data.items():
            redact_values(value, keys_to_redact)
            if key.lower() in keys_to_redact and not isinstance(value, (dict, list)):
                replace_term = get_replace_term(key)
                json_data[key] = replace_term
    elif isinstance(json_data, list):
        for item in json_data:
            redact_values(item, keys_to_redact)


def redact_values_from_csv(input_file_path, keys_to_redact = replace_mapping_keys) -> str:
    with open(input_file_path, 'r', newline='') as input_file:
        reader = csv.DictReader(input_file)
        rows = list(reader)

    redact_csv_rows(rows, keys_to_redact)

    output_file_path = get_redacted_file_path(input_file_path)

    with open(output_file_path, 'w', newline='') as output_file:
        fieldnames = reader.fieldnames
        writer = csv.DictWriter(output_file, fieldnames=fieldnames)
        writer.writeheader()
        writer.writerows(rows)
    return output_file_path


def redact_csv_rows(rows, keys_to_redact):
    for row in rows:
        for key, value in row.items():
            if key.lower() in keys_to_redact and isinstance(row, dict):
                # Assuming the value is a JSON string
                try:
                    json_value = json.loads(value)
                    redact_values(json_value, keys_to_redact)
                    row[key] = json.dumps(json_value)
                except json.JSONDecodeError:
                    pass
            elif key.lower() in replace_csv_mapping_keys:
                row[key] = 'redacted'
            else:
                pass


def get_redacted_file_path(original_file_path):
    original_file_path = original_file_path.replace("input", "output")
    base_name, extension = os.path.splitext(original_file_path)
    file_name = f"{base_name}_redacted{extension}"
    return file_name


def get_replace_term(key):
    key = key.lower()
    return settings.replace_mapping_dict.get(key, 'redacted')


# redact_values_from_json(
#     'latest_data.json', replace_mapping_keys
# )

# redact_values_from_csv(
#     'case_event_202401261547.csv')
