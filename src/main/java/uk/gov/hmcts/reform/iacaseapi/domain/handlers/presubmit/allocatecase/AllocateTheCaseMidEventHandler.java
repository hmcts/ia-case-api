package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.allocatecase;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CASE_WORKER_LOCATION_LIST;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CASE_WORKER_NAME_LIST;

import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Value;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.Assignment;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.CaseWorkerService;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;

@Component
public class AllocateTheCaseMidEventHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final FeatureToggler featureToggler;
    private final CaseWorkerService caseWorkerService;

    public AllocateTheCaseMidEventHandler(
        FeatureToggler featureToggler,
        CaseWorkerService caseWorkerService) {
        this.featureToggler = featureToggler;
        this.caseWorkerService = caseWorkerService;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.MID_EVENT
            && callback.getEvent() == Event.ALLOCATE_THE_CASE
            && featureToggler.getValue("allocate-a-case-feature", false);
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        String securityClassification = callback.getCaseDetails().getSecurityClassification();
        return populateDynamicListWithCaseWorkerNamesForSelectedLocation(asylumCase, securityClassification);
    }

    private PreSubmitCallbackResponse<AsylumCase> populateDynamicListWithCaseWorkerNamesForSelectedLocation(
        AsylumCase asylumCase,
        String securityClassification
    ) {
        String selectedLocation = asylumCase.read(CASE_WORKER_LOCATION_LIST, String.class)
            .orElseThrow(() -> new RuntimeException("caseWorkerLocationList field is not present on the caseData"));

        return getCallbackResponse(asylumCase, getCaseWorkerValueListForGivenLocation(
            selectedLocation,
            securityClassification
        ));
    }

    private PreSubmitCallbackResponse<AsylumCase> getCallbackResponse(
        AsylumCase asylumCase,
        List<Value> caseWorkerValueListForGivenLocation
    ) {
        PreSubmitCallbackResponse<AsylumCase> response = new PreSubmitCallbackResponse<>(asylumCase);
        if (caseWorkerValueListForGivenLocation.isEmpty()) {
            response.addError("There are no caseworkers for the selected location. Select a different location.");
        } else {
            asylumCase.write(
                CASE_WORKER_NAME_LIST,
                new DynamicList(new Value("", ""), caseWorkerValueListForGivenLocation)
            );
        }
        return response;
    }

    private List<Value> getCaseWorkerValueListForGivenLocation(String location, String securityClassification) {
        List<Assignment> roleAssignments = caseWorkerService.getRoleAssignmentsPerLocationAndClassification(
            location,
            securityClassification
        );

        return roleAssignments.stream()
            .map(role -> caseWorkerService.getCaseWorkerNameForActorId(role.getActorId()))
            .filter(caseWorkerName -> StringUtils.isNotEmpty(StringUtils.trimToEmpty(caseWorkerName.getName())))
            .map(caseWorkerName -> new Value(caseWorkerName.getId(), caseWorkerName.getName()))
            .collect(Collectors.toList());
    }

}
