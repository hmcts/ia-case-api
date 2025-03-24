camundaBranch=${1:-master}
dmnBranch=${2:-master}
waStandaloneBranch=${3:-master}

echo "Loading Environment Variables"
source ./bin/variables/load-dev-user-preview-environment-variables.sh

echo "Importing Roles to the pod"
./bin/add-roles.sh

echo "Importing Org Roles to the pod"
./bin/add-org-roles-to-users.sh

echo "Importing Camunda definitions"
./bin/pull-latest-camunda-wa-files.sh ${waStandaloneBranch}
./bin/pull-latest-dmn-files.sh ${dmnBranch}

echo "ENV variables set for devuser-preview environment."
echo "XUI_URL: $XUI_WEBAPP_URL"
