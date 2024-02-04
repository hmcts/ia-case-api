package uk.gov.hmcts.reform.iacaseapi.infrastructure.clients;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClientResponseException;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplyNocRetryableExecutorTest {
    private ApplyNocRetryableExecutor applyNocRetryableExecutor;

    @Mock
    private Callback<AsylumCase> callback;

    @Mock
    private CaseDetails<AsylumCase> caseDetails;

    private static final long caseId = 456L;

    @BeforeEach
    void setUp() {
        applyNocRetryableExecutor = new ApplyNocRetryableExecutor();
    }

    @Test
    void retryCall() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getId()).thenReturn(caseId);
        assertThatThrownBy(
                () -> applyNocRetryableExecutor.retryApplyNoc(callback)
        ).isInstanceOf(RestClientResponseException.class);
    }
}
