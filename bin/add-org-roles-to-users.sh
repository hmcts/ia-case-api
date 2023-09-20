#!/usr/bin/env bash

# Setup Users
echo ""
echo "Setting up WA Users and role assignments..."
./bin/utils/organisational-role-assignment.sh "${IA_WA_CASEOFFICER_USERNAME}" "${IA_WA_CASEOFFICER_PASSWORD}" "PUBLIC" "case-allocator" '{"jurisdiction":"IA","primaryLocation":"765324"}'
./bin/utils/organisational-role-assignment.sh "${IA_WA_CASEOFFICER_USERNAME}" "${IA_WA_CASEOFFICER_PASSWORD}" "PUBLIC" "task-supervisor" '{"jurisdiction":"IA","primaryLocation":"765324"}'
./bin/utils/organisational-role-assignment.sh "${IA_WA_CASEOFFICER_USERNAME}" "${IA_WA_CASEOFFICER_PASSWORD}" "PUBLIC" "tribunal-caseworker" '{"jurisdiction":"IA","primaryLocation":"765324"}'
