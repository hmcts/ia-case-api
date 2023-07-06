package uk.gov.hmcts.reform.iacaseapi.infrastructure.utils;

import java.util.Optional;

import feign.RetryableException;
import feign.Retryer;
import lombok.extern.slf4j.Slf4j;

import static java.util.concurrent.TimeUnit.SECONDS;

@Slf4j
public class FeignRetryer extends Retryer.Default {

    public FeignRetryer() {
        super(500, SECONDS.toMillis(1), 10);
    }

    @Override
    public void continueOrPropagate(RetryableException e) {
        String errorMessage = Optional.ofNullable(e.getMessage()).orElse("");
        if (errorMessage.contains("failed to respond")) {
            log.warn("A call to an external API failed and will be tried again a few times.");
            super.continueOrPropagate(e);
        } else {
            throw e;
        }
    }

    @Override
    @SuppressWarnings({"java:S2975", "java:S1182"}) // we are intentionally deviating from normal clone methods here
    public Retryer clone() {
        return new FeignRetryer();
    }
}
