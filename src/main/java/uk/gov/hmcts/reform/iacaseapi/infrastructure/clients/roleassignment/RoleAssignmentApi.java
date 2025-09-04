package uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.roleassignment;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.iacaseapi.infrastructure.config.ServiceTokenGeneratorConfiguration.SERVICE_AUTHORIZATION;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.QueryRequest;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.RoleAssignment;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.RoleAssignmentResource;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.config.DisableHystrixFeignConfiguration;


@FeignClient(
    name = "role-assignment-service-api",
    url = "${role-assignment-service.url}",
    configuration = DisableHystrixFeignConfiguration.class
)
public interface RoleAssignmentApi {

    @PostMapping(value = "/am/role-assignments", consumes = "application/json")
    void assignRole(
            @RequestHeader(AUTHORIZATION) String userToken,
            @RequestHeader(SERVICE_AUTHORIZATION) String s2sToken,
            @RequestBody RoleAssignment body
    );

    @PostMapping(value = "/am/role-assignments/query", consumes = "application/json")
    RoleAssignmentResource queryRoleAssignments(
        @RequestHeader(AUTHORIZATION) String userToken,
        @RequestHeader(SERVICE_AUTHORIZATION) String s2sToken,
        @RequestBody QueryRequest queryRequest
    );

    @DeleteMapping(value = "/am/role-assignments/{assignmentId}", consumes = "application/json")
    void deleteRoleAssignment(
        @RequestHeader(AUTHORIZATION) String userToken,
        @RequestHeader(SERVICE_AUTHORIZATION) String s2sToken,
        @PathVariable("assignmentId") String assignmentId
    );

    String ACTOR_ID = "actorId";

    @GetMapping(
        value = "/am/role-assignments/actors/{actorId}",
        consumes = APPLICATION_JSON_VALUE,
        headers = CONTENT_TYPE + "=" + APPLICATION_JSON_VALUE
    )
    RoleAssignmentResource getRoleAssignments(
        @RequestHeader(AUTHORIZATION) String authorization,
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorization,
        @PathVariable(ACTOR_ID) String actorId);

}
