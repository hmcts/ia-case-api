import csv
import json
import os

from filepath_settings import settings


def create_jsons_from_csv(csv_file, events: list[int] = None, output_dir_name_suffix: str = 'latest'):
    with open(csv_file, 'r') as file:
        csv_reader = csv.DictReader(file)
        events_counter = 1
        dir_name = make_output_dir(output_dir_name_suffix)
        for row in csv_reader:
            if 'data' in row and events_counter in events:
                data = json.loads(row['data'])
                event_name = get_event_name(row)
                case_id = get_case_id(row)
                filename = f"case_{case_id}_event_{events_counter}_{event_name}.json"
                full_filepath = os.path.join(dir_name, filename)
                with open(full_filepath, 'w') as json_file:
                    json.dump(data, json_file, indent=2)
                os.chmod(full_filepath, 0o777)
            events_counter += 1


def get_event_name(row):
    event_name = row['event_id']
    return event_name


def get_case_id(row):
    case_id = row['case_data_id']
    return case_id


def make_output_dir(case_name: str) -> str:
    directory_name = f'output_jsons_{case_name}'
    full_filepath = os.path.join(settings.output_json_directory, directory_name)
    if not os.path.exists(full_filepath):
        os.makedirs(full_filepath)
    os.chmod(full_filepath, 0o777)
    return full_filepath

# create_jsons_from_csv('case_event_202402071630.csv', events=list(range(1, 7)), output_dir_name_prefix='5405')
