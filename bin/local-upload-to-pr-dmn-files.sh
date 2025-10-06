#!/usr/bin/env bash

if [ -z "$1" ]; then
    echo "Missing ia-case-api PR number. Usage: $0 <ia-case-api PR number> <task-config branch name>"
    exit 1
fi

if [ -z "$2" ]; then
    echo "Missing task-config branch name. Usage: $0 <ia-case-api PR number> <task-config branch name>"
    exit 1
fi

caseApiPrNumber=$1
branchName=$2

source ./bin/variables/load-dev-user-preview-environment-variables.sh $caseApiPrNumber
#Checkout specific branch pf  camunda bpmn definition
git clone https://github.com/hmcts/ia-task-configuration.git
cd ia-task-configuration

echo "Switch to ${branchName} branch on ia-task-configuration"
git checkout ${branchName}
cd ..

#Copy camunda folder which contains dmn files
cp -r ./ia-task-configuration/src/main/resources .
rm -rf ./ia-task-configuration

./bin/import-dmn-diagram.sh . ia ia
