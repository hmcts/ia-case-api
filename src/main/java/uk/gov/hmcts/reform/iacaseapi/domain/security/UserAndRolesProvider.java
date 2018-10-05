package uk.gov.hmcts.reform.iacaseapi.domain.security;

import java.util.List;
import java.util.Optional;

public interface UserAndRolesProvider {

    String getUserId();

    Optional<String> tryGetUserId();

    List<String> getUserRoles();
}
