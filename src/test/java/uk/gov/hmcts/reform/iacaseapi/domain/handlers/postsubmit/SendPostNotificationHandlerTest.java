package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.*;
import uk.gov.hmcts.reform.iacaseapi.domain.service.PostNotificationSender;


@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class SendPostNotificationHandlerTest {

    @Mock
    private PostNotificationSender<AsylumCase> postNotificationSender;
    @Mock
    private Callback<AsylumCase> callback;

    private SendPostNotificationHandler sendPostNotificationHandler;

    @BeforeEach
    public void setUp() {

        sendPostNotificationHandler =
            new SendPostNotificationHandler(postNotificationSender);
    }

    @Test
    void should_notify_case_officer_that_case_is_listed() {

        when(callback.getEvent()).thenReturn(Event.APPLY_NOC_DECISION);

        PostSubmitCallbackResponse expectedUpdatedCase = mock(PostSubmitCallbackResponse.class);

        when(postNotificationSender.send(callback)).thenReturn(expectedUpdatedCase);

        PostSubmitCallbackResponse callbackResponse =
            sendPostNotificationHandler.handle(callback);

        assertNotNull(callbackResponse);
        assertEquals(expectedUpdatedCase, callbackResponse);

        verify(postNotificationSender, times(1)).send(callback);

        reset(callback);
        reset(postNotificationSender);
    }


    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> sendPostNotificationHandler.handle(callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        assertThatThrownBy(() -> sendPostNotificationHandler.handle(callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {
            when(callback.getEvent()).thenReturn(event);
            boolean canHandle = sendPostNotificationHandler.canHandle(callback);

            if (Arrays.asList(
                Event.APPLY_NOC_DECISION,
                Event.REMOVE_REPRESENTATION
            ).contains(event)) {
                assertTrue(canHandle);
            } else {
                assertFalse(canHandle);
            }

            reset(callback);
        }
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> sendPostNotificationHandler.canHandle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
