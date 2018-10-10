package uk.gov.hmcts.reform.iacaseapi.shared.domain.security;

import java.util.Map;

public interface AccessTokenDecoder {

    Map<String, String> decode(
        String accessToken
    );
}
