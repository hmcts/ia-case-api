package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.allocatecase;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CASE_WORKER_LOCATION_LIST;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CASE_WORKER_NAME_LIST;

import lombok.extern.slf4j.Slf4j;
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
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class AllocateTheCaseToCaseWorkerMidEventHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final FeatureToggler featureToggler;
    private final CaseWorkerService caseWorkerService;
    private final AllocateTheCaseService allocateTheCaseService;

    public AllocateTheCaseToCaseWorkerMidEventHandler(
        FeatureToggler featureToggler,
        CaseWorkerService caseWorkerService,
        AllocateTheCaseService allocateTheCaseService
    ) {
        this.featureToggler = featureToggler;
        this.caseWorkerService = caseWorkerService;
        this.allocateTheCaseService = allocateTheCaseService;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");
        log.info("canHandle method...");

        boolean allocateToCaseWorkerOption = allocateTheCaseService
            .isAllocateToCaseWorkerOption(callback.getCaseDetails().getCaseData());
        log.info("allocateToCaseWorkerOption: {}", allocateToCaseWorkerOption);

        boolean midEvent = callbackStage == PreSubmitCallbackStage.MID_EVENT;
        log.info("midEvent: {}", midEvent);

        boolean event = callback.getEvent() == Event.ALLOCATE_THE_CASE;
        log.info("event: {}", event);

        boolean featureTogglerValue = featureToggler.getValue("allocate-a-case-feature", false);
        log.info("featureTogglerValue: {}", featureTogglerValue);

        return allocateToCaseWorkerOption && midEvent && event && featureTogglerValue;
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        log.info("handle method...");

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
        log.info("populateDynamicListWithCaseWorkerNamesForSelectedLocation method...");
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
        log.info("getCallbackResponse method...");
        PreSubmitCallbackResponse<AsylumCase> response = new PreSubmitCallbackResponse<>(asylumCase);
        if (caseWorkerValueListForGivenLocation.isEmpty()) {
            log.info("caseworker list is empty...");
            response.addError("There are no users for the location you have selected. Choose another location to continue.");
        } else {
            log.info("caseworker list has values...");
            log.info(caseWorkerValueListForGivenLocation.toString());
            asylumCase.write(
                CASE_WORKER_NAME_LIST,
                new DynamicList(new Value("", ""), caseWorkerValueListForGivenLocation)
            );
        }
        return response;
    }

    private List<Value> getCaseWorkerValueListForGivenLocation(String location, String securityClassification) {
        log.info("getCaseWorkerValueListForGivenLocation method...");
        List<Assignment> roleAssignments = caseWorkerService.getRoleAssignmentsPerLocationAndClassification(
            location,
            securityClassification
        );
        return roleAssignments.stream()
            .map(role -> caseWorkerService.getCaseWorkerNameForActorId(role.getActorId()))
            .filter(caseWorkerName -> StringUtils.isNotEmpty(caseWorkerName.getFormattedName()))
            .map(caseWorkerName -> new Value(caseWorkerName.getId(), caseWorkerName.getFormattedName()))
            .collect(Collectors.toList());
    }

}
