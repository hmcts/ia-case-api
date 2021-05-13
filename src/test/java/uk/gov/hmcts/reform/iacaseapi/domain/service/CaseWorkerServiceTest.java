package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.Classification.PRIVATE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.Classification.PUBLIC;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.Classification.RESTRICTED;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import lombok.Builder;
import lombok.Value;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.Attributes;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.Classification;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.GrantType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.Jurisdiction;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.QueryRequest;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.RoleAssignmentResource;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.RoleName;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.RoleType;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.allocatecase.CaseWorkerName;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.refdata.CaseWorkerProfile;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.refdata.UserIds;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.refdata.RefDataCaseWorkerApi;

@ExtendWith(MockitoExtension.class)
class CaseWorkerServiceTest {

    @Mock
    private RoleAssignmentService roleAssignmentService;
    @Mock
    private RefDataCaseWorkerApi refDataCaseWorkerApi;
    @Mock
    private AuthTokenGenerator authTokenGenerator;
    @Mock
    private IdamService idamService;
    @InjectMocks
    private CaseWorkerService caseWorkerService;

    @Captor
    private ArgumentCaptor<QueryRequest> captor;

    private static final String ACTOR_ID = "some actor id";

    @ParameterizedTest
    @MethodSource("scenarioProvider")
    void getRoleAssignmentsPerLocationAndClassification(Scenario scenario) {

        when(roleAssignmentService.queryRoleAssignments(any(QueryRequest.class)))
            .thenReturn(new RoleAssignmentResource(Collections.emptyList()));

        caseWorkerService.getRoleAssignmentsPerLocationAndClassification(
            "some location",
            scenario.classification.name()
        );

        verify(roleAssignmentService).queryRoleAssignments(captor.capture());

        QueryRequest actualQueryRequest = captor.getValue();
        QueryRequest expectedQueryRequest = QueryRequest.builder()
            .roleType(List.of(RoleType.ORGANISATION))
            .roleName(List.of(RoleName.TRIBUNAL_CASEWORKER, RoleName.SENIOR_TRIBUNAL_CASEWORKER))
            .grantType(List.of(GrantType.STANDARD))
            .classification(scenario.expectedClassification)
            .attributes(Map.of(
                Attributes.JURISDICTION, List.of(Jurisdiction.IA.name()),
                Attributes.PRIMARY_LOCATION, List.of("some location")
            ))
            .build();
        assertThat(actualQueryRequest)
            .usingRecursiveComparison().ignoringFields("validAt").isEqualTo(expectedQueryRequest);
    }

    private static Stream<Scenario> scenarioProvider() {
        Scenario publicScenario = Scenario.builder()
            .classification(PUBLIC)
            .expectedClassification(List.of(PUBLIC, RESTRICTED, PRIVATE))
            .build();

        Scenario restrictedScenario = Scenario.builder()
            .classification(RESTRICTED)
            .expectedClassification(List.of(RESTRICTED, PRIVATE))
            .build();

        Scenario privateScenario = Scenario.builder()
            .classification(PRIVATE)
            .expectedClassification(List.of(PRIVATE))
            .build();

        return Stream.of(publicScenario, restrictedScenario, privateScenario);
    }

    @Value
    @Builder
    private static class Scenario {
        Classification classification;
        List<Classification> expectedClassification;
    }

    @ParameterizedTest
    @MethodSource("getCaseWorkerNameForActorIdScenarioProvider")
    void getCaseWorkerNameForActorId(CaseWorkerNameScenario scenario) {

        String userBearerToken = "some user bearer token";
        when(idamService.getUserToken()).thenReturn(userBearerToken);

        String serviceBearerToken = "some service bearer token";
        when(authTokenGenerator.generate()).thenReturn(serviceBearerToken);

        List<String> someActorIds = newArrayList(ACTOR_ID);
        when(refDataCaseWorkerApi.fetchUsersById(
            userBearerToken,
            serviceBearerToken,
            new UserIds(someActorIds)
        )).thenReturn(Collections.singletonList(scenario.getCaseWorkerProfile()));

        List<CaseWorkerName> actualCaseWorkerName = caseWorkerService.getCaseWorkerNameForActorIds(someActorIds);

        assertThat(actualCaseWorkerName.get(0)).isEqualTo(scenario.getExpectedCaseWorkerName());
    }

    private static Stream<CaseWorkerNameScenario> getCaseWorkerNameForActorIdScenarioProvider() {

        CaseWorkerNameScenario caseWorkerProfileExistsInRefDataApiScenario = new CaseWorkerNameScenario(
            CaseWorkerProfile.builder().id(ACTOR_ID).firstName("some firstname").lastName("some lastname").build(),
            new CaseWorkerName(ACTOR_ID, "some firstname some lastname")
        );

        CaseWorkerNameScenario caseWorkerProfileDoesNotExistsInRefDataApiScenario = new CaseWorkerNameScenario(
            CaseWorkerProfile.builder().id(ACTOR_ID).build(),
            new CaseWorkerName(ACTOR_ID, "")
        );

        CaseWorkerNameScenario caseWorkerProfileWithNullFirstnameScenario = new CaseWorkerNameScenario(
            CaseWorkerProfile.builder().id(ACTOR_ID).firstName(null).lastName("some lastname").build(),
            new CaseWorkerName(ACTOR_ID, "some lastname")
        );

        CaseWorkerNameScenario caseWorkerProfileWithNullLastnameScenario = new CaseWorkerNameScenario(
            CaseWorkerProfile.builder().id(ACTOR_ID).firstName("some firstname").lastName(null).build(),
            new CaseWorkerName(ACTOR_ID, "some firstname")
        );

        return Stream.of(caseWorkerProfileExistsInRefDataApiScenario,
            caseWorkerProfileDoesNotExistsInRefDataApiScenario,
            caseWorkerProfileWithNullFirstnameScenario,
            caseWorkerProfileWithNullLastnameScenario);
    }

    @Value
    private static class CaseWorkerNameScenario {
        CaseWorkerProfile caseWorkerProfile;
        CaseWorkerName expectedCaseWorkerName;
    }
}
