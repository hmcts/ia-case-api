package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealReviewOutcome;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.SourceOfAppeal;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.JourneyType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.Scheduler;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.AsylumCaseServiceResponseException;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.TimedEvent;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
@MockitoSettings(strictness = Strictness.LENIENT)
class AutomaticDirectionRequestingHearingRequirementsHandlerTest {


    private final boolean timedEventServiceEnabled = true;
    private final int reviewInDays = 5;
    private final LocalDate now = LocalDate.now();
    private final String id = "someId";
    private final long caseId = 12345;
    private final String jurisdiction = "IA";
    private final String caseType = "Asylum";
    private final boolean rehydratedAppeal = true;
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
    private AutomaticDirectionRequestingHearingRequirementsHandler automaticDirectionHandler;

    private static Stream<Arguments> provideParameterValues() {
        return Stream.of(
            Arguments.of(JourneyType.AIP, Event.REQUEST_RESPONSE_REVIEW),
            Arguments.of(JourneyType.AIP, Event.ADD_APPEAL_RESPONSE),
            Arguments.of(JourneyType.REP, Event.REQUEST_RESPONSE_REVIEW),
            Arguments.of(JourneyType.REP, Event.ADD_APPEAL_RESPONSE)
        );
    }

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
    @MethodSource("provideParameterValues")
    void should_schedule_automatic_direction_5_days_from_now(JourneyType journeyType, Event event) {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(event);
        when(featureToggler.getValue("timed-event-short-delay", false)).thenReturn(false);
        when(caseDetails.getId()).thenReturn(caseId);
        when(dateProvider.now()).thenReturn(now);
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class))
            .thenReturn(Optional.of(journeyType));

        when(asylumCase.read(APPEAL_REVIEW_OUTCOME, AppealReviewOutcome.class))
            .thenReturn(Optional.of(AppealReviewOutcome.DECISION_MAINTAINED));

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
    @MethodSource("provideParameterValues")
    void should_schedule_automatic_direction_in_10_minutes_for_test_user(JourneyType journeyType, Event event) {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(event);
        when(featureToggler.getValue("timed-event-short-delay", false)).thenReturn(false);
        when(caseDetails.getId()).thenReturn(caseId);
        when(dateProvider.now()).thenReturn(now);

        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class))
            .thenReturn(Optional.of(journeyType));

        when(asylumCase.read(APPEAL_REVIEW_OUTCOME, AppealReviewOutcome.class))
            .thenReturn(Optional.of(AppealReviewOutcome.DECISION_MAINTAINED));

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
    @MethodSource("provideParameterValues")
    void should_rethrow_exception_when_scheduler_failed(JourneyType journeyType, Event event) {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(event);
        when(featureToggler.getValue("timed-event-short-delay", false)).thenReturn(false);
        when(caseDetails.getId()).thenReturn(caseId);
        when(dateProvider.now()).thenReturn(now);

        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class))
            .thenReturn(Optional.of(journeyType));

        when(asylumCase.read(APPEAL_REVIEW_OUTCOME, AppealReviewOutcome.class))
            .thenReturn(Optional.of(AppealReviewOutcome.DECISION_MAINTAINED));

        when(scheduler.schedule(any(TimedEvent.class))).thenThrow(AsylumCaseServiceResponseException.class);

        assertThatThrownBy(() -> automaticDirectionHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .isExactlyInstanceOf(AsylumCaseServiceResponseException.class);
    }

    @ParameterizedTest
    @EnumSource(value = JourneyType.class, names = {"AIP", "REP"})
    void should_skip_timed_event_when_review_outcome_is_to_withdraw(JourneyType journeyType) {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.REQUEST_RESPONSE_REVIEW);
        when(featureToggler.getValue("timed-event-short-delay", false)).thenReturn(false);
        when(dateProvider.now()).thenReturn(now);

        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class))
            .thenReturn(Optional.of(journeyType));

        when(asylumCase.read(APPEAL_REVIEW_OUTCOME, AppealReviewOutcome.class))
            .thenReturn(Optional.of(AppealReviewOutcome.DECISION_WITHDRAWN));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            automaticDirectionHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertThat(asylumCase).isEqualTo(callbackResponse.getData());

        verify(scheduler, times(0)).schedule(any(TimedEvent.class));
        verify(asylumCase, times(0)).write(AUTOMATIC_DIRECTION_REQUESTING_HEARING_REQUIREMENTS, id);
    }

    @Test
    void should_skip_timed_event_if_hearing_req_submitted_when_case_was_ada() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.REQUEST_RESPONSE_REVIEW);
        when(featureToggler.getValue("timed-event-short-delay", false)).thenReturn(false);
        when(dateProvider.now()).thenReturn(now);

        when(asylumCase.read(APPEAL_REVIEW_OUTCOME, AppealReviewOutcome.class))
            .thenReturn(Optional.of(AppealReviewOutcome.DECISION_MAINTAINED));

        when(asylumCase.read(HAS_TRANSFERRED_OUT_OF_ADA, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(ADA_HEARING_REQUIREMENTS_SUBMITTED, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            automaticDirectionHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertThat(asylumCase).isEqualTo(callbackResponse.getData());

        verify(scheduler, times(0)).schedule(any(TimedEvent.class));
        verify(asylumCase, times(0)).write(AUTOMATIC_DIRECTION_REQUESTING_HEARING_REQUIREMENTS, id);
    }

    @Test
    void should_throw_when_appeal_review_outcome_is_missing() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.REQUEST_RESPONSE_REVIEW);
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class))
            .thenReturn(Optional.of(JourneyType.AIP));
        when(featureToggler.getValue("timed-event-short-delay", false)).thenReturn(false);
        when(dateProvider.now()).thenReturn(now);
        assertThatThrownBy(() -> automaticDirectionHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Appeal Review Outcome is mandatory")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(IS_REHYDRATED_APPEAL, YesOrNo.class))
                .thenReturn(Optional.of(YesOrNo.YES));

        assertThatThrownBy(() ->
                automaticDirectionHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback)
        )
                .hasMessage("Cannot handle callback")
                .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);

        assertThatThrownBy(() ->
                automaticDirectionHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback)
        )
                .hasMessage("Cannot handle callback")
                .isExactlyInstanceOf(IllegalStateException.class);
    }


    @Test
    void should_not_schedule_request_hearing_requirements_for_rehydrated_appeal() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.REQUEST_RESPONSE_REVIEW);

        when(asylumCase.read(SOURCE_OF_APPEAL,SourceOfAppeal.class))
                .thenReturn(Optional.of(SourceOfAppeal.REHYDRATED_APPEAL));

        boolean canHandle = automaticDirectionHandler
                .canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        Assertions.assertFalse(canHandle);
        verifyNoInteractions(scheduler);
    }


    @Test
    void it_can_handle_callback() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        // ensure this is NOT rehydrated
        when(asylumCase.read(SOURCE_OF_APPEAL, SourceOfAppeal.class))
                .thenReturn(Optional.of(SourceOfAppeal.PAPER_FORM));

        for (Event event : Event.values()) {
            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = automaticDirectionHandler.canHandle(callbackStage, callback);

                boolean expected =
                        callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                                && Arrays.asList(Event.REQUEST_RESPONSE_REVIEW, Event.ADD_APPEAL_RESPONSE)
                                .contains(event);

                assertThat(canHandle).isEqualTo(expected);
            }
        }
    }


    @Test
    void handling_should_throw_for_rehydrated_appeal() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.REQUEST_RESPONSE_REVIEW);
        when(asylumCase.read(SOURCE_OF_APPEAL, SourceOfAppeal.class))
                .thenReturn(Optional.of(SourceOfAppeal.REHYDRATED_APPEAL));

        assertThatThrownBy(() ->
                automaticDirectionHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback)
        )
                .hasMessage("Cannot handle callback")
                .isExactlyInstanceOf(IllegalStateException.class);
    }


}