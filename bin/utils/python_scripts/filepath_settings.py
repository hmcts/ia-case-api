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
        'attendingjudge': 'redacted - attendingjudge',
        'feedescription': 'redacted - feedescription',
        'searchpostcode': 'n11 1yz',
        'posttown': 'town',
        'details': 'redacted - details',
        'decisionreason': 'redacted - decisionreason',
        'appellantfamilyname': 'redacted - appellantfamilyname',
        'appellantgivennames': 'redacted - appellantgivennames',
        'appellantdateofbirth': '1980-01-01',
        'appellantemailaddress': 'email@email.com',
        'casenotedescription': 'redacted - casenotedescription',
        'casenotesubject': 'redacted - casenotesubject',
        'user': 'redacted - user',
        'question': 'redacted - question',
        'explanation': 'redacted - explanation',
        'address': 'redacted - address',
        'bundlefilenameprefix': 'EA 50111 2023',
        'casenamehmctsinternal': 'redacted redacted - casenamehmctsinternal',
        'hmctscasenameinternal': 'redacted redacted - hmctscasenameinternal',
        'appellantnamefordisplay': 'redacted redacted - appellantnamefordisplay',
        'reasonsforappealdecision': 'redacted - reasonsforappealdecision',
        'description': 'redacted - description',
        'answer': 'redacted - answer',
        'legalrepname': 'redacted - legalrepname',
        'mobilenumber': '07451111111',
        'legalrepcompany': 'redacted - legalrepcompany',
        'partyname': 'redacted - partyname',
        'legalrepcompanyname': 'redacted - legalrepcompanyname',
        'county': 'redacted - county',
        'legalrepresentativename': 'redacted - legalrepresentativename',
        'legalrepreferencenumber': 'AA/1234',
        'directioneditexplanation': 'redacted - directioneditexplanation',
        'fullname': 'redacted - fullname',
        'dayofbirth': 1,
        'familyname': 'redacted - familyname',
        'yearofbirth': 1980,
        'monthofbirth': 1,
        'displaydateofbirth': '10 Jan 1980',
        'documentreference': '012345678',
        'displayappellantdetailstitle': 'redacted - displayappellantdetailstitle',
        'displayapplicationdetailstitle': 'redacted - displayapplicationdetailstitle',
        'homeofficesearchresponse': 'redacted - homeofficesearchresponse',
        'remotevideocalldescription': 'redacted - remotevideocalldescription',
        'hearingdaterangedescription': 'Only include dates between 10 Aug 2023 and 10 Oct 2023.',
        'interpreterlanguagereadonly': "Language\t\tEnglish\nDialect\t\t\tENG",
        'legalrepresentativeemailaddress': 'email@email.co.uk',
        'remotevideocalltribunalresponse': 'redacted - remotevideocalltribunalresponse',
        'appellantnationalitiesdescription': 'France',
        'language': 'Arabic',
        'languagedialect': 'redacted - languagedialect',
        'legalaidaccountnumber': 'OG123V1',
        'appellantphonenumber': '07451111111',
        'givenname': 'redacted - givenname',
        'data': 'redacted - data',
        'witnessname': 'redacted - witnessname',
        'witnessdetailsreadonly': 'redacted - witnessdetailsreadonly',
        'multimediaTribunalResponse': 'redacted - multimediaTribunalResponse',
        'appellantfullname': 'redacted - appellantfullname',
        'endappealapprovername': 'redacted - endappealapprovername',
        'dateToAvoidReason': 'redacted - dateToAvoidReason',
        'sponsorNameForDisplay': 'redacted - sponsorNameForDisplay',
        'sponsorAddressForDisplay': '10 street',
        'sponsorMobileNumber': '07451111111',
        'Name': 'redacted - Name',
        'attendingAppellant': 'redacted - attendingAppellant',
        'attendingHomeOfficeLegalRepresentative': 'redacted - attendingHomeOfficeLegalRepresentative',
        'attendingAppellantsLegalRepresentative': 'redacted - attendingAppellantsLegalRepresentative',
        'newMatters': 'redacted - newMatters',
        'applicationOutOfTimeExplanation': 'redacted - applicationOutOfTimeExplanation',
        'caseName': 'redacted - caseName',
        'additionalRequestsDescription': 'redacted - additionalRequestsDescription',
        'uploadedHomeOfficeBundleDocs': 'redacted.pdf',
        'applicantFullName': 'redacted - applicantFullName',
        'applicantGivenNames': 'redacted - applicantGivenNames',
        'applicantDateOfBirth': '1990-01-10',
        'sponsorEmail': 'email@email.com',
        'exceptionalCircumstances': 'redacted - exceptionalCircumstances',
        'supporterGivenNames': 'redacted - supporterGivenNames',
        'supporterFamilyNames': 'redacted - supporterFamilyNames',
        'applicantFamilyName': 'redacted - applicantFamilyName',
        'supporterEmailAddress1': 'email@email.com',
        'supporterMobileNumber1': '01111111111',
        'supporterMobileNumber2': '01111111111',
        'supporterEmailAddress2': 'email@email.com',
        'applicantMobileNumber1': '01111111111',
        'applicantMobileNumber2': '01111111111',
        'supporterOccupation': 'redacted - supporterOccupation',
        'supporterPassport': '123456789',
        'supporterDOB': '1970-01-10',
        'judgeDetailsName': 'redacted - judgeDetailsName',
        'conditionsForBailActivities': 'redacted - conditionsForBailActivities',
        'conditionsForBailAppearance': 'redacted - conditionsForBailAppearance',
        'conditionsForBailElectronicMonitoring': 'redacted - conditionsForBailElectronicMonitoring',
        'legalRepEmail': 'email@email.com',
        'legalRepPhone': '01111111111',
        'legalRepFamilyName': 'redacted - legalRepFamilyName',
        'aipSponsorEmailForDisplay': 'aipSponsorEmail@ForDisplay.com',
        'EmailAddress': 'email@email.com',
        'interpreterEmail': 'email@interpreter.com',
        'interpreterFamilyName': 'redacted - interpreterFamilyName',
        'interpreterGivenNames': 'redacted - interpreterGivenNames',
        'interpreterPhoneNumber': '12345678999',
        'flagComment': 'redacted - flagComment'
    }

    csv_rows_to_redact = {
        "user_first_name": "redacted",
        "user_last_name": "redacted",
        "caseName": 'redacted'
    }


settings = Settings()
