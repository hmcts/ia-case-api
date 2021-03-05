package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.allocatecase;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.ALLOCATE_THE_CASE_TO;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CASE_WORKER_LOCATION_LIST;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CASE_WORKER_NAME;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CASE_WORKER_NAME_LIST;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.RoleAssignmentService;

@Component
public class AllocateTheCaseToCaseWorkerHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final RoleAssignmentService roleAssignmentService;
    private final FeatureToggler featureToggler;
    private final AllocateTheCaseService allocateTheCaseService;

    public AllocateTheCaseToCaseWorkerHandler(
        RoleAssignmentService roleAssignmentService,
        FeatureToggler featureToggler,
        AllocateTheCaseService allocateTheCaseService
    ) {
        this.roleAssignmentService = roleAssignmentService;
        this.featureToggler = featureToggler;
        this.allocateTheCaseService = allocateTheCaseService;
    }

    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return allocateTheCaseService.isAllocateToCaseWorkerOption(callback.getCaseDetails().getCaseData())
            && callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
            && callback.getEvent() == Event.ALLOCATE_THE_CASE
            && featureToggler.getValue("allocate-a-case-feature", false);
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(PreSubmitCallbackStage callbackStage,
                                                        Callback<AsylumCase> callback) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        CaseDetails<AsylumCase> caseDetails = callback.getCaseDetails();
        AsylumCase caseData = caseDetails.getCaseData();

        DynamicList caseWorkerNameList = caseData.read(
            CASE_WORKER_NAME_LIST,
            DynamicList.class
        ).orElseThrow(() -> new RuntimeException("caseWorkerNameList field is not present on the caseData"));

        roleAssignmentService.assignRole(caseDetails.getId(), caseWorkerNameList.getValue().getCode());

        caseData.write(CASE_WORKER_NAME, caseWorkerNameList.getValue().getLabel());

        caseData.clear(CASE_WORKER_NAME_LIST);
        caseData.clear(CASE_WORKER_LOCATION_LIST);
        caseData.clear(ALLOCATE_THE_CASE_TO);

        return new PreSubmitCallbackResponse<>(caseData);
    }
}
