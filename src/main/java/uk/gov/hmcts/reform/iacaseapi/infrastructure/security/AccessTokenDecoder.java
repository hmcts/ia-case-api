package uk.gov.hmcts.reform.iacaseapi.infrastructure.security;

import java.util.Map;

public interface AccessTokenDecoder {

    Map<String, String> decode(
        String accessToken
    );
}
