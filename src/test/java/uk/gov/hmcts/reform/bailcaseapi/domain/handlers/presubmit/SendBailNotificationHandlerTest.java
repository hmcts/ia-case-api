package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.service.NotificationSender;


@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class SendBailNotificationHandlerTest {

    @Mock
    private NotificationSender<BailCase> notificationSender;
    @Mock
    private Callback<BailCase> callback;

    private uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.SendBailNotificationHandler sendBailNotificationHandler;

    @BeforeEach
    public void setUp() {
        sendBailNotificationHandler = new uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.SendBailNotificationHandler(notificationSender);
    }

    @Test
    void should_send_notification_and_update_the_case() {

        Arrays.asList(
            Event.SUBMIT_APPLICATION,
            Event.UPLOAD_BAIL_SUMMARY,
            Event.MAKE_NEW_APPLICATION,
            Event.FORCE_CASE_TO_HEARING
        ).forEach(event -> {

            BailCase expectedUpdatedCase = mock(BailCase.class);

            when(callback.getEvent()).thenReturn(event);
            when(notificationSender.send(callback)).thenReturn(expectedUpdatedCase);

            PreSubmitCallbackResponse<BailCase> callbackResponse =
                sendBailNotificationHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

            assertNotNull(callbackResponse);
            assertEquals(expectedUpdatedCase, callbackResponse.getData());

            verify(notificationSender, times(1)).send(callback);

            reset(callback);
            reset(notificationSender);
        });
    }

    @Test
    void should_be_handled_at_latest_point() {
        assertEquals(DispatchPriority.LATEST, sendBailNotificationHandler.getDispatchPriority());
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> sendBailNotificationHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.NOC_REQUEST);
        assertThatThrownBy(() -> sendBailNotificationHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = sendBailNotificationHandler.canHandle(callbackStage, callback);

                if (callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    &&
                    Arrays.asList(
                        Event.START_APPLICATION,
                        Event.EDIT_BAIL_APPLICATION,
                        Event.SUBMIT_APPLICATION,
                        Event.UPLOAD_BAIL_SUMMARY,
                        Event.UPLOAD_SIGNED_DECISION_NOTICE,
                        Event.END_APPLICATION,
                        Event.UPLOAD_DOCUMENTS,
                        Event.SEND_BAIL_DIRECTION,
                        Event.EDIT_BAIL_DOCUMENTS,
                        Event.CHANGE_BAIL_DIRECTION_DUE_DATE,
                        Event.MAKE_NEW_APPLICATION,
                        Event.EDIT_BAIL_APPLICATION_AFTER_SUBMIT,
                        Event.CREATE_BAIL_CASE_LINK,
                        Event.MAINTAIN_BAIL_CASE_LINKS,
                        Event.CASE_LISTING,
                        Event.RECORD_THE_DECISION,
                        Event.FORCE_CASE_TO_HEARING,
                        Event.CHANGE_TRIBUNAL_CENTRE
                    ).contains(event)) {

                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }

            reset(callback);
        }
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> sendBailNotificationHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> sendBailNotificationHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> sendBailNotificationHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> sendBailNotificationHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
