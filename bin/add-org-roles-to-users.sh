#!/usr/bin/env bash

# Setup Users
echo ""
echo "Setting up WA Users and role assignments..."
./bin/utils/organisational-role-assignment.sh "${IA_WA_CASEOFFICER_USERNAME}" "${IA_WA_CASEOFFICER_PASSWORD}" "PUBLIC" "case-allocator" '{"jurisdiction":"IA","primaryLocation":"765324"}' "LEGAL_OPERATIONS"
./bin/utils/organisational-role-assignment.sh "${IA_WA_CASEOFFICER_USERNAME}" "${IA_WA_CASEOFFICER_PASSWORD}" "PUBLIC" "task-supervisor" '{"jurisdiction":"IA","primaryLocation":"765324"}' "LEGAL_OPERATIONS"
./bin/utils/organisational-role-assignment.sh "${IA_WA_CASEOFFICER_USERNAME}" "${IA_WA_CASEOFFICER_PASSWORD}" "PUBLIC" "tribunal-caseworker" '{"jurisdiction":"IA","primaryLocation":"765324"}' "LEGAL_OPERATIONS"

./bin/utils/organisational-role-assignment.sh "${IA_WA_ADMINOFFICER_USERNAME}" "${IA_WA_ADMINOFFICER_PASSWORD}" "PUBLIC" "case-allocator" '{"jurisdiction":"IA","primaryLocation":"765324"}' "ADMIN"
./bin/utils/organisational-role-assignment.sh "${IA_WA_ADMINOFFICER_USERNAME}" "${IA_WA_ADMINOFFICER_PASSWORD}" "PUBLIC" "task-supervisor" '{"jurisdiction":"IA","primaryLocation":"765324"}' "ADMIN"
./bin/utils/organisational-role-assignment.sh "${IA_WA_ADMINOFFICER_USERNAME}" "${IA_WA_ADMINOFFICER_PASSWORD}" "PUBLIC" "hearing-centre-admin" '{"jurisdiction":"IA","primaryLocation":"765324"}' "ADMIN"
./bin/utils/organisational-role-assignment.sh "${IA_WA_ADMINOFFICER_USERNAME}" "${IA_WA_ADMINOFFICER_PASSWORD}" "PUBLIC" "hmcts-admin" '{"jurisdiction":"IA","primaryLocation":"765324"}' "ADMIN"

./bin/utils/organisational-role-assignment.sh "${IA_WA_CTSC_ADMIN_USERNAME}" "${IA_WA_CTSC_ADMIN_PASSWORD}" "PUBLIC" "ctsc" '{"jurisdiction":"IA","primaryLocation":"765324"}' "CTSC"
./bin/utils/organisational-role-assignment.sh "${IA_WA_CTSC_ADMIN_USERNAME}" "${IA_WA_CTSC_ADMIN_PASSWORD}" "PUBLIC" "case-allocator" '{"jurisdiction":"IA","primaryLocation":"765324"}' "CTSC"
./bin/utils/organisational-role-assignment.sh "${IA_WA_CTSC_ADMIN_USERNAME}" "${IA_WA_CTSC_ADMIN_PASSWORD}" "PUBLIC" "hmcts-ctsc" '{"jurisdiction":"IA","primaryLocation":"765324"}' "CTSC"
./bin/utils/organisational-role-assignment.sh "${IA_WA_CTSC_ADMIN_USERNAME}" "${IA_WA_CTSC_ADMIN_PASSWORD}" "PUBLIC" "task-supervisor" '{"jurisdiction":"IA","primaryLocation":"765324"}' "CTSC"

