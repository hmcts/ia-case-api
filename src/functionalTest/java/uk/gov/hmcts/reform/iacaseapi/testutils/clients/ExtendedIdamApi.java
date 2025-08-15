package uk.gov.hmcts.reform.iacaseapi.testutils.clients;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.IdamApi;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.idam.UserInfo;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.config.FeignConfiguration;

@FeignClient(
    primary = true,
    name = "extended-idam-api",
    url = "${idam.baseUrl}",
    configuration = FeignConfiguration.class
)
public interface ExtendedIdamApi extends IdamApi {

    @GetMapping(value = "/o/userinfo", produces = "application/json")
    UserInfo userInfo(@RequestHeader(AUTHORIZATION) String userToken);
}
