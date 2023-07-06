package uk.gov.hmcts.reform.iacaseapi.utils;

import java.util.Date;

import feign.RetryableException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.utils.FeignRetryer;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class FeignRetryerTest {
    @Test
    void testForFailedToRespondErrors() {
        // Given
        FeignRetryer retryer = new FeignRetryer();
        RetryableException ex = mock(RetryableException.class);
        given(ex.retryAfter()).willReturn(new Date());
        given(ex.getMessage()).willReturn("failed to respond");

        // When
        retryer.continueOrPropagate(ex);

        // Then: it passed execution to the underlying class
        verify(ex, times(2)).retryAfter();
    }

    @Test
    void testForOtherErrors() {
        // Given
        FeignRetryer retryer = new FeignRetryer();
        RetryableException ex = mock(RetryableException.class);
        given(ex.getMessage()).willReturn("other error");

        // When, Then: it didn't pass execution to the underlying class and threw the exception instead
        assertThrows(RetryableException.class, () -> retryer.continueOrPropagate(ex));
        verify(ex, times(0)).retryAfter();

    }
}
