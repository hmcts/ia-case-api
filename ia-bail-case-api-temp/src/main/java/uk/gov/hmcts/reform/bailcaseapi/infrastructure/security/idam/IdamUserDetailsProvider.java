package uk.gov.hmcts.reform.bailcaseapi.infrastructure.security.idam;

import feign.FeignException;
import uk.gov.hmcts.reform.bailcaseapi.domain.UserDetailsProvider;
import uk.gov.hmcts.reform.bailcaseapi.domain.service.IdamService;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.clients.IdamApi;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.clients.model.idam.UserInfo;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.security.AccessTokenProvider;

public class IdamUserDetailsProvider implements UserDetailsProvider {

    private final AccessTokenProvider accessTokenProvider;
    private final IdamApi idamApi;
    private final IdamService idamService;

    public IdamUserDetailsProvider(
        AccessTokenProvider accessTokenProvider,
        IdamApi idamApi,
        IdamService idamService
    ) {

        this.accessTokenProvider = accessTokenProvider;
        this.idamApi = idamApi;
        this.idamService = idamService;
    }

    public IdamUserDetails getUserDetails() {
        String accessToken = accessTokenProvider.getAccessToken();
        UserInfo response;

        try {
            response = idamService.getUserInfo(accessToken);
        } catch (FeignException ex) {
            throw new IdentityManagerResponseException(
                "Could not get user details with IDAM",
                ex
            );
        }

        if (response.getUid() == null) {
            throw new IllegalStateException("IDAM user details missing 'uid' field");
        }

        if (response.getRoles() == null) {
            throw new IllegalStateException("IDAM user details missing 'roles' field");
        }

        return new IdamUserDetails(
            accessToken, response.getUid(),
            response.getRoles(),
            response.getEmail(),
            response.getGivenName(),
            response.getFamilyName()
        );
    }
}
