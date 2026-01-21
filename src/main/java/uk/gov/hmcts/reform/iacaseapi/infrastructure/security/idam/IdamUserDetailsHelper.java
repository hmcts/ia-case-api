package uk.gov.hmcts.reform.iacaseapi.infrastructure.security.idam;

import java.util.Arrays;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.UserDetailsHelper;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserRole;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserRoleLabel;

@Slf4j
@Component
public class IdamUserDetailsHelper implements UserDetailsHelper {

    @Override
    public UserRole getLoggedInUserRole(UserDetails userDetails) {

        Stream<UserRole> allowedRoles = Arrays.stream(UserRole.values())
            .filter(r -> r != UserRole.UNKNOWN);

        return allowedRoles
            .filter(r -> userDetails.getRoles().contains(r.toString()))
            .findAny()
            .orElseThrow(() -> new IllegalStateException("No valid user role is present."));
    }

    public UserRoleLabel getLoggedInUserRoleLabel(UserDetails userDetails) {
        return switch (getLoggedInUserRole(userDetails)) {
            case HOME_OFFICE_APC, HOME_OFFICE_POU, HOME_OFFICE_LART, HOME_OFFICE_GENERIC ->
                UserRoleLabel.HOME_OFFICE_GENERIC;
            case LEGAL_REPRESENTATIVE -> UserRoleLabel.LEGAL_REPRESENTATIVE;
            case CASE_OFFICER, TRIBUNAL_CASEWORKER, CHALLENGED_ACCESS_LEGAL_OPERATIONS, SENIOR_TRIBUNAL_CASEWORKER ->
                UserRoleLabel.TRIBUNAL_CASEWORKER;
            case ADMIN_OFFICER, HEARING_CENTRE_ADMIN, CTSC, CTSC_TEAM_LEADER, NATIONAL_BUSINESS_CENTRE,
                 CHALLENGED_ACCESS_CTSC, CHALLENGED_ACCESS_ADMIN -> UserRoleLabel.ADMIN_OFFICER;
            case IDAM_JUDGE, JUDICIARY, JUDGE, SENIOR_JUDGE, LEADERSHIP_JUDGE, FEE_PAID_JUDGE, LEAD_JUDGE,
                 HEARING_JUDGE, FTPA_JUDGE, HEARING_PANEL_JUDGE, CHALLENGED_ACCESS_JUDICIARY -> UserRoleLabel.JUDGE;
            case CITIZEN -> UserRoleLabel.CITIZEN;
            case SYSTEM -> UserRoleLabel.SYSTEM;
            default -> throw new IllegalStateException("Unauthorized role to make an application");
        };
    }

    @Override
    public UserRoleLabel getLoggedInUserRoleLabel(UserDetails userDetails, boolean isBailCase) {
        if (!isBailCase) {
            return getLoggedInUserRoleLabel(userDetails);
        }
        return switch (getLoggedInUserRole(userDetails)) {
            case HOME_OFFICE_BAIL, HOME_OFFICE_POU -> UserRoleLabel.HOME_OFFICE_BAIL;
            case LEGAL_REPRESENTATIVE -> UserRoleLabel.LEGAL_REPRESENTATIVE;
            case CASE_OFFICER -> UserRoleLabel.TRIBUNAL_CASEWORKER;
            case ADMIN_OFFICER -> UserRoleLabel.ADMIN_OFFICER;
            case JUDICIARY, JUDGE -> UserRoleLabel.JUDGE;
            case SYSTEM -> UserRoleLabel.SYSTEM;
            default -> throw new IllegalStateException("Unauthorized role to make an application");
        };
    }
}
