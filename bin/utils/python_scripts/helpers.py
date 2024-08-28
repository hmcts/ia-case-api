import csv
import os


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


# remove_first_n_rows("/ia-case-api/bin/utils/python_scripts/output_csv_files/case_event_202407221346_redacted.csv", 2)


update_case_data_with_latest_event("/ia-case-api/bin/utils/python_scripts/output_csv_files/case_data_202407221421_redacted.csv", "ia-case-api/bin/utils/python_scripts/output_csv_files/case_event_202407221346_redacted_reduced.csv")