package uk.gov.hmcts.reform.iacaseapi.infrastructure.security.idam;

import feign.FeignException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.iacaseapi.domain.UserDetailsProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.service.IdamService;
import uk.gov.hmcts.reform.iacaseapi.domain.service.RoleAssignmentService;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.idam.UserInfo;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.security.AccessTokenProvider;

@Slf4j
public class IdamUserDetailsProvider implements UserDetailsProvider {

    private final AccessTokenProvider accessTokenProvider;
    private final RoleAssignmentService roleAssignmentService;
    private final IdamService idamService;

    public IdamUserDetailsProvider(
        AccessTokenProvider accessTokenProvider,
        RoleAssignmentService roleAssignmentService,
        IdamService idamService
    ) {

        this.accessTokenProvider = accessTokenProvider;
        this.roleAssignmentService = roleAssignmentService;
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
        List<String> amRoles = roleAssignmentService.getAmRolesFromUser(response.getUid(), accessToken);
        List<String> idamRoles = Collections.emptyList();
        if (response.getRoles() != null) {
            idamRoles = response.getRoles();
        }

        List<String> roles = Stream.concat(amRoles.stream(), idamRoles.stream()).toList();

        if (roles.isEmpty()) {
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
            roles,
            response.getEmail(),
            response.getGivenName(),
            response.getFamilyName()
        );
    }
}
