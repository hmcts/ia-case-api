package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.allocatecase;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CASE_WORKER_LOCATION_LIST;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CASE_WORKER_NAME_LIST;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.Classification.PRIVATE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.Classification.PUBLIC;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.Classification.RESTRICTED;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Value;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.Attributes;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.Classification;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.GrantType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.QueryRequest;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.RoleAssignmentResource;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.RoleName;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.RoleType;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.refdata.CaseWorkerProfile;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.refdata.UserIds;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.refdata.RefDataCaseWorkerApi;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.roleassignment.RoleAssignmentService;

@Component
public class AllocateTheCaseMidEventHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final FeatureToggler featureToggler;
    private final RoleAssignmentService roleAssignmentService;
    private final RefDataCaseWorkerApi refDataCaseWorkerApi;
    private final AuthTokenGenerator serviceAuthTokenGenerator;
    private final UserDetails userDetails;

    public AllocateTheCaseMidEventHandler(
        FeatureToggler featureToggler,
        RoleAssignmentService roleAssignmentService,
        RefDataCaseWorkerApi refDataCaseWorkerApi,
        AuthTokenGenerator serviceAuthTokenGenerator,
        UserDetails userDetails
    ) {
        this.featureToggler = featureToggler;
        this.roleAssignmentService = roleAssignmentService;
        this.refDataCaseWorkerApi = refDataCaseWorkerApi;
        this.serviceAuthTokenGenerator = serviceAuthTokenGenerator;
        this.userDetails = userDetails;
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
        populateDynamicListWithCaseWorkerNamesForSelectedLocation(asylumCase, securityClassification);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private void populateDynamicListWithCaseWorkerNamesForSelectedLocation(AsylumCase asylumCase,
                                                                           String securityClassification) {
        String selectedLocation = asylumCase.read(CASE_WORKER_LOCATION_LIST, String.class)
            .orElseThrow(() -> new RuntimeException("caseWorkerLocationList field is not present on the caseData"));

        asylumCase.write(
            CASE_WORKER_NAME_LIST,
            new DynamicList(
                new Value("", ""),
                getCaseWorkerListForGivenLocation(selectedLocation, securityClassification)
            )
        );
    }

    private List<Value> getCaseWorkerListForGivenLocation(String location, String securityClassification) {
        RoleAssignmentResource roleAssignments = roleAssignmentService
            .queryRoleAssignments(QueryRequest
                .builder()
                .roleType(List.of(RoleType.ORGANISATION))
                .roleName(List.of(RoleName.TRIBUNAL_CASEWORKER, RoleName.SENIOR_TRIBUNAL_CASEWORKER))
                .grantType(List.of(GrantType.STANDARD))
                .classification(getClassification(securityClassification))
                .attributes(Map.of(
                    Attributes.JURISDICTION, List.of("IA"),
                    Attributes.PRIMARY_LOCATION, List.of(location)
                ))
                .validAt(LocalDateTime.now())
                .build());

        return roleAssignments.getRoleAssignmentResponse().stream()
            .map(role -> new Value(role.getActorId(), getCaseNameForActorId(role.getActorId())))
            .collect(Collectors.toList());
    }

    private String getCaseNameForActorId(String actorId) {
        CaseWorkerProfile caseWorkerProfile = refDataCaseWorkerApi.fetchUsersById(
            userDetails.getAccessToken(),
            serviceAuthTokenGenerator.generate(),
            new UserIds(List.of(actorId))
        );
        return caseWorkerProfile.getFirstName() + " " + caseWorkerProfile.getLastName();
    }

    private List<Classification> getClassification(String securityClassification) {
        if (PUBLIC.name().equals(securityClassification)) {
            return List.of(PUBLIC, RESTRICTED, PRIVATE);
        }
        if (RESTRICTED.name().equals(securityClassification)) {
            return List.of(RESTRICTED, PRIVATE);
        }
        return List.of(PRIVATE);
    }

}
