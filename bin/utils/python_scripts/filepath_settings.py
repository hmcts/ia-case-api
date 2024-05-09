import os

PROJECT_DIR = os.path.dirname(__file__)
PYTHON_SCRIPTS_DIR = os.path.join(PROJECT_DIR)
EXPORTED_CSV_INPUT_DIR = os.path.join(PYTHON_SCRIPTS_DIR, 'input_csv_files')
OUTPUT_CSV_DIR = os.path.join(PYTHON_SCRIPTS_DIR, "output_csv_files")
OUTPUT_JSON_DIRECTORY = os.path.join(PYTHON_SCRIPTS_DIR, "output_jsons")


class Settings:
    project_dir = PROJECT_DIR
    scripts_dir = PYTHON_SCRIPTS_DIR
    exported_csv_dir = EXPORTED_CSV_INPUT_DIR
    output_csv_dir = OUTPUT_CSV_DIR
    output_json_directory = OUTPUT_JSON_DIRECTORY

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
        'casenamehmctsinternal': 'redacted redacted',
        'hmctscasenameinternal': 'redacted redacted',
        'appellantnamefordisplay': 'redacted redacted',
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
        "language": "Arabic",
        "languagedialect": "redacted",
        "legalaidaccountnumber": "OG123V1",
        "appellantphonenumber": "07451111111",
        "givenname": "redacted",
        "data": "redacted",
        "witnessname": "redacted",
        "witnessdetailsreadonly": "redacted",
        "multimediaTribunalResponse": "redacted",
        "appellantfullname": "redacted",
        "endappealapprovername": "redacted",
        "dateToAvoidReason": "redacted",
        "sponsorNameForDisplay": "redacted",
        "sponsorAddressForDisplay": "10 street",
        "sponsorMobileNumber": "07451111111",
        "Name": "redacted",
        "attendingAppellant": "redacted",
        "attendingHomeOfficeLegalRepresentative": "redacted",
        "attendingAppellantsLegalRepresentative": "redacted",
        "newMatters": "redacted",
        "applicationOutOfTimeExplanation": "redacted",
        "caseName": 'redacted',
        "additionalRequestsDescription": "redacted",
        "uploadedHomeOfficeBundleDocs": "redacted.pdf",
        "applicantFullName": "redacted",
        "applicantGivenNames": "redacted",
        "applicantDateOfBirth": "1990-01-10"
    }

    csv_rows_to_redact = {
        "user_first_name": "redacted",
        "user_last_name": "redacted",
        "caseName": 'redacted'
    }


settings = Settings()
