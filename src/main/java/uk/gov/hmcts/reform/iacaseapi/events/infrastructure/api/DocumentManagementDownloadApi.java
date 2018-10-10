package uk.gov.hmcts.reform.iacaseapi.events.infrastructure.api;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(
    name = "document-management-download-api",
    url = "${documentManagementApi.baseUrl}"
)
public interface DocumentManagementDownloadApi {

    @RequestMapping(
        method = RequestMethod.GET,
        value = "{binaryUri}"
    )
    ResponseEntity<Resource> downloadResource(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
        @RequestHeader("ServiceAuthorization") String serviceAuthorization,
        @RequestHeader("user-id") String userId,
        @RequestHeader("user-roles") String userRoles,
        @PathVariable("binaryUri") String binaryUri
    );
}