./bin/utils/organisational-role-assignment.sh "${TEST_JUDGE_X_USERNAME}" "${TEST_JUDGE_X_PASSWORD}" "PUBLIC" "leadership-judge" '{"jurisdiction":"IA","primaryLocation":"765324"}' "JUDICIAL"
./bin/utils/organisational-role-assignment.sh "${TEST_JUDGE_X_USERNAME}" "${TEST_JUDGE_X_PASSWORD}" "PUBLIC" "case-allocator" '{"jurisdiction":"IA","primaryLocation":"765324"}' "JUDICIAL"
./bin/utils/organisational-role-assignment.sh "${TEST_JUDGE_X_USERNAME}" "${TEST_JUDGE_X_PASSWORD}" "PUBLIC" "judge" '{"jurisdiction":"IA","primaryLocation":"765324"}' "JUDICIAL"
./bin/utils/organisational-role-assignment.sh "${TEST_JUDGE_X_USERNAME}" "${TEST_JUDGE_X_PASSWORD}" "PUBLIC" "task-supervisor" '{"jurisdiction":"IA","primaryLocation":"765324"}' "JUDICIAL"

./bin/utils/organisational-role-assignment.sh "${IA_WA_CASEOFFICER_USERNAME_STCW}" "${IA_WA_CASEOFFICER_PASSWORD_STCW}" "PUBLIC" "case-allocator" '{"jurisdiction":"IA","primaryLocation":"765324"}' "LEGAL_OPERATIONS"
./bin/utils/organisational-role-assignment.sh "${IA_WA_CASEOFFICER_USERNAME_STCW}" "${IA_WA_CASEOFFICER_PASSWORD_STCW}" "PUBLIC" "task-supervisor" '{"jurisdiction":"IA","primaryLocation":"765324"}' "LEGAL_OPERATIONS"
./bin/utils/organisational-role-assignment.sh "${IA_WA_CASEOFFICER_USERNAME_STCW}" "${IA_WA_CASEOFFICER_PASSWORD_STCW}" "PUBLIC" "tribunal-caseworker" '{"jurisdiction":"IA","primaryLocation":"765324"}' "LEGAL_OPERATIONS"

./bin/utils/organisational-role-assignment.sh "${IA_WA_ADMINOFFICER_USERNAME_1}" "${IA_WA_ADMINOFFICER_PASSWORD_1}" "PUBLIC" "case-allocator" '{"jurisdiction":"IA","primaryLocation":"765324"}' "ADMIN"
./bin/utils/organisational-role-assignment.sh "${IA_WA_ADMINOFFICER_USERNAME_1}" "${IA_WA_ADMINOFFICER_PASSWORD_1}" "PUBLIC" "task-supervisor" '{"jurisdiction":"IA","primaryLocation":"765324"}' "ADMIN"
./bin/utils/organisational-role-assignment.sh "${IA_WA_ADMINOFFICER_USERNAME_1}" "${IA_WA_ADMINOFFICER_PASSWORD_1}" "PUBLIC" "hearing-centre-admin" '{"jurisdiction":"IA","primaryLocation":"765324"}' "ADMIN"
./bin/utils/organisational-role-assignment.sh "${IA_WA_ADMINOFFICER_USERNAME_1}" "${IA_WA_ADMINOFFICER_PASSWORD_1}" "PUBLIC" "hmcts-admin" '{"jurisdiction":"IA","primaryLocation":"765324"}' "ADMIN"

./bin/utils/organisational-role-assignment.sh "${IA_WA_JUDGE_USERNAME}" "${IA_WA_JUDGE_PASSWORD}" "PUBLIC" "leadership-judge" '{"jurisdiction":"IA","primaryLocation":"765324"}' "JUDICIAL"
./bin/utils/organisational-role-assignment.sh "${IA_WA_JUDGE_USERNAME}" "${IA_WA_JUDGE_PASSWORD}" "PUBLIC" "case-allocator" '{"jurisdiction":"IA","primaryLocation":"765324"}' "JUDICIAL"
./bin/utils/organisational-role-assignment.sh "${IA_WA_JUDGE_USERNAME}" "${IA_WA_JUDGE_PASSWORD}" "PUBLIC" "judge" '{"jurisdiction":"IA","primaryLocation":"765324"}' "JUDICIAL"
./bin/utils/organisational-role-assignment.sh "${IA_WA_JUDGE_USERNAME}" "${IA_WA_JUDGE_PASSWORD}" "PUBLIC" "task-supervisor" '{"jurisdiction":"IA","primaryLocation":"765324"}' "JUDICIAL"
