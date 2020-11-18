package uk.gov.hmcts.reform.iacaseapi.infrastructure.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.config.FeignConfiguration;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.rd.Organisation;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.rd.OrganisationUser;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.rd.OrganisationUsers;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.rd.Status;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.reform.iacaseapi.infrastructure.config.ServiceTokenGeneratorConfiguration.SERVICE_AUTHORIZATION;

@FeignClient(
    name = "rd-professional-api",
    url = "${prof.ref.data.url}",
    configuration = FeignConfiguration.class
)
public interface OrganisationApi {
    @GetMapping("/refdata/external/v1/organisations/users")
    OrganisationUsers findUsersInOrganisation(
        @RequestHeader(AUTHORIZATION) String authorisation,
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorization,
        @RequestParam(value = "status") Status status,
        @RequestParam(value = "returnRoles") Boolean returnRoles
    );

    @GetMapping("/refdata/external/v1/organisations/users/accountId")
    OrganisationUser findUserByEmail(
        @RequestHeader(AUTHORIZATION) String authorisation,
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorization,
        @RequestParam(value = "email") final String email
    );

    @GetMapping("/refdata/external/v1/organisations")
    Organisation findUserOrganisation(
        @RequestHeader(AUTHORIZATION) String authorisation,
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorization
    );
}
