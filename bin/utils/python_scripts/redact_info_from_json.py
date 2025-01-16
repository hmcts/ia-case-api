import os
import json
import csv
import sys

from filepath_settings import settings

"""

Usage:

Replace filepath with absolute filepath of JSON/CSV requiring redacting at the bottom of this file where
desired function is called.

Notes:

Fields to be redacted are hardcoded in replace mapping dict in filepath_settings.py, so use with caution and double
check redacted JSON and add any additionally required fields to be redacted in replace_mapping_dict with their corresponding
replace data

"""

replace_mapping_keys = [x.lower() for x in list(settings.replace_mapping_dict.keys())]
replace_csv_mapping_keys = [x.lower() for x in list(settings.csv_rows_to_redact.keys())]

lowercase_dict = {key.lower(): value for key, value in settings.replace_mapping_dict.items()}

csv.field_size_limit(sys.maxsize)


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


def redact_values_from_csv(input_file_path, keys_to_redact=replace_mapping_keys) -> str:
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
    lower_key = key.lower()
    return lowercase_dict.get(lower_key, 'redacted')


def replace_specific_field_in_json_file(
        json_filepath: str,
        field: str,
        path_fields: list[str] = [],
        id_list: list[str] = [],
        replace_term: str = 'SPECIFICALLY_REDACTED'):

    with open(json_filepath, 'r') as file:
        json_data = json.load(file)
        search_json_and_replace(json_data, field, path_fields, id_list, replace_term)

    with open(json_filepath, 'w') as file:
        json.dump(json_data, file, indent=4)


def search_json_and_replace(obj, field, path_fields, id_list, replace_term):
    field_should_be_parsed = True
    if isinstance(obj, dict):
        for key, value in obj.items():
            if id_list:
                if key == 'id':
                    current_id_value_being_parsed = obj[key]
                    if current_id_value_being_parsed not in id_list:
                        field_should_be_parsed = False
            if key == field:
                print(f'Replacing {obj[key]} with {replace_term}')
                obj[key] = replace_term
            elif key in path_fields and field_should_be_parsed:
                search_json_and_replace(value, field, path_fields, id_list, replace_term)
    elif isinstance(obj, list):
        for item in obj:
            search_json_and_replace(item, field, path_fields, id_list, replace_term)

    return obj


def replace_specific_field_in_csv_file_of_jsons(
        input_file_path,
        field: str,
        events_to_redact: list[int] = [],
        path_fields: list[str] = [],
        id_list: list[str] = [],
        replace_term: str = 'SPECIFICALLY_REDACTED'):

    with open(input_file_path, 'r', newline='') as input_file:
        reader = csv.DictReader(input_file)
        rows = list(reader)

        if not events_to_redact:
            events_to_redact = list(range(1, len(rows)))

        for idx, row in enumerate(rows):
            if idx in events_to_redact:
                json_data = json.loads(row['data'])
                row['data'] = search_json_and_replace(json_data, field, path_fields, id_list, replace_term)

        fieldnames = reader.fieldnames

    with open(input_file_path, 'w', newline='') as output_file:
        writer = csv.DictWriter(output_file, fieldnames=fieldnames)
        writer.writeheader()
        writer.writerows(rows)

# example usage

# redact_values_from_json(
#     'latest_data.json', replace_mapping_keys
# )

# redact_values_from_csv(
#     'case_event_202401261547.csv')

# replace_specific_field_in_json_file('case_7088977_event_1_recordRemissionDecision.json', 'document_filename', ['caseNotes', 'value', 'caseNoteDocument'])

# replace_specific_field_in_csv_file_of_jsons('case_event_202403261448_redacted.csv', 'document_filename', path_fields=['caseNotes', 'value', 'caseNoteDocument'], id_list=['1'])