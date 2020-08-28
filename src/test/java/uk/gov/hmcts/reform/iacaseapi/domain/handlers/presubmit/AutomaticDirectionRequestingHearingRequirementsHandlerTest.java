package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.AUTOMATIC_DIRECTION_REQUESTING_HEARING_REQUIREMENTS;

import java.time.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.Scheduler;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.AsylumCaseServiceResponseException;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.TimedEvent;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class AutomaticDirectionRequestingHearingRequirementsHandlerTest {


    @Mock private
    DateProvider dateProvider;
    @Mock private
    Scheduler scheduler;
    @Mock private
    FeatureToggler featureToggler;

    @Mock private
    Callback<AsylumCase> callback;
    @Mock private
    CaseDetails<AsylumCase> caseDetails;
    @Mock private
    AsylumCase asylumCase;

    @Captor ArgumentCaptor<TimedEvent> timedEventArgumentCaptor;

    boolean timedEventServiceEnabled = true;
    int reviewInDays = 5;
    LocalDate now = LocalDate.now();
    String id = "someId";
    long caseId = 12345;
    String jurisdiction = "IA";
    String caseType = "Asylum";

    AutomaticDirectionRequestingHearingRequirementsHandler automaticDirectionHandler;

    @BeforeEach
    void setUp() {

        automaticDirectionHandler =
            new AutomaticDirectionRequestingHearingRequirementsHandler(
                timedEventServiceEnabled,
                reviewInDays,
                dateProvider,
                scheduler,
                featureToggler
            );
    }

    @Test
    void should_schedule_automatic_direction_5_days_from_now() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.REQUEST_RESPONSE_REVIEW);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(featureToggler.getValue("timed-event-short-delay", false)).thenReturn(false);
        when(caseDetails.getId()).thenReturn(caseId);
        when(dateProvider.now()).thenReturn(now);

        TimedEvent timedEvent = new TimedEvent(
            id,
            Event.REQUEST_HEARING_REQUIREMENTS_FEATURE,
            ZonedDateTime.of(now.plusDays(reviewInDays + 1), LocalTime.MIDNIGHT, ZoneId.systemDefault()),
            jurisdiction,
            caseType,
            caseId
        );
        when(scheduler.schedule(any(TimedEvent.class))).thenReturn(timedEvent);


        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            automaticDirectionHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertThat(asylumCase).isEqualTo(callbackResponse.getData());
        verify(asylumCase, times(1)).write(AUTOMATIC_DIRECTION_REQUESTING_HEARING_REQUIREMENTS, id);
        verify(scheduler).schedule(timedEventArgumentCaptor.capture());

        TimedEvent result = timedEventArgumentCaptor.getValue();

        assertEquals(timedEvent.getCaseId(), result.getCaseId());
        assertEquals(timedEvent.getJurisdiction(), result.getJurisdiction());
        assertEquals(timedEvent.getCaseType(), result.getCaseType());
        assertEquals(timedEvent.getScheduledDateTime(), result.getScheduledDateTime());
        assertEquals(timedEvent.getEvent(), result.getEvent());
        assertEquals("", result.getId());
    }

    @Test
    void should_schedule_automatic_direction_in_10_minutes_for_test_user() {

        LocalDateTime nowWithTime = LocalDateTime.now();

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.REQUEST_RESPONSE_REVIEW);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(featureToggler.getValue("timed-event-short-delay", false)).thenReturn(false);
        when(caseDetails.getId()).thenReturn(caseId);
        when(dateProvider.now()).thenReturn(now);
        when(dateProvider.nowWithTime()).thenReturn(nowWithTime);

        when(featureToggler.getValue("timed-event-short-delay", false)).thenReturn(true);

        TimedEvent timedEvent = new TimedEvent(
            id,
            Event.REQUEST_HEARING_REQUIREMENTS_FEATURE,
            ZonedDateTime.of(nowWithTime, ZoneId.systemDefault()).plusMinutes(5),
            jurisdiction,
            caseType,
            caseId
        );
        when(scheduler.schedule(any(TimedEvent.class))).thenReturn(timedEvent);


        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            automaticDirectionHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertThat(asylumCase).isEqualTo(callbackResponse.getData());
        verify(asylumCase, times(1)).write(AUTOMATIC_DIRECTION_REQUESTING_HEARING_REQUIREMENTS, id);
        verify(scheduler).schedule(timedEventArgumentCaptor.capture());

        TimedEvent result = timedEventArgumentCaptor.getValue();

        assertEquals(timedEvent.getCaseId(), result.getCaseId());
        assertEquals(timedEvent.getJurisdiction(), result.getJurisdiction());
        assertEquals(timedEvent.getCaseType(), result.getCaseType());
        assertEquals(timedEvent.getScheduledDateTime(), result.getScheduledDateTime());
        assertEquals(timedEvent.getEvent(), result.getEvent());
        assertEquals("", result.getId());
    }

    @Test
    void should_rethrow_exception_when_scheduler_failed() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.REQUEST_RESPONSE_REVIEW);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(featureToggler.getValue("timed-event-short-delay", false)).thenReturn(false);
        when(caseDetails.getId()).thenReturn(caseId);
        when(dateProvider.now()).thenReturn(now);

        when(scheduler.schedule(any(TimedEvent.class))).thenThrow(AsylumCaseServiceResponseException.class);

        assertThatThrownBy(() -> automaticDirectionHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .isExactlyInstanceOf(AsylumCaseServiceResponseException.class);
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> automaticDirectionHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        assertThatThrownBy(() -> automaticDirectionHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = automaticDirectionHandler.canHandle(callbackStage, callback);

                if (callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT && event.equals(Event.REQUEST_RESPONSE_REVIEW)) {

                    assertThat(canHandle).isEqualTo(true);
                } else {
                    assertThat(canHandle).isEqualTo(false);
                }
            }

            reset(callback);
        }
    }

}