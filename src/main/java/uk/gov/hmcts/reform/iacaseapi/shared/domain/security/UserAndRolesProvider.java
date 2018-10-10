package uk.gov.hmcts.reform.iacaseapi.shared.domain.security;

import java.util.List;

public interface UserAndRolesProvider extends UserProvider {

    List<String> getUserRoles();
}
