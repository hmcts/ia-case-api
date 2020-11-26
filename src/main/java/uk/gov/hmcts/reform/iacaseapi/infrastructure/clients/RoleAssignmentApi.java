package uk.gov.hmcts.reform.iacaseapi.infrastructure.clients;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.reform.iacaseapi.infrastructure.config.ServiceTokenGeneratorConfiguration.SERVICE_AUTHORIZATION;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.RoleAssignment;

@FeignClient(
        name = "role-assignment-service-api",
        url = "${role-assignment-service.url}"
)
public interface RoleAssignmentApi {

    @PostMapping(value = "/am/role-assignments", consumes = "application/json")
    void assignRole(
            @RequestHeader(AUTHORIZATION) String userToken,
            @RequestHeader(SERVICE_AUTHORIZATION) String s2sToken,
            @RequestBody RoleAssignment body
    );

}
