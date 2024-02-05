package uk.gov.hmcts.reform.iacaseapi.infrastructure.clients;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.iacaseapi.component.testutils.SpringBootIntegrationTest;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.UserDetailsProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.DbAppealReferenceNumberGenerator;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.RetryCounter;

public class ApplyNocRetryableExecutorIntegrationTest extends SpringBootIntegrationTest {
    @MockBean
    private AuthTokenGenerator serviceAuthTokenGenerator;

    @MockBean
    private UserDetailsProvider userDetailsProvider;

    @MockBean
    private RetryCounter retryCounter;

    @Mock
    private Callback<AsylumCase> callback;

    @Autowired
    private ApplyNocRetryableExecutor applyNocRetryableExecutor;

    @Test
    void should_retry() {
        applyNocRetryableExecutor.retryApplyNoc(callback);
    }
}
