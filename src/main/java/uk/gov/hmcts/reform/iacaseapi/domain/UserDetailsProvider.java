package uk.gov.hmcts.reform.iacaseapi.domain;

import java.util.Arrays;
import java.util.stream.Stream;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserRole;

public interface UserDetailsProvider {

    UserDetails getUserDetails();

    default UserRole getLoggedInUserRole() {

        Stream<UserRole> allowedRoles = Arrays.stream(UserRole.values());

        return allowedRoles
            .filter(r -> getUserDetails().getRoles().contains(r.toString()))
            .findAny()
            .orElseThrow(() -> new IllegalStateException("No valid user role is present."));
    }
}
