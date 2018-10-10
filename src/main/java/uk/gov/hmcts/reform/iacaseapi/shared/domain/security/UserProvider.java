package uk.gov.hmcts.reform.iacaseapi.shared.domain.security;

import java.util.Optional;

public interface UserProvider {

    String getUserId();

    Optional<String> tryGetUserId();
}
