package uk.gov.hmcts.reform.iacaseapi.domain;

import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserRole;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserRoleLabel;

public interface UserDetailsProvider {

    UserDetails getUserDetails();

    UserRole getLoggedInUserRole();

    UserRoleLabel getLoggedInUserRoleLabel();

}
