package uk.gov.hmcts.reform.iacaseapi.shared.infrastructure.security;

import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.security.AccessTokenProvider;

@Service
public class HttpRequestAccessTokenProvider implements AccessTokenProvider {

    private final String AUTHORIZATION = "Authorization";

    public String getAccessToken() {
        return tryGetAccessToken()
            .orElseThrow(() -> new IllegalStateException("Access token not present"));
    }

    public Optional<String> tryGetAccessToken() {

        if (RequestContextHolder.getRequestAttributes() == null) {
            throw new IllegalStateException("No current HTTP request");
        }

        return Optional.ofNullable(
            ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                .getRequest()
                .getHeader(AUTHORIZATION)
        );
    }
}
