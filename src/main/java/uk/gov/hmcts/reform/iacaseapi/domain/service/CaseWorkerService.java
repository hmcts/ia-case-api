package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.Classification.PRIVATE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.Classification.PUBLIC;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.Classification.RESTRICTED;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.Assignment;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.Attributes;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.Classification;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.GrantType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.Jurisdiction;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.QueryRequest;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.RoleName;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.RoleType;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.refdata.CaseWorkerProfile;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.refdata.UserIds;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.refdata.RefDataCaseWorkerApi;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.roleassignment.RoleAssignmentService;

@Component
public class CaseWorkerService {

    private final RoleAssignmentService roleAssignmentService;
    private final RefDataCaseWorkerApi refDataCaseWorkerApi;
    private final UserDetails userDetails;
    private final AuthTokenGenerator serviceAuthTokenGenerator;

    public CaseWorkerService(RoleAssignmentService roleAssignmentService,
                             RefDataCaseWorkerApi refDataCaseWorkerApi,
                             UserDetails userDetails,
                             AuthTokenGenerator serviceAuthTokenGenerator) {
        this.roleAssignmentService = roleAssignmentService;
        this.refDataCaseWorkerApi = refDataCaseWorkerApi;
        this.userDetails = userDetails;
        this.serviceAuthTokenGenerator = serviceAuthTokenGenerator;
    }

    public List<Assignment> getRoleAssignmentsPerLocationAndClassification(
        String location,
        String securityClassification
    ) {
        return roleAssignmentService
            .queryRoleAssignments(QueryRequest.builder()
                .roleType(List.of(RoleType.ORGANISATION))
                .roleName(List.of(RoleName.TRIBUNAL_CASEWORKER, RoleName.SENIOR_TRIBUNAL_CASEWORKER))
                .grantType(List.of(GrantType.STANDARD))
                .classification(getClassification(securityClassification))
                .attributes(Map.of(
                    Attributes.JURISDICTION, List.of(Jurisdiction.IA.name()),
                    Attributes.PRIMARY_LOCATION, List.of(location)
                ))
                .validAt(LocalDateTime.now())
                .build()
            ).getRoleAssignmentResponse();
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

    public String getCaseWorkerNameForActorId(String actorId) {
        CaseWorkerProfile caseWorkerProfile = refDataCaseWorkerApi.fetchUsersById(
            userDetails.getAccessToken(),
            serviceAuthTokenGenerator.generate(),
            new UserIds(List.of(actorId))
        );
        return caseWorkerProfile.getFirstName() + " " + caseWorkerProfile.getLastName();
    }

}
