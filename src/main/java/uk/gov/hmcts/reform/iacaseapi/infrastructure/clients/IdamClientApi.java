package uk.gov.hmcts.reform.iacaseapi.infrastructure.clients;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.idam.User;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.config.DisableHystrixFeignConfiguration;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.config.FeignConfiguration;

@FeignClient(
    name = "idam-client-api",
    url = "${idam.apiUrl}",
    configuration = {FeignConfiguration.class, DisableHystrixFeignConfiguration.class}
)
public interface IdamClientApi {
    @GetMapping(value = "/api/v1/users", produces = "application/json", consumes = "application/json")
    ResponseEntity<List<User>> getUserV1(@RequestHeader(AUTHORIZATION) String userToken,
                                         @RequestParam("query") final String elasticSearchQuery);

    @GetMapping(value = "/api/v2/users/{userId}", produces = "application/json", consumes = "application/json")
    ResponseEntity<User> getUser(@RequestHeader(AUTHORIZATION) String userToken, @PathVariable String userId);

    @GetMapping(value = "/api/v2/users-by-email/{email}", produces = "application/json", consumes = "application/json")
    ResponseEntity<User> getUserFromEmail(@RequestHeader(AUTHORIZATION) String userToken, @PathVariable String email);
}