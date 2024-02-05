package uk.gov.hmcts.reform.iacaseapi.infrastructure;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RetryCounterTest {

    private RetryCounter retryCounter;

    @BeforeEach
    void setUp() {
        retryCounter = new RetryCounter();
    }

    @Test
    void getRetryCount_calls_getRetryContext() {
        retryCounter.getContext();
    }
}