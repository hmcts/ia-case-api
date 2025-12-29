package uk.gov.hmcts.reform.iacaseapi.domain;

import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserRole;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserRoleLabel;

public interface UserDetailsHelper {

    UserRole getLoggedInUserRole(UserDetails userDetails);

    UserRoleLabel getLoggedInUserRoleLabel(UserDetails userDetails, boolean isBailCase);

}
