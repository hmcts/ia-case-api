package uk.gov.hmcts.reform.iacaseapi.infrastructure.clients;

import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;

@Slf4j
@Service
public class ApplyNocRetryableExecutor {
    @Retryable(maxAttempts = 4, backoff = @Backoff(60000))
    public void retryApplyNoc(final Callback<AsylumCase> callback) {
        log.info(
            "Executing Apply NoC for case {}",
            callback.getCaseDetails().getId()
        );

        throw new RestClientResponseException("", 0, "", null, null, null);
    }
}
