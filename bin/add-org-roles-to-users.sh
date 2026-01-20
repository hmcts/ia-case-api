#!/usr/bin/env bash

# Setup Users
echo ""p
echo "Setting up WA Users and role assignments..."
./bin/utils/organisational-role-assignment.sh "${IA_WA_CASEOFFICER_USERNAME}" "${IA_WA_CASEOFFICER_PASSWORD}" "PUBLIC" "case-allocator" '{"jurisdiction":"IA","primaryLocation":"765324"}' "LEGAL_OPERATIONS"
./bin/utils/organisational-role-assignment.sh "${IA_WA_CASEOFFICER_USERNAME}" "${IA_WA_CASEOFFICER_PASSWORD}" "PUBLIC" "task-supervisor" '{"jurisdiction":"IA","primaryLocation":"765324"}' "LEGAL_OPERATIONS"
./bin/utils/organisational-role-assignment.sh "${IA_WA_CASEOFFICER_USERNAME}" "${IA_WA_CASEOFFICER_PASSWORD}" "PUBLIC" "tribunal-caseworker" '{"jurisdiction":"IA","primaryLocation":"765324","workTypes":"hearing_work,routine_work,decision_making_work,applications,stf_24w_hearing_work,stf_24w_routine_work,stf_24w_decision_making_work,stf_24w_applications"}' "LEGAL_OPERATIONS"

./bin/utils/organisational-role-assignment.sh "${IA_WA_ADMINOFFICER_USERNAME}" "${IA_WA_ADMINOFFICER_PASSWORD}" "PUBLIC" "case-allocator" '{"jurisdiction":"IA","primaryLocation":"765324"}' "ADMIN"
./bin/utils/organisational-role-assignment.sh "${IA_WA_ADMINOFFICER_USERNAME}" "${IA_WA_ADMINOFFICER_PASSWORD}" "PUBLIC" "task-supervisor" '{"jurisdiction":"IA","primaryLocation":"765324"}' "ADMIN"
./bin/utils/organisational-role-assignment.sh "${IA_WA_ADMINOFFICER_USERNAME}" "${IA_WA_ADMINOFFICER_PASSWORD}" "PUBLIC" "hearing-centre-admin" '{"jurisdiction":"IA","primaryLocation":"765324","workTypes":"hearing_work,upper_tribunal,routine_work,review_case,stf_24w_hearing_work,stf_24w_upper_tribunal,stf_24w_routine_work,stf_24w_review_case"}' "ADMIN"
./bin/utils/organisational-role-assignment.sh "${IA_WA_ADMINOFFICER_USERNAME}" "${IA_WA_ADMINOFFICER_PASSWORD}" "PUBLIC" "hmcts-admin" '{"jurisdiction":"IA","primaryLocation":"765324"}' "ADMIN"

./bin/utils/organisational-role-assignment.sh "${IA_WA_CTSC_ADMIN_USERNAME}" "${IA_WA_CTSC_ADMIN_PASSWORD}" "PUBLIC" "ctsc" '{"jurisdiction":"IA","primaryLocation":"765324","workTypes":"hearing_work,upper_tribunal,routine_work,stf_24w_hearing_work,stf_24w_upper_tribunal,stf_24w_routine_work"}' "CTSC"
./bin/utils/organisational-role-assignment.sh "${IA_WA_CTSC_ADMIN_USERNAME}" "${IA_WA_CTSC_ADMIN_PASSWORD}" "PUBLIC" "case-allocator" '{"jurisdiction":"IA","primaryLocation":"765324"}' "CTSC"
./bin/utils/organisational-role-assignment.sh "${IA_WA_CTSC_ADMIN_USERNAME}" "${IA_WA_CTSC_ADMIN_PASSWORD}" "PUBLIC" "hmcts-ctsc" '{"jurisdiction":"IA","primaryLocation":"765324"}' "CTSC"
./bin/utils/organisational-role-assignment.sh "${IA_WA_CTSC_ADMIN_USERNAME}" "${IA_WA_CTSC_ADMIN_PASSWORD}" "PUBLIC" "task-supervisor" '{"jurisdiction":"IA","primaryLocation":"765324"}' "CTSC"

