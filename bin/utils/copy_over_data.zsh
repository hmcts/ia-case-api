#!/bin/zsh
if [ -z "$1" ]; then
    echo "Missing PR_NUMBER. Usage: $0 <PR_NUMBER> <caseReferenceToReplaceWith> <caseReferenceToReplace>"
    exit 1
fi

if [ -z "$2" ]; then
    echo "Missing caseReferenceToReplaceWith. Usage: $0 <PR_NUMBER> <caseReferenceToReplaceWith> <caseReferenceToReplace>"
    exit 1
fi

if [ -z "$3" ]; then
    echo "Missing caseReferenceToReplace. Usage: $0 <PR_NUMBER> <caseReferenceToReplaceWith> <caseReferenceToReplace>"
    exit 1
fi

prNumber=$1
ogCaseReference=$2
newCaseReference=$3

PGPASSWORD=hmcts psql -h ia-preview.postgres.database.azure.com -p 5432 -U hmcts -d "pr-$prNumber-data-store" <<SQL
DO \$\$
DECLARE ogCaseDataId int; newCaseDataId int; ogData jsonb; ogDataClassification jsonb; ogState varchar;
BEGIN
SELECT data, data_classification, state, id INTO ogData, ogDataClassification, ogState, ogCaseDataId FROM case_data WHERE reference = $ogCaseReference;
SELECT id INTO newCaseDataId FROM case_data WHERE reference = $newCaseReference;
UPDATE case_data SET "data"=ogData,"data_classification"=ogDataClassification,"state"=ogState WHERE jurisdiction='IA' AND case_type_id='Asylum' AND reference = $newCaseReference;
DELETE FROM case_event WHERE case_data_id=newCaseDataId;
CREATE TEMPORARY TABLE temporary_table AS SELECT * FROM case_event WHERE case_data_id=ogCaseDataId;
UPDATE temporary_table SET case_data_id=newCaseDataId, id=DEFAULT;
INSERT INTO case_event (case_data_id,created_date,event_id,summary,description,user_id,case_type_id,case_type_version,state_id,"data",user_first_name,user_last_name,event_name,state_name,data_classification,security_classification)
SELECT case_data_id,created_date,event_id,summary,description,user_id,case_type_id,case_type_version,state_id,"data",user_first_name,user_last_name,event_name,state_name,data_classification,security_classification FROM temporary_table;
DROP TABLE temporary_table;
END \$\$;
SQL