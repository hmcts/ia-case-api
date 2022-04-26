package uk.gov.hmcts.reform.bailcaseapi.infrastructure.clients;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class BailCaseNotificationApiSenderTest {

    private static final String ENDPOINT = "http://endpoint";
    private static final String CCD_SUBMITTED_PATH = "/path";

    @Mock
    private BailCaseCallbackApiDelegator bailCaseCallbackApiDelegator;
    @Mock
    private Callback<BailCase> callback;

    private BailCaseNotificationApiSender bailCaseNotificationApiSender;

    @BeforeEach
    public void setUp() {

        bailCaseNotificationApiSender =
            new BailCaseNotificationApiSender(
                bailCaseCallbackApiDelegator,
                ENDPOINT,
                CCD_SUBMITTED_PATH
            );
    }

    @Test
    void should_delegate_callback_to_downstream_api() {

        final BailCase notifiedBailCase = mock(BailCase.class);

        when(bailCaseCallbackApiDelegator.delegate(callback, ENDPOINT + CCD_SUBMITTED_PATH))
            .thenReturn(notifiedBailCase);

        final BailCase bailCaseResponse = bailCaseNotificationApiSender.send(callback);

        verify(bailCaseCallbackApiDelegator, times(1))
            .delegate(callback, ENDPOINT + CCD_SUBMITTED_PATH);

        assertEquals(notifiedBailCase, bailCaseResponse);
    }

    @Test
    void should_delegate_about_to_start_callback_to_downstream_api() {

        final BailCase notifiedBailCase = mock(BailCase.class);

        when(bailCaseCallbackApiDelegator.delegate(callback, ENDPOINT + CCD_SUBMITTED_PATH))
            .thenReturn(notifiedBailCase);

        final BailCase actualBailCase = bailCaseNotificationApiSender.send(callback);

        verify(bailCaseCallbackApiDelegator, times(1))
            .delegate(callback, ENDPOINT + CCD_SUBMITTED_PATH);

        assertEquals(notifiedBailCase, actualBailCase);
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> bailCaseNotificationApiSender.send(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
