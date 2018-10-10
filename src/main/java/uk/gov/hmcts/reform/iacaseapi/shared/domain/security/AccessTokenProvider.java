package uk.gov.hmcts.reform.iacaseapi.shared.domain.security;

import java.util.Optional;

public interface AccessTokenProvider {

    String getAccessToken();

    Optional<String> tryGetAccessToken();
}
