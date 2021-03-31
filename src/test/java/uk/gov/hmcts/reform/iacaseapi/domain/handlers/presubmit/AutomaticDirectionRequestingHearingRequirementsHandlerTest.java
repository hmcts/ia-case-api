package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
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


    @Mock
    private DateProvider dateProvider;
    @Mock
    private Scheduler scheduler;
    @Mock
    private FeatureToggler featureToggler;

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;

    @Captor
    private ArgumentCaptor<TimedEvent> timedEventArgumentCaptor;

    private boolean timedEventServiceEnabled = true;
    private int reviewInDays = 5;
    private LocalDate now = LocalDate.now();
    private String id = "someId";
    private long caseId = 12345;
    private String jurisdiction = "IA";
    private String caseType = "Asylum";

    private AutomaticDirectionRequestingHearingRequirementsHandler automaticDirectionHandler;

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

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {"REQUEST_RESPONSE_REVIEW", "ADD_APPEAL_RESPONSE"})
    void should_schedule_automatic_direction_5_days_from_now(Event event) {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(event);
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

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {"REQUEST_RESPONSE_REVIEW", "ADD_APPEAL_RESPONSE"})
    void should_schedule_automatic_direction_in_10_minutes_for_test_user(Event event) {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(event);
        when(featureToggler.getValue("timed-event-short-delay", false)).thenReturn(false);
        when(caseDetails.getId()).thenReturn(caseId);
        when(dateProvider.now()).thenReturn(now);

        LocalDateTime nowWithTime = LocalDateTime.now();

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

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {"REQUEST_RESPONSE_REVIEW", "ADD_APPEAL_RESPONSE"})
    void should_rethrow_exception_when_scheduler_failed(Event event) {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(event);
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

                if (callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && Arrays.asList(Event.REQUEST_RESPONSE_REVIEW, Event.ADD_APPEAL_RESPONSE).contains(event)) {

                    assertThat(canHandle).isEqualTo(true);
                } else {
                    assertThat(canHandle).isEqualTo(false);
                }
            }

            reset(callback);
        }
    }

}
