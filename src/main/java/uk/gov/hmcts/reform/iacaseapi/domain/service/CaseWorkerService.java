package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;
import static org.apache.commons.lang3.StringUtils.trim;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.Classification.PRIVATE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.Classification.PUBLIC;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.Classification.RESTRICTED;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.Assignment;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.Attributes;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.Classification;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.GrantType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.Jurisdiction;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.QueryRequest;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.RoleName;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.RoleType;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.allocatecase.CaseWorkerName;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.refdata.UserIds;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.refdata.RefDataCaseWorkerApi;

@Component
@Slf4j
public class CaseWorkerService {

    private final RoleAssignmentService roleAssignmentService;
    private final RefDataCaseWorkerApi refDataCaseWorkerApi;
    private final IdamService idamService;
    private final AuthTokenGenerator serviceAuthTokenGenerator;

    public CaseWorkerService(
        RoleAssignmentService roleAssignmentService,
        RefDataCaseWorkerApi refDataCaseWorkerApi,
        IdamService idamService,
        AuthTokenGenerator serviceAuthTokenGenerator
    ) {
        this.roleAssignmentService = roleAssignmentService;
        this.refDataCaseWorkerApi = refDataCaseWorkerApi;
        this.idamService = idamService;
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

    public List<CaseWorkerName> getCaseWorkerNameForActorIds(List<String> actorIds) {
        return refDataCaseWorkerApi
            .fetchUsersById(
                idamService.getServiceUserToken(),
                serviceAuthTokenGenerator.generate(),
                new UserIds(actorIds)
            )
            .stream()
            .map(caseWorkerProfile -> {
                String caseWorkerNameFormatted = trim(String.format("%s %s",
                    defaultIfEmpty(caseWorkerProfile.getFirstName(), EMPTY),
                    defaultIfEmpty(caseWorkerProfile.getLastName(), EMPTY)));

                return new CaseWorkerName(caseWorkerProfile.getId(), caseWorkerNameFormatted);
            })
            .collect(Collectors.toList());
    }
}
