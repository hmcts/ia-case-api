package uk.gov.hmcts.reform.iacaseapi.infrastructure.security.idam;

import feign.FeignException;
import java.util.Arrays;
import java.util.stream.Stream;
import uk.gov.hmcts.reform.iacaseapi.domain.UserDetailsProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserRole;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserRoleLabel;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.IdamApi;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.idam.UserInfo;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.security.AccessTokenProvider;

public class IdamUserDetailsProvider implements UserDetailsProvider {

    private final AccessTokenProvider accessTokenProvider;
    private final IdamApi idamApi;

    public IdamUserDetailsProvider(
        AccessTokenProvider accessTokenProvider,
        IdamApi idamApi
    ) {

        this.accessTokenProvider = accessTokenProvider;
        this.idamApi = idamApi;
    }

    public IdamUserDetails getUserDetails() {

        String accessToken = accessTokenProvider.getAccessToken();

        UserInfo response;

        try {

            response = idamApi.userInfo(accessToken);

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

        if (response.getEmail() == null) {
            throw new IllegalStateException("IDAM user details missing 'sub' field");
        }

        if (response.getGivenName() == null) {
            throw new IllegalStateException("IDAM user details missing 'given_name' field");
        }

        if (response.getFamilyName() == null) {
            throw new IllegalStateException("IDAM user details missing 'family_name' field");
        }

        return new IdamUserDetails(
            accessToken,
            response.getUid(),
            response.getRoles(),
            response.getEmail(),
            response.getGivenName(),
            response.getFamilyName()
        );
    }

    public UserRole getLoggedInUserRole() {

        Stream<UserRole> allowedRoles = Arrays.stream(UserRole.values());

        return allowedRoles
            .filter(r -> getUserDetails().getRoles().contains(r.toString()))
            .findAny()
            .orElseThrow(() -> new IllegalStateException("No valid user role is present."));
    }

    public UserRoleLabel getLoggedInUserRoleLabel() {

        switch (getLoggedInUserRole()) {
            case HOME_OFFICE_APC:
            case HOME_OFFICE_POU:
            case HOME_OFFICE_LART:
            case HOME_OFFICE_GENERIC:
                return UserRoleLabel.HOME_OFFICE_GENERIC;
            case LEGAL_REPRESENTATIVE:
                return UserRoleLabel.LEGAL_REPRESENTATIVE;
            case CASE_OFFICER:
                return UserRoleLabel.TRIBUNAL_CASEWORKER;
            case ADMIN_OFFICER:
                return UserRoleLabel.ADMIN_OFFICER;
            case JUDICIARY:
            case JUDGE:
                return UserRoleLabel.JUDGE;
            case SYSTEM:
                return UserRoleLabel.SYSTEM;

            default:
                throw new IllegalStateException("Unauthorized role to make an application");
        }
    }
}
