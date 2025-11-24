package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CASE_ID_LIST;

import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.IdamService;
import uk.gov.hmcts.reform.iacaseapi.domain.service.RoleAssignmentService;

@Slf4j
@Component
public class RemoveCaseManagerBulkHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final RoleAssignmentService roleAssignmentService;
    private final IdamService idamService;

    public RemoveCaseManagerBulkHandler(RoleAssignmentService roleAssignmentService,
                                        IdamService idamService) {
        this.roleAssignmentService = roleAssignmentService;
        this.idamService = idamService;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
            && callback.getEvent() == Event.REMOVE_CASE_MANAGER_BULK;
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }
        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        String caseIdList = asylumCase.read(CASE_ID_LIST, String.class)
            .orElse("");
        Arrays.stream(caseIdList.split(","))
            .forEach(caseId -> roleAssignmentService.removeCaseManagerRole(caseId, idamService.getServiceUserToken()));
        asylumCase.clear(CASE_ID_LIST);
        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
