package uk.gov.hmcts.reform.iacaseapi.infrastructure.utils;

import java.util.Optional;

import feign.FeignException;
import feign.RetryableException;
import feign.Retryer;

import static java.util.concurrent.TimeUnit.SECONDS;

public class FeignRetryer extends Retryer.Default {

    public FeignRetryer() {
        super(500, SECONDS.toMillis(1), 10);
    }

    @Override
    public void continueOrPropagate(RetryableException e) {
        String errorMessage = Optional.ofNullable(e.getMessage()).orElse("");
        if (errorMessage.contains("failed to respond")) {
            super.continueOrPropagate(e);
        } else {
            throw new RetryableException(e.status(), "(not retryable) " + e.getMessage(), e.method(), e, e.retryAfter(), e.request());
        }
    }

    @Override
    @SuppressWarnings({"java:S2975", "java:S1182"}) // we are intentionally deviating from normal clone methods here
    public Retryer clone() {
        return new FeignRetryer();
    }
}
