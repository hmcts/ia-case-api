import csv
import os
import json
import stat

from filepath_settings import settings


def remove_last_n_rows(csv_file, n):
    with open(csv_file, 'r') as file:
        lines = list(csv.reader(file))

    updated_lines = lines[:-n]
    file_name, file_extension = os.path.splitext(csv_file)
    new_csv_file = file_name + '_reduced' + file_extension

    with open(new_csv_file, 'w', newline='') as file:
        csv.writer(file).writerows(updated_lines)


def remove_first_n_rows(csv_file, n):
    with open(csv_file, 'r') as file:
        reader = csv.reader(file)
        lines = list(reader)

    file_name, file_extension = os.path.splitext(csv_file)
    new_csv_file = file_name + '_reduced' + file_extension
    header_row = lines[0]
    updated_lines = [header_row] + lines[n+2:]

    with open(new_csv_file, 'w', newline='') as file:
        writer = csv.writer(file)
        writer.writerows(updated_lines)


def update_case_data_with_latest_event(case_data_file, case_event_file):
    """
    Used for updating a case_data csv file to a certain event state to import test cases at specific stages of the journey.
    :param case_data_file:
    :param case_event_file:
    :return:
    """
    with open(case_event_file, 'r') as event_file:
        event_reader = csv.reader(event_file)
        event_header = next(event_reader)
        latest_event_row = next(event_reader, None)

    if latest_event_row:
        created_date = latest_event_row[event_header.index('created_date')]
        state_id = latest_event_row[event_header.index('state_id')]
        data = latest_event_row[event_header.index('data')]
        data_classification = latest_event_row[event_header.index('data_classification')]

        updated_data_rows = []
        with open(case_data_file, 'r') as data_file:
            data_reader = csv.reader(data_file)
            updated_data_rows.append(next(data_reader))
            for row in data_reader:
                row[1] = created_date if row[1] != 'last_modified_date' else row[1]
                row[4] = state_id if row[3] != 'state' else row[3]
                row[5] = data if row[5] != 'data' else row[5]
                row[6] = data_classification if row[6] != 'data_classification' else row[6]
                updated_data_rows.append(row)

        output_file = case_data_file.rsplit('.', 1)[0] + '_modified.csv'
        with open(output_file, 'w', newline='') as output_data_file:
            data_writer = csv.writer(output_data_file)
            data_writer.writerows(updated_data_rows)


def process_field_type(entry):

    field_type = entry.get('FieldType')

    if field_type == 'Label':
        return None
    elif field_type == 'Text':
        if 'number' in entry["ID"].lower:
            return '123456789'
        else:
            return f'redacted - {entry["ID"]}'
    elif field_type == 'Date':
        return '1970-01-10'
    elif field_type == 'YesOrNo':
        return None
    elif field_type == 'checklist':
        return None
    elif field_type == 'FixedRadioList':
        return None
    elif field_type == 'Collection':
        return None
    elif field_type == 'AddressUK':
        return '10 Street'
    elif field_type == 'Email':
        return 'email@redacted.com'
    elif field_type == 'appealGroundsEuRefusal':
        return None
    elif field_type == 'appealGroundsHumanRightsRefusal':
        return None
    elif field_type == 'appealGroundsDecisionHumanRightsRefusal':
        return None
    elif field_type == 'appealGroundsDeprivation':
        return None
    elif field_type == 'appealGroundsHumanRights':
        return None
    elif field_type == 'appealGroundsProtection':
        return None
    elif field_type == 'isFeeConsistentWithDecision':
        return None
    elif field_type == 'feeUpdateStatus':
        return None
    elif field_type == 'isDocumentSignedToday':
        return None
    elif field_type == 'makeAnApplication':
        return None
    elif field_type == 'FlagLauncher':
        return None
    elif field_type == 'ComponentLauncher':
        return None
    elif field_type == 'Document':
        return None
    elif field_type == 'appealGroundsEuRefusal':
        return None
    elif field_type == 'appealGroundsEuRefusal':
        return None
    else:
        print(f'Unrecognised field type: {field_type}')
        return None


def get_fields_from_case_field_json(filepath: str) -> dict:
    redacted_dict = {}
    output_directory = settings.field_redaction_jsons

    os.chmod(output_directory, stat.S_IRWXU | stat.S_IRGRP | stat.S_IXGRP | stat.S_IROTH | stat.S_IXOTH)

    output_filepath = os.path.join(output_directory, 'field_redactions.json')

    try:
        with open(filepath, 'r') as file:
            data = json.load(file)
    except FileNotFoundError:
        print(f"Error: File '{filepath}' not found.")
        return {}
    except json.JSONDecodeError:
        print(f"Error: Failed to decode JSON from file '{filepath}'.")
        return {}

    for entry in data:
        if 'ID' in entry:
            redacted_value = process_field_type(entry)
            if redacted_value:
                redacted_dict[entry['ID']] = redacted_value

    try:
        with open(output_filepath, 'w') as outfile:
            json.dump(redacted_dict, outfile, indent=4)
    except Exception as e:
        print(f"Error writing to '{output_filepath}': {e}")
        return {}

    return redacted_dict


def get_unique_field_types(filepath: str) -> set:
    """Extract unique FieldType values from the JSON."""
    field_types = set()  # Use a set to store unique FieldType values

    try:
        with open(filepath, 'r') as file:
            data = json.load(file)
    except FileNotFoundError:
        print(f"Error: File '{filepath}' not found.")
        return set()
    except json.JSONDecodeError:
        print(f"Error: Failed to decode JSON from file '{filepath}'.")
        return set()

    # Iterate over the entries and collect FieldType values
    for entry in data:
        if isinstance(entry, dict):  # Ensure the entry is a dictionary
            field_type = entry.get('FieldType')
            if field_type:
                field_types.add(field_type)

    return field_types


def output_field_types_to_csv(filepath: str):
    """Write the unique FieldType values to a CSV file."""
    # Extract unique FieldType values
    field_types = get_unique_field_types(filepath)

    # Define the output directory and CSV file path
    output_directory = settings.scripts_dir
    if not os.path.exists(output_directory):
        os.makedirs(output_directory)

    csv_filepath = os.path.join(output_directory, 'unique_field_types.csv')

    # Write the FieldType values to the CSV file
    try:
        with open(csv_filepath, 'w', newline='') as csvfile:
            writer = csv.writer(csvfile)
            writer.writerow(['FieldType'])  # CSV header
            for field_type in field_types:
                writer.writerow([field_type])
        print(f"Unique FieldTypes have been written to '{csv_filepath}'")
    except Exception as e:
        print(f"Error writing to CSV: {e}")



# remove_first_n_rows("/ia-case-api/bin/utils/python_scripts/output_csv_files/case_event_202407221346_redacted.csv", 2)

# update_case_data_with_latest_event("/ia-case-api/bin/utils/python_scripts/output_csv_files/case_data_202407221421_redacted.csv", "ia-case-api/bin/utils/python_scripts/output_csv_files/case_event_202407221346_redacted_reduced.csv")

# get_fields_from_case_field_json("/ia-ccd-definitions/definitions/appeal/json/CaseField.json")

# output_field_types_to_csv('/scratches/scratch_3.json')