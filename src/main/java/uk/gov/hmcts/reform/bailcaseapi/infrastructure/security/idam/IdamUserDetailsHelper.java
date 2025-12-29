package uk.gov.hmcts.reform.bailcaseapi.infrastructure.security.idam;

import java.util.Arrays;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bailcaseapi.domain.UserDetailsHelper;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.UserRole;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.UserRoleLabel;

@Slf4j
@Component
public class IdamUserDetailsHelper implements UserDetailsHelper {

    @Override
    public UserRole getLoggedInUserRole(UserDetails userDetails) {
        Stream<UserRole> allowedRoles = Arrays.stream(UserRole.values());
        return allowedRoles
            .filter(r -> userDetails.getRoles().contains(r.toString()))
            .findAny()
            .orElseThrow(() -> new IllegalStateException("No valid user role is present."));
    }

    @Override
    public UserRoleLabel getLoggedInUserRoleLabel(UserDetails userDetails) {
        switch (getLoggedInUserRole(userDetails)) {
            case HOME_OFFICE_BAIL, HOME_OFFICE_POU:
                return UserRoleLabel.HOME_OFFICE_BAIL;
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

    @Override
    public String getIdamUserName(UserDetails userDetails) {
        return userDetails.getForename() + " " + userDetails.getSurname();
    }
}
