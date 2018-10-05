package uk.gov.hmcts.reform.iacaseapi.domain.security;

import java.util.Optional;

public interface AccessTokenProvider {

    String getAccessToken();

    Optional<String> tryGetAccessToken();
}
