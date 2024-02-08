import csv
import json
import os


def create_jsons_from_csv(csv_file, events: list[int] = None, output_dir_name_prefix: str = ''):
    with open(csv_file, 'r') as file:
        csv_reader = csv.DictReader(file)
        events_counter = 1
        dir_name = make_output_dir(output_dir_name_prefix)
        for row in csv_reader:
            if 'data' in row and events_counter in events:
                data = json.loads(row['data'])
                filename = f"event_{events_counter}.json"
                full_filepath = os.path.join(dir_name, filename)
                with open(full_filepath, 'w') as json_file:
                    json.dump(data, json_file, indent=2)
            events_counter += 1


def make_output_dir(case_name: str) -> str:
    directory_name = f'output_{case_name}_jsons'
    if not os.path.exists(directory_name):
        os.makedirs(directory_name)
    return directory_name

# create_jsons_from_csv('case_event_202402071630.csv', events=list(range(1, 7)), output_dir_name_prefix='5405')
