import os
import csv

from filepath_settings import settings
from create_jsons_from_event_csv import create_jsons_from_csv
from redact_info_from_json import redact_values_from_csv


def prep_import_data(directory: str = settings.exported_csv_dir, events_to_get_individual_json: list[int] = None):
    """
    Function to prep exported CSV data for importing. Redacts and transforms most recent exported files within
    python_scripts directory.

    Exported files should be in format: case_event_202402080516.csv and case_data_202402080518.csv
    (default export pattern)
    """
    latest_case_event_data = get_latest_file(directory, 'case_event')
    latest_case_data = get_latest_file(directory, 'case_data')
    if events_to_get_individual_json:
        create_jsons_from_csv(latest_case_event_data, events=events_to_get_individual_json)
    redacted_case_data = redact_values_from_csv(latest_case_data)
    os.chmod(redacted_case_data, 0o777)
    case_data_id = input(
        'Import the redacted case data CSV and retrieve the correct case id.\nEnter the new case data id:')\
        .encode('utf-8').decode('utf-8')
    redacted_case_event_data = redact_values_from_csv(latest_case_event_data)
    redacted_replaced_event_data = replace_case_data_id(case_data_id, redacted_case_event_data)
    os.chmod(redacted_replaced_event_data, 0o777)


def get_latest_file(dir_path: str, file_prefix: str) -> str:
    dir_files = os.listdir(dir_path)
    files = [file for file in dir_files if file_prefix in file]
    times = []
    for file in files:
        try:
            file = file.split('.')[0]
            times.append(int(file[-12:]))
        except Exception as e:
            print(e)
            pass
    latest = max(times)
    filename = f"{file_prefix}_{latest}.csv"
    full_filepath = os.path.join(dir_path, filename)
    return full_filepath


def replace_case_data_id(new_id: str, file_path: str):
    with open(file_path, 'r') as file:
        reader = csv.DictReader(file)
        data_list = list(reader)

    for row in data_list:
        if 'case_data_id' in row:
            row['case_data_id'] = new_id

    with open(file_path, 'w', newline='') as file:
        fieldnames = reader.fieldnames
        writer = csv.DictWriter(file, fieldnames=fieldnames)
        writer.writeheader()
        writer.writerows(data_list)
    return file_path

prep_import_data(events_to_get_individual_json=range(1, 100))