./bin/utils/organisational-role-assignment.sh "${TEST_JUDGE_X_USERNAME}" "${TEST_JUDGE_X_PASSWORD}" "PUBLIC" "leadership-judge" '{"jurisdiction":"IA","primaryLocation":"765324","workTypes":"hearing_work,upper_tribunal,decision_making_work,applications,access_requests,stf_24w_hearing_work,stf_24w_upper_tribunal,stf_24w_decision_making_work,stf_24w_applications,stf_24w_access_requests"}' "JUDICIAL"
./bin/utils/organisational-role-assignment.sh "${TEST_JUDGE_X_USERNAME}" "${TEST_JUDGE_X_PASSWORD}" "PUBLIC" "case-allocator" '{"jurisdiction":"IA","primaryLocation":"765324"}' "JUDICIAL"
./bin/utils/organisational-role-assignment.sh "${TEST_JUDGE_X_USERNAME}" "${TEST_JUDGE_X_PASSWORD}" "PUBLIC" "judge" '{"jurisdiction":"IA","primaryLocation":"765324","workTypes":"hearing_work,upper_tribunal,decision_making_work,applications,stf_24w_hearing_work,stf_24w_upper_tribunal,stf_24w_decision_making_work,stf_24w_applications"}' "JUDICIAL"
./bin/utils/organisational-role-assignment.sh "${TEST_JUDGE_X_USERNAME}" "${TEST_JUDGE_X_PASSWORD}" "PUBLIC" "task-supervisor" '{"jurisdiction":"IA","primaryLocation":"765324"}' "JUDICIAL"

./bin/utils/organisational-role-assignment.sh "${IA_WA_CASEOFFICER_USERNAME_STCW}" "${IA_WA_CASEOFFICER_PASSWORD_STCW}" "PUBLIC" "case-allocator" '{"jurisdiction":"IA","primaryLocation":"765324"}' "LEGAL_OPERATIONS"
./bin/utils/organisational-role-assignment.sh "${IA_WA_CASEOFFICER_USERNAME_STCW}" "${IA_WA_CASEOFFICER_PASSWORD_STCW}" "PUBLIC" "task-supervisor" '{"jurisdiction":"IA","primaryLocation":"765324"}' "LEGAL_OPERATIONS"
./bin/utils/organisational-role-assignment.sh "${IA_WA_CASEOFFICER_USERNAME_STCW}" "${IA_WA_CASEOFFICER_PASSWORD_STCW}" "PUBLIC" "hearing-viewer" '{"jurisdiction":"IA","primaryLocation":"765324"}' "LEGAL_OPERATIONS"
./bin/utils/organisational-role-assignment.sh "${IA_WA_CASEOFFICER_USERNAME_STCW}" "${IA_WA_CASEOFFICER_PASSWORD_STCW}" "PUBLIC" "hearing-manager" '{"jurisdiction":"IA","primaryLocation":"765324"}' "LEGAL_OPERATIONS"
./bin/utils/organisational-role-assignment.sh "${IA_WA_CASEOFFICER_USERNAME_STCW}" "${IA_WA_CASEOFFICER_PASSWORD_STCW}" "PUBLIC" "tribunal-caseworker" '{"jurisdiction":"IA","primaryLocation":"765324","workTypes":"hearing_work,routine_work,decision_making_work,applications,stf_24w_hearing_work,stf_24w_routine_work,stf_24w_decision_making_work,stf_24w_applications"}' "LEGAL_OPERATIONS"

./bin/utils/organisational-role-assignment.sh "${IA_WA_ADMINOFFICER_USERNAME_1}" "${IA_WA_ADMINOFFICER_PASSWORD_1}" "PUBLIC" "case-allocator" '{"jurisdiction":"IA","primaryLocation":"765324"}' "ADMIN"
./bin/utils/organisational-role-assignment.sh "${IA_WA_ADMINOFFICER_USERNAME_1}" "${IA_WA_ADMINOFFICER_PASSWORD_1}" "PUBLIC" "task-supervisor" '{"jurisdiction":"IA","primaryLocation":"765324"}' "ADMIN"
./bin/utils/organisational-role-assignment.sh "${IA_WA_ADMINOFFICER_USERNAME_1}" "${IA_WA_ADMINOFFICER_PASSWORD_1}" "PUBLIC" "hearing-centre-admin" '{"jurisdiction":"IA","primaryLocation":"765324","workTypes":"hearing_work,upper_tribunal,routine_work,review_case,stf_24w_hearing_work,stf_24w_upper_tribunal,stf_24w_routine_work,stf_24w_review_case"}' "ADMIN"
./bin/utils/organisational-role-assignment.sh "${IA_WA_ADMINOFFICER_USERNAME_1}" "${IA_WA_ADMINOFFICER_PASSWORD_1}" "PUBLIC" "hmcts-admin" '{"jurisdiction":"IA","primaryLocation":"765324"}' "ADMIN"
./bin/utils/organisational-role-assignment.sh "${IA_WA_ADMINOFFICER_USERNAME_1}" "${IA_WA_ADMINOFFICER_PASSWORD_1}" "PUBLIC" "hearing-manager" '{"jurisdiction":"IA","primaryLocation":"765324"}' "ADMIN"
./bin/utils/organisational-role-assignment.sh "${IA_WA_ADMINOFFICER_USERNAME_1}" "${IA_WA_ADMINOFFICER_PASSWORD_1}" "PUBLIC" "hearing-viewer" '{"jurisdiction":"IA","primaryLocation":"765324"}' "ADMIN"

