import os
import json
import csv

"""
Script for redacting data from a case data JSON or CSV.

Usage: 

Replace filepath with absolute filepath of JSON/CSV requiring redacting at the bottom of this file where 
desired function is called (comment out other if not needed).

Run python bin/utils/python_scripts/redact_info_from_json.py while in ia/case/api directory
 
Script will output redacted JSON/CSV file in the same directory as original
with '_redacted' suffix.

Notes:
 
Fields to be redacted are hardcoded in replace mapping dict, so use with caution and double check redacted JSON 
(I suggest comparing two files in IDE) as it's only been tested on a few cases' data.

Add any additionally required fields (in full lowercase) to be redacted in replace_mapping_dict with their corresponding
replace data

Replaces all document filenames so if testing any specifics you may want to manually change the filenames
"""

replace_mapping_dict = {
    'email': 'email@email.com',
    'postcode': 'N11 1yz',
    'dateofbirth': '1980-01-01',
    'document_filename': 'redacted.pdf',
    'document_url': 'http://dm-store-aat.service.core-compute-aat.internal/documents/9ce3f9d5-31ef-4021-9aa5-4d017c404cfe',
    'document_binary_url': 'http://dm-store-aat.service.core-compute-aat.internal/documents/9ce3f9d5-31ef-4021-9aa5-4d017c404cfe/binary',
    'name': 'redacted.pdf',
    'filename': 'redacted.pdf',
    'addressline1': '10 street',
    'addressline2': 'town',
    'addressline3': 'city',
    'attendingjudge': 'redacted',
    'feedescription': 'redacted',
    'searchpostcode': 'n11 1yz',
    'posttown': 'town',
    'details': 'redacted',
    'decisionreason': 'redacted',
    'appellantfamilyname': 'redacted',
    'appellantgivennames': 'redacted',
    'appellantdateofbirth': '1980-01-01',
    'appellantemailaddress': 'email@email.com',
    'casenotedescription': 'redacted',
    'casenotesubject': 'redacted',
    'user': 'redacted',
    'question': 'redacted',
    'explanation': 'redacted',
    'address': 'redacted',
    'bundlefilenameprefix': 'EA 50111 2023',
    'casenamehmctsinternal': 'redacted',
    'hmctscasenameinternal': 'redacted',
    'appellantnamefordisplay': 'redacted',
    'reasonsforappealdecision': 'redacted',
    'description': 'redacted',
    'answer': 'redacted',
    "legalrepname": "redacted",
    "mobilenumber": "07451111111",
    "legalrepcompany": "redacted",
    "partyname": "redacted",
    "legalrepcompanyname": "redacted",
    "county": "redacted",
    "legalrepresentativename": "redacted",
    "legalrepreferencenumber": "AA/1234",
    "directioneditexplanation": 'redacted',
    "fullname": "redacted",
    "dayofbirth": 1,
    "familyname": "redacted",
    "yearofbirth": 1980,
    "monthofbirth": 1,
    "displaydateofbirth": "10 Jan 1980",
    "documentreference": "012345678",
    "displayappellantdetailstitle": "redacted",
    "displayapplicationdetailstitle": "redacted",
    "homeofficesearchresponse": 'redacted',
    "remotevideocalldescription": 'redacted',
    "hearingdaterangedescription": 'redacted',
    "interpreterlanguagereadonly": "Language\t\tEnglish\nDialect\t\t\tENG",
    "legalrepresentativeemailaddress": "email@email.co.uk",
    "remotevideocalltribunalresponse": "redacted",
    "appellantnationalitiesdescription": "France",
    "language": "English",
    "languagedialect": "redacted",
    "legalaidaccountNumber": "OG123V1",
    "appellantPhoneNumber": "07451111111",
    "givenName": "redacted",
    "appellantfullname": "redacted",
    "endappealapprovername": "redacted",
    
    # Refactor to separate list for CSV rows
    "data": "redacted",
}

csv_rows_to_redact = {
    "user_first_name": "redacted",
    "user_last_name": "redacted"
}

replace_mapping_keys = list(replace_mapping_dict.keys())
replace_csv_mapping_keys = list(csv_rows_to_redact.keys())


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


def redact_values_from_csv(input_file_path, keys_to_redact):
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
                    print(f'Not a valid json within CSV field: {key}')
                    pass
            elif key.lower() in replace_csv_mapping_keys:
                row[key] = 'redacted'
            else:
                pass


def get_redacted_file_path(original_file_path):
    base_name, extension = os.path.splitext(original_file_path)
    return f"{base_name}_redacted{extension}"


def get_replace_term(key):
    key = key.lower()
    return replace_mapping_dict.get(key, 'redacted')


# redact_values_from_json(
#     '/Users/jacobcohensolirius/HMCTS/IA/ia-case-api/bin/utils/python_scripts/SNi_tickets/SNI-5296/latest_data.json', replace_mapping_keys
# )

redact_values_from_csv('/Users/jacobcohensolirius/HMCTS/IA/ia-case-api/bin/utils/python_scripts/SNi_tickets/SNI_5340/case_event_202401261547.csv', replace_mapping_keys)
