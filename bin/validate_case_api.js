const process = require('process');
const fs = require('fs');
const walkSync = require('walk-sync');
const request = require('request');

const BASE_IA_CCD_DIR = process.env.IA_CCD_DIR !== undefined ? process.env.IA_CCD_DIR : '../ia-ccd-definitions/';
const IA_CCD_FIELDS_FILE = 'definitions/appeal/json/CaseField.json';

const BASE_SCAN_DIR = './';
const SCAN_PATTERNS = ['src/main/java/**/*.java'];

const FIELD_DEFINITIONS = 'src/main/java/uk/gov/hmcts/reform/iacaseapi/domain/entities/AsylumCaseFieldDefinition.java';
const IA_CASE_API_GITHUB = 'https://github.com/hmcts/ia-case-api/blob/master/';

const IGNORED = [
    {
        type: 'unknown',
        field: 'RESPONDENTS_DISPUTED_SCHEDULE_OF_ISSUES_DESCRIPTION',
        filename: 'src/main/java/uk/gov/hmcts/reform/iacaseapi/infrastructure/config/AsylumCaseDataConfiguration.java'
    },
    {
        type: 'unknown',
        field: 'DECISION_AND_REASONS_DOCUMENTS',
        filename: 'src/main/java/uk/gov/hmcts/reform/iacaseapi/infrastructure/config/AsylumCaseDataConfiguration.java'
    },
    {
        type: 'unknown',
        field: 'RESPONDENTS_AGREED_SCHEDULE_OF_ISSUES_DESCRIPTION',
        filename: 'src/main/java/uk/gov/hmcts/reform/iacaseapi/infrastructure/config/AsylumCaseDataConfiguration.java'
    },
];

// helper function
const camelToSnake = function (string) {
    return string.replace(/[\w]([A-Z])/g, function (m) {
        return m[0] + "_" + m[1];
    }).toUpperCase();
};

class Path {
    constructor(type, field, filename) {
        this.type = type;
        this.field = field;
        this.filename = filename;
    }
}

// extract existing fields from CCD
const existingCaseFields = JSON.parse(fs.readFileSync(BASE_IA_CCD_DIR + IA_CCD_FIELDS_FILE, 'utf8'))
    .map(caseField => caseField.ID);

// extract non existing fields from code
const nonExistingCaseFields = fs.readFileSync(FIELD_DEFINITIONS, 'utf8')
    .match(/\"\w+\"/g)
    .map(s => s.slice(1, -1))
    .filter(caseApiCaseField => !existingCaseFields.includes(caseApiCaseField))
    .map(caseApiCaseField => camelToSnake(caseApiCaseField));

// analyze paths and fields
const pathsToCheck = [];
walkSync(BASE_SCAN_DIR, {globs: SCAN_PATTERNS, directories: false})
    .filter(file => file !== FIELD_DEFINITIONS)
    .forEach(path => {
        let content = fs.readFileSync(path, 'utf8');
        nonExistingCaseFields.forEach(field => {

            if (content.includes('.write(' + field) || content.includes('.write(AsylumCaseFieldDefinition.' + field)) {
                pathsToCheck.push(new Path( 'write', field, path));
            } else if (content.includes('.read(' + field) || content.includes('.read(AsylumCaseFieldDefinition.' + field)) {
                pathsToCheck.push(new Path( 'read', field, path));
            } else if (content.includes(field)) {
                pathsToCheck.push(new Path( 'unknown', field, path));
            }
        });
    });

// filter exceptions
const afterFilterPathsToCheck = pathsToCheck.filter(
    path => IGNORED.findIndex(ignored => ignored.type === path.type && ignored.field === path.field && ignored.filename === path.filename) === -1
);

if (afterFilterPathsToCheck.length !== 0) {
    console.log('Please analyze below all WARNs and ERRORs before merging ia-case-api changes to master:');
} else {
    console.log('Validation passed successfully');
}

// requests for master code source
afterFilterPathsToCheck.forEach(function (path) {

    request(IA_CASE_API_GITHUB + path.filename, {json: false}, (err, res, body) => {

        if (err) {
            return console.log(err);
        }

        if (res.statusCode === 404) {
            console.log('INFO new field in new file for field: ' + path.field + ' in path: ' + path.filename);
        } else if (res.statusCode === 200) {
            let message = '';
            if (path.type === 'write') {
                message = 'ERROR intermittent CCD def is needed for field: ' + path.field + ' in path: ' + path.filename;
            } else if (path.type === 'unknown') {
                message = 'WARN intermittent CCD def may be needed for field: ' + path.field + ' in path: ' + path.filename;
            } else if (path.type === 'read') {
                message = 'INFO intermittent CCD def is not needed for field: ' + path.field + ' in path: ' + path.filename;
            }

            console.log(message);
        }
    });
});
