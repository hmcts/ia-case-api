camundaBranch=${1:-master}
dmnBranch=${2:-master}
waStandaloneBranch=${3:-master}

echo "Loading Environment Variables"
source ./bin/variables/load-dev-user-preview-environment-variables.sh

echo "Importing Roles to the pod"
./bin/add-roles.sh

echo "Importing Org Roles to the pod"
./bin/add-org-roles-to-users.sh

#echo "Importing CCD definitions"
#./bin/build-release-ccd-definition.sh preview
#ccdDefinitionFilePath="$(pwd)/build/ccd-release-config/civil-ccd-preview.xlsx"
#./bin/utils/ccd-import-definition.sh ${ccdDefinitionFilePath}
#rm -rf $(pwd)/build/ccd-release-config

echo "Importing Camunda definitions"
#./bin/pull-latest-camunda-files.sh ${camundaBranch}
./bin/pull-latest-camunda-wa-files.sh ${waStandaloneBranch}
./bin/pull-latest-dmn-files.sh ${dmnBranch}
#\rm -rf $(pwd)/camunda

echo "ENV variables set for devuser-preview environment."
echo "XUI_URL: $XUI_WEBAPP_URL"
