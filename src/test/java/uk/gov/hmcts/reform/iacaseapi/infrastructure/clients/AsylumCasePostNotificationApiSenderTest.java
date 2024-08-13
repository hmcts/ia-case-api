package uk.gov.hmcts.reform.iacaseapi.infrastructure.clients;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.service.Scheduler;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.TimedEvent;

import java.time.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class AsylumCasePostNotificationApiSenderTest {

    private static final String ENDPOINT = "http://endpoint";
    private static final String CCD_SUBMITTED_PATH = "/path";

    @Mock
    private AsylumCaseCallbackApiDelegator asylumCaseCallbackApiDelegator;
    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private DateProvider dateProvider;
    @Mock
    private Scheduler scheduler;

    private AsylumCasePostNotificationApiSender asylumCasePostNotificationApiSender;

    @Test
    void should_delegate_callback_to_downstream_api() {
        asylumCasePostNotificationApiSender =
            new AsylumCasePostNotificationApiSender(
                asylumCaseCallbackApiDelegator,
                ENDPOINT,
                CCD_SUBMITTED_PATH,
                false,
                dateProvider,
                scheduler
            );
        final PostSubmitCallbackResponse notifiedAsylumCase = mock(PostSubmitCallbackResponse.class);

        when(asylumCaseCallbackApiDelegator.delegatePostSubmit(callback, ENDPOINT + CCD_SUBMITTED_PATH))
            .thenReturn(notifiedAsylumCase);

        final PostSubmitCallbackResponse postSubmitCallbackResponse = asylumCasePostNotificationApiSender.send(callback);
        verify(scheduler, never()).schedule(any(TimedEvent.class));
        verify(asylumCaseCallbackApiDelegator, times(1))
            .delegatePostSubmit(callback, ENDPOINT + CCD_SUBMITTED_PATH);

        assertEquals(notifiedAsylumCase, postSubmitCallbackResponse);
    }

    @Test
    void should_delegate_about_to_start_callback_to_downstream_api() {
        asylumCasePostNotificationApiSender =
            new AsylumCasePostNotificationApiSender(
                asylumCaseCallbackApiDelegator,
                ENDPOINT,
                CCD_SUBMITTED_PATH,
                false,
                dateProvider,
                scheduler
            );
        final PostSubmitCallbackResponse notifiedAsylumCase = mock(PostSubmitCallbackResponse.class);

        when(asylumCaseCallbackApiDelegator.delegatePostSubmit(callback, ENDPOINT + CCD_SUBMITTED_PATH))
            .thenReturn(notifiedAsylumCase);

        final PostSubmitCallbackResponse actualAsylumCase = asylumCasePostNotificationApiSender.send(callback);
        verify(scheduler, never()).schedule(any(TimedEvent.class));
        verify(asylumCaseCallbackApiDelegator, times(1))
            .delegatePostSubmit(callback, ENDPOINT + CCD_SUBMITTED_PATH);

        assertEquals(notifiedAsylumCase, actualAsylumCase);
    }

    @Test
    void should_not_allow_null_arguments() {
        asylumCasePostNotificationApiSender =
            new AsylumCasePostNotificationApiSender(
                asylumCaseCallbackApiDelegator,
                ENDPOINT,
                CCD_SUBMITTED_PATH,
                false,
                dateProvider,
                scheduler
            );
        assertThatThrownBy(() -> asylumCasePostNotificationApiSender.send(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void schedules_save_notification_to_data_event_if_tes_enabled() {
        asylumCasePostNotificationApiSender =
            new AsylumCasePostNotificationApiSender(
                asylumCaseCallbackApiDelegator,
                ENDPOINT,
                CCD_SUBMITTED_PATH,
                true,
                dateProvider,
                scheduler
            );
        final PostSubmitCallbackResponse notifiedAsylumCase = mock(PostSubmitCallbackResponse.class);

        when(asylumCaseCallbackApiDelegator.delegatePostSubmit(callback, ENDPOINT + CCD_SUBMITTED_PATH))
            .thenReturn(notifiedAsylumCase);
        LocalDateTime localDateTime =
            LocalDateTime.of(2004, Month.FEBRUARY, 5, 10, 57, 33);
        when(dateProvider.nowWithTime()).thenReturn(localDateTime);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getId()).thenReturn(0L);
        final PostSubmitCallbackResponse actualAsylumCase = asylumCasePostNotificationApiSender.send(callback);
        ZonedDateTime expectedTime = ZonedDateTime.of(localDateTime, ZoneId.systemDefault())
            .plusSeconds(15);
        TimedEvent expectedTimedEvent = new TimedEvent(
            "",
            Event.SAVE_NOTIFICATIONS_TO_DATA,
            expectedTime,
            "IA",
            "Asylum",
            0L
        );
        verify(scheduler).schedule(refEq(expectedTimedEvent));
        verify(asylumCaseCallbackApiDelegator, times(1))
            .delegatePostSubmit(callback, ENDPOINT + CCD_SUBMITTED_PATH);

        assertEquals(notifiedAsylumCase, actualAsylumCase);
    }
}
