package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.NonLegalRepDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Value;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.Assignment;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.RoleAssignmentResource;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.IdamService;
import uk.gov.hmcts.reform.iacaseapi.domain.service.RoleAssignmentService;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.idam.User;

@Component
@Slf4j
public class RevokeCitizenPreparer implements PreSubmitCallbackHandler<AsylumCase> {
    private final RoleAssignmentService roleAssignmentService;
    private final IdamService idamService;

    public RevokeCitizenPreparer(RoleAssignmentService roleAssignmentService,
                                 IdamService idamService) {
        this.roleAssignmentService = roleAssignmentService;
        this.idamService = idamService;
    }

    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {

        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_START
            && callback.getEvent() == Event.REVOKE_CITIZEN_ACCESS;
    }

    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback) {

        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        long caseId = callback.getCaseDetails().getId();

        RoleAssignmentResource roleAssignmentResource = roleAssignmentService
            .getUsersAssignedToCase(caseId);
        List<Assignment> assignmentList = roleAssignmentResource.getRoleAssignmentResponse();
        log.info("Found '{}' '[CREATOR]' case roles in the appeal with case ID {}",
            assignmentList.size(), caseId);

        if (assignmentList.isEmpty()) {
            return new PreSubmitCallbackResponse<>(asylumCase)
                .withError("No users have case access with caseId: " + caseId);
        }
        String nlrIdamId = asylumCase.read(AsylumCaseFieldDefinition.NLR_DETAILS, NonLegalRepDetails.class)
            .map(NonLegalRepDetails::getIdamId).orElse("");
        List<Value> userValueList = assignmentList
            .stream()
            .map(Assignment::getActorId)
            .map(idamService::getUserFromIdV1)
            .filter(Objects::nonNull)
            .filter(User::isActive)
            .map(user -> new Value(user.toValueId(), user.toRevokeAccessDlString(nlrIdamId)))
            .toList();

        if (userValueList.isEmpty()) {
            return new PreSubmitCallbackResponse<>(asylumCase)
                .withError("No citizen/non LR users with case access were found: " + caseId);
        }

        DynamicList usersDynamicList = new DynamicList(null, userValueList);
        asylumCase.write(AsylumCaseFieldDefinition.REVOKE_ACCESS_DL, usersDynamicList);
        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}