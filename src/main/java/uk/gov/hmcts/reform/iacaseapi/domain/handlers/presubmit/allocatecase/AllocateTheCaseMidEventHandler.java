package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.allocatecase;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CASE_WORKER_LOCATION_LIST;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CASE_WORKER_NAME_LIST;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Value;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.QueryRequest;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.RoleAssignmentResource;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.RoleAssignmentService;

@Component
public class AllocateTheCaseMidEventHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final FeatureToggler featureToggler;
    private final RoleAssignmentService roleAssignmentService;

    public AllocateTheCaseMidEventHandler(
        FeatureToggler featureToggler,
        RoleAssignmentService roleAssignmentService
    ) {
        this.featureToggler = featureToggler;
        this.roleAssignmentService = roleAssignmentService;
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

        String location = asylumCase.read(CASE_WORKER_LOCATION_LIST, String.class)
            .orElseThrow(() -> new RuntimeException("caseWorkerLocationList field is not present on the caseData"));

        populateDynamicListWithCaseWorkerNamesForSelectedLocation(asylumCase, location);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private void populateDynamicListWithCaseWorkerNamesForSelectedLocation(AsylumCase asylumCase, String location) {
        asylumCase.write(
            CASE_WORKER_NAME_LIST,
            new DynamicList(new Value("", ""), getCaseWorkerListForGivenLocation(location))
        );
    }

    private List<Value> getCaseWorkerListForGivenLocation(String location) {
        RoleAssignmentResource roleAssignments = roleAssignmentService
            .queryRoleAssignments(QueryRequest.builder().build());

        return roleAssignments.getRoleAssignmentResponse().stream()
            .map(role -> new Value(role.getActorId(), role.getActorId()))
            .collect(Collectors.toList());
    }

}