./bin/utils/organisational-role-assignment.sh "${IA_WA_JUDGE_USERNAME}" "${IA_WA_JUDGE_PASSWORD}" "PUBLIC" "leadership-judge" '{"jurisdiction":"IA","primaryLocation":"765324","workTypes":"hearing_work,upper_tribunal,decision_making_work,applications,access_requests,stf_24w_hearing_work,stf_24w_upper_tribunal,stf_24w_decision_making_work,stf_24w_applications,stf_24w_access_requests"}' "JUDICIAL"
./bin/utils/organisational-role-assignment.sh "${IA_WA_JUDGE_USERNAME}" "${IA_WA_JUDGE_PASSWORD}" "PUBLIC" "case-allocator" '{"jurisdiction":"IA","primaryLocation":"765324"}' "JUDICIAL"
./bin/utils/organisational-role-assignment.sh "${IA_WA_JUDGE_USERNAME}" "${IA_WA_JUDGE_PASSWORD}" "PUBLIC" "judge" '{"jurisdiction":"IA","primaryLocation":"765324","workTypes":"hearing_work,upper_tribunal,decision_making_work,applications,stf_24w_hearing_work,stf_24w_upper_tribunal,stf_24w_decision_making_work,stf_24w_applications"}' "JUDICIAL"
./bin/utils/organisational-role-assignment.sh "${IA_WA_JUDGE_USERNAME}" "${IA_WA_JUDGE_PASSWORD}" "PUBLIC" "task-supervisor" '{"jurisdiction":"IA","primaryLocation":"765324"}' "JUDICIAL"

./bin/utils/organisational-role-assignment.sh "${IA_WA_CTSC_ADMIN_USERNAME_2}" "${IA_WA_CTSC_ADMIN_PASSWORD_2}" "PUBLIC" "ctsc" '{"jurisdiction":"IA","primaryLocation":"765324","workTypes":"hearing_work,upper_tribunal,routine_work,stf_24w_hearing_work,stf_24w_upper_tribunal,stf_24w_routine_work"}' "CTSC"
./bin/utils/organisational-role-assignment.sh "${IA_WA_CTSC_ADMIN_USERNAME_2}" "${IA_WA_CTSC_ADMIN_PASSWORD_2}" "PUBLIC" "case-allocator" '{"jurisdiction":"IA","primaryLocation":"765324"}' "CTSC"
./bin/utils/organisational-role-assignment.sh "${IA_WA_CTSC_ADMIN_USERNAME_2}" "${IA_WA_CTSC_ADMIN_PASSWORD_2}" "PUBLIC" "hmcts-ctsc" '{"jurisdiction":"IA","primaryLocation":"765324"}' "CTSC"
./bin/utils/organisational-role-assignment.sh "${IA_WA_CTSC_ADMIN_USERNAME_2}" "${IA_WA_CTSC_ADMIN_PASSWORD_2}" "PUBLIC" "task-supervisor" '{"jurisdiction":"IA","primaryLocation":"765324"}' "CTSC"
./bin/utils/organisational-role-assignment.sh "${IA_WA_CTSC_ADMIN_USERNAME_2}" "${IA_WA_CTSC_ADMIN_PASSWORD_2}" "PUBLIC" "hearing-viewer" '{"jurisdiction":"IA","primaryLocation":"765324"}' "CTSC"
./bin/utils/organisational-role-assignment.sh "${IA_WA_CTSC_ADMIN_USERNAME_2}" "${IA_WA_CTSC_ADMIN_PASSWORD_2}" "PUBLIC" "hearing-manager" '{"jurisdiction":"IA","primaryLocation":"765324"}' "CTSC"

