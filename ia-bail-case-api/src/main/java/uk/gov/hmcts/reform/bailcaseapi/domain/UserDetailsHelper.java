package uk.gov.hmcts.reform.bailcaseapi.domain;

import uk.gov.hmcts.reform.bailcaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.UserRole;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.UserRoleLabel;

public interface UserDetailsHelper {

    UserRole getLoggedInUserRole(UserDetails userDetails);

    UserRoleLabel getLoggedInUserRoleLabel(UserDetails userDetails);

    String getIdamUserName(UserDetails userDetails);
}
