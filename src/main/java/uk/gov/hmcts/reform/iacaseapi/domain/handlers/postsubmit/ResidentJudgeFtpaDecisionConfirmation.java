package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.UserDetailsProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserRole;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.Assignment;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.Attributes;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.Jurisdiction;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.QueryRequest;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.RoleAssignmentResource;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.RoleCategory;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.RoleName;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.RoleType;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PostSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.RoleAssignmentService;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@Slf4j
public class ResidentJudgeFtpaDecisionConfirmation implements PostSubmitCallbackHandler<AsylumCase> {

    public static final String DLRM_SETASIDE_FEATURE_FLAG = "dlrm-setaside-feature-flag";
    private final FeatureToggler featureToggler;
    private final RoleAssignmentService roleAssignmentService;
    private final UserDetailsProvider userDetailsProvider;

    ResidentJudgeFtpaDecisionConfirmation(FeatureToggler featureToggler,
                                          RoleAssignmentService roleAssignmentService,
                                          UserDetailsProvider userDetailsProvider) {
        this.featureToggler = featureToggler;
        this.roleAssignmentService = roleAssignmentService;
        this.userDetailsProvider = userDetailsProvider;
    }

    public boolean canHandle(
            Callback<AsylumCase> callback
    ) {
        requireNonNull(callback, "callback must not be null");

        return callback.getEvent() == Event.RESIDENT_JUDGE_FTPA_DECISION || callback.getEvent() == Event.DECIDE_FTPA_APPLICATION;
    }

    public PostSubmitCallbackResponse handle(
            Callback<AsylumCase> callback
    ) {
        if (!canHandle(callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase =
                callback
                        .getCaseDetails()
                        .getCaseData();


        final String ftpaApplicantType =
                asylumCase
                        .read(FTPA_APPLICANT_TYPE, String.class)
                        .orElseThrow(() -> new IllegalStateException("FtpaApplicantType is not present"));

        PostSubmitCallbackResponse postSubmitResponse =
                new PostSubmitCallbackResponse();

        postSubmitResponse.setConfirmationHeader("# You've recorded the First-tier permission to appeal decision");

        String ftpaOutcomeType = asylumCase.read(ftpaApplicantType.equals("appellant") == true ? FTPA_APPELLANT_RJ_DECISION_OUTCOME_TYPE : FTPA_RESPONDENT_RJ_DECISION_OUTCOME_TYPE, String.class)
                .orElseThrow(() -> new IllegalStateException("ftpaDecisionOutcomeType is not present"));

        boolean isDlrmSetAside
                = featureToggler.getValue(DLRM_SETASIDE_FEATURE_FLAG, false);

        removeCaseManagerRole(callback);

        switch (ftpaOutcomeType) {

            case "granted":
            case "partiallyGranted":
                postSubmitResponse.setConfirmationBody(
                        "#### What happens next\n\n"
                                + "Both parties have been notified of the decision. The Upper Tribunal has also been notified, and will now proceed with the case.<br>"
                );
                break;

            case "refused":
            case "notAdmitted":
                postSubmitResponse.setConfirmationBody(
                        "#### What happens next\n\n"
                                + "Both parties have been notified that permission was refused. They'll also be able to access this information in the FTPA tab.<br>"
                );
                break;

            case "reheardRule32":
            case "reheardRule35":
                if (isDlrmSetAside) {
                    postSubmitResponse.setConfirmationBody(
                            "#### What happens next\n\n"
                                    + "Both parties will be notified of the decision. A Legal Officer will review any Tribunal instructions and then relist the case.<br>"
                    );
                } else {
                    postSubmitResponse.setConfirmationBody(
                            "#### What happens next\n\n"
                                    + "Both parties will be notified of the decision. A Caseworker will review any Tribunal instructions and then relist the case.<br>"
                    );
                }
                break;

            case "remadeRule31":
            case "remadeRule32":

                if (isDlrmSetAside) {
                    postSubmitResponse.setConfirmationHeader("# You've disposed of the application");
                    postSubmitResponse.setConfirmationBody(
                            "#### What happens next\n\n"
                                    + "A Judge will update the decision.<br>"
                    );
                } else {
                    postSubmitResponse.setConfirmationBody(
                            "#### What happens next\n\n"
                                    + "Both parties have been notified of the decision.<br>"
                    );
                }

                break;

            default:
                throw new IllegalStateException("FtpaDecisionOutcome is not present");
        }

        return postSubmitResponse;
    }

    private void removeCaseManagerRole(Callback<AsylumCase> callback) {
        List<String> userRoles = userDetailsProvider.getUserDetails().getRoles();
        List<String> rolesForRemoval = List.of(UserRole.JUDGE.name(), UserRole.JUDICIARY.name());
        if (userRoles.stream().noneMatch(rolesForRemoval::contains)) {
            log.info("User is not a Judge or Judicial user. Case Manager role was not removed.");
            return;
        }

        String caseId = String.valueOf(callback.getCaseDetails().getId());
        QueryRequest queryRequest = QueryRequest.builder()
            .roleType(List.of(RoleType.CASE))
            .roleName(List.of(RoleName.CASE_MANAGER))
            .roleCategory(List.of(RoleCategory.JUDICIAL))
            .attributes(Map.of(
                Attributes.JURISDICTION, List.of(Jurisdiction.IA.name()),
                Attributes.CASE_TYPE, List.of("Asylum"),
                Attributes.CASE_ID, List.of(caseId)
            ))
            .build();
        log.debug("Query role assignment with the parameters: {}", queryRequest);

        RoleAssignmentResource roleAssignmentResource = roleAssignmentService.queryRoleAssignments(queryRequest);
        Optional<Assignment> roleAssignment = roleAssignmentResource.getRoleAssignmentResponse().stream().findFirst();

        if (roleAssignment.isPresent()) {
            String actorId = roleAssignment.get().getActorId();
            log.info("Removing Case Manager role from Judicial user {} for case ID {}", actorId, caseId);

            roleAssignmentService.deleteRoleAssignment(roleAssignment.get().getId());

            log.info("Successfully removed Case Manager role from Judicial user {} for case ID {}", actorId, caseId);
        } else {
            log.error("Problem removing Case Manager role for case ID {}. Role assignment for user not found", caseId);
        }
    }
}

