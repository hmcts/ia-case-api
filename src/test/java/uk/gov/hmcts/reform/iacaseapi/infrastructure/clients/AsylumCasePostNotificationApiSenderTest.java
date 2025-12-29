package uk.gov.hmcts.reform.iacaseapi.infrastructure.clients;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class AsylumCasePostNotificationApiSenderTest {

    private static final String ENDPOINT = "http://endpoint";
    private static final String CCD_SUBMITTED_PATH = "/path";

    @Mock
    private CallbackApiDelegator callbackApiDelegator;
    @Mock
    private Callback<AsylumCase> callback;

    private AsylumCasePostNotificationApiSender asylumCasePostNotificationApiSender;

    @BeforeEach
    public void setUp() {

        asylumCasePostNotificationApiSender =
            new AsylumCasePostNotificationApiSender(
                callbackApiDelegator,
                ENDPOINT,
                CCD_SUBMITTED_PATH
            );
    }

    @Test
    void should_delegate_callback_to_downstream_api() {

        final PostSubmitCallbackResponse notifiedAsylumCase = mock(PostSubmitCallbackResponse.class);

        when(callbackApiDelegator.delegatePostSubmit(callback, ENDPOINT + CCD_SUBMITTED_PATH))
            .thenReturn(notifiedAsylumCase);

        final PostSubmitCallbackResponse postSubmitCallbackResponse = asylumCasePostNotificationApiSender.send(callback);

        verify(callbackApiDelegator, times(1))
            .delegatePostSubmit(callback, ENDPOINT + CCD_SUBMITTED_PATH);

        assertEquals(notifiedAsylumCase, postSubmitCallbackResponse);
    }

    @Test
    void should_delegate_about_to_start_callback_to_downstream_api() {

        final PostSubmitCallbackResponse notifiedAsylumCase = mock(PostSubmitCallbackResponse.class);

        when(callbackApiDelegator.delegatePostSubmit(callback, ENDPOINT + CCD_SUBMITTED_PATH))
            .thenReturn(notifiedAsylumCase);

        final PostSubmitCallbackResponse actualAsylumCase = asylumCasePostNotificationApiSender.send(callback);

        verify(callbackApiDelegator, times(1))
            .delegatePostSubmit(callback, ENDPOINT + CCD_SUBMITTED_PATH);

        assertEquals(notifiedAsylumCase, actualAsylumCase);
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> asylumCasePostNotificationApiSender.send(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
