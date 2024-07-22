#!/usr/bin/env bash

branchName=$1

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