./bin/utils/organisational-role-assignment.sh "${IA_WA_JUDGE_NO_IDAM_USERNAME}" "${IA_WA_JUDGE_NO_IDAM_PASSWORD}" "PUBLIC" "leadership-judge" '{"jurisdiction":"IA","primaryLocation":"765324","workTypes":"hearing_work,upper_tribunal,decision_making_work,applications,access_requests,stf_24w_hearing_work,stf_24w_upper_tribunal,stf_24w_decision_making_work,stf_24w_applications,stf_24w_access_requests"}' "JUDICIAL"
./bin/utils/organisational-role-assignment.sh "${IA_WA_JUDGE_NO_IDAM_USERNAME}" "${IA_WA_JUDGE_NO_IDAM_PASSWORD}" "PUBLIC" "case-allocator" '{"jurisdiction":"IA","primaryLocation":"765324"}' "JUDICIAL"
./bin/utils/organisational-role-assignment.sh "${IA_WA_JUDGE_NO_IDAM_USERNAME}" "${IA_WA_JUDGE_NO_IDAM_PASSWORD}" "PUBLIC" "judge" '{"jurisdiction":"IA","primaryLocation":"765324","workTypes":"hearing_work,upper_tribunal,decision_making_work,applications,stf_24w_hearing_work,stf_24w_upper_tribunal,stf_24w_decision_making_work,stf_24w_applications"}' "JUDICIAL"
./bin/utils/organisational-role-assignment.sh "${IA_WA_JUDGE_NO_IDAM_USERNAME}" "${IA_WA_JUDGE_NO_IDAM_PASSWORD}" "PUBLIC" "task-supervisor" '{"jurisdiction":"IA","primaryLocation":"765324"}' "JUDICIAL"

./bin/utils/organisational-role-assignment.sh "${IA_WA_ADMINOFFICER_NO_IDAM_USERNAME}" "${IA_WA_ADMINOFFICER_NO_IDAM_PASSWORD}" "PUBLIC" "case-allocator" '{"jurisdiction":"IA","primaryLocation":"765324"}' "ADMIN"
./bin/utils/organisational-role-assignment.sh "${IA_WA_ADMINOFFICER_NO_IDAM_USERNAME}" "${IA_WA_ADMINOFFICER_NO_IDAM_PASSWORD}" "PUBLIC" "task-supervisor" '{"jurisdiction":"IA","primaryLocation":"765324"}' "ADMIN"
./bin/utils/organisational-role-assignment.sh "${IA_WA_ADMINOFFICER_NO_IDAM_USERNAME}" "${IA_WA_ADMINOFFICER_NO_IDAM_PASSWORD}" "PUBLIC" "hearing-centre-admin" '{"jurisdiction":"IA","primaryLocation":"765324","workTypes":"hearing_work,upper_tribunal,routine_work,review_case,stf_24w_hearing_work,stf_24w_upper_tribunal,stf_24w_routine_work,stf_24w_review_case"}' "ADMIN"
./bin/utils/organisational-role-assignment.sh "${IA_WA_ADMINOFFICER_NO_IDAM_USERNAME}" "${IA_WA_ADMINOFFICER_NO_IDAM_PASSWORD}" "PUBLIC" "hmcts-admin" '{"jurisdiction":"IA","primaryLocation":"765324"}' "ADMIN"

./bin/utils/organisational-role-assignment.sh "${IA_WA_CASEOFFICER_NO_IDAM_USERNAME}" "${IA_WA_CASEOFFICER_NO_IDAM_PASSWORD}" "PUBLIC" "case-allocator" '{"jurisdiction":"IA","primaryLocation":"765324"}' "LEGAL_OPERATIONS"
./bin/utils/organisational-role-assignment.sh "${IA_WA_CASEOFFICER_NO_IDAM_USERNAME}" "${IA_WA_CASEOFFICER_NO_IDAM_PASSWORD}" "PUBLIC" "task-supervisor" '{"jurisdiction":"IA","primaryLocation":"765324"}' "LEGAL_OPERATIONS"
./bin/utils/organisational-role-assignment.sh "${IA_WA_CASEOFFICER_NO_IDAM_USERNAME}" "${IA_WA_CASEOFFICER_NO_IDAM_PASSWORD}" "PUBLIC" "tribunal-caseworker" '{"jurisdiction":"IA","primaryLocation":"765324","workTypes":"hearing_work,routine_work,decision_making_work,applications,stf_24w_hearing_work,stf_24w_routine_work,stf_24w_decision_making_work,stf_24w_applications"}' "LEGAL_OPERATIONS"

echo -e "\nAdding system user roles..."
./bin/utils/system-hearings-user-role-assignment.sh "${IA_SYSTEM_USERNAME}" "${IA_SYSTEM_PASSWORD}"
./bin/utils/system-allocator-user-role-assignment.sh "${IA_SYSTEM_USERNAME}" "${IA_SYSTEM_PASSWORD}"
