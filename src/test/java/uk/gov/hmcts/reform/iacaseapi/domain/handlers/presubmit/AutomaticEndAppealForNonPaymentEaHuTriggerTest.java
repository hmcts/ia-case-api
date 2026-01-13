package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.stream.Stream;
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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.JourneyType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.service.Scheduler;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.AsylumCaseServiceResponseException;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.TimedEvent;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
@MockitoSettings(strictness = Strictness.LENIENT)
class AutomaticEndAppealForNonPaymentEaHuTriggerTest {

    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;
    @Mock
    private DateProvider dateProvider;
    @Mock
    private Scheduler scheduler;

    @Captor
    private ArgumentCaptor<TimedEvent> timedEventArgumentCaptor;
    private final LocalDateTime now = LocalDateTime.now();
    private final String id = "someId";
    private final long caseId = 12345;

    private final String jurisdiction = "IA";
    private final String caseType = "Asylum";

    private AutomaticEndAppealForNonPaymentEaHuTrigger automaticEndAppealForNonPaymentEaHuTrigger;

    @BeforeEach
    void setUp() {

        automaticEndAppealForNonPaymentEaHuTrigger =
            new AutomaticEndAppealForNonPaymentEaHuTrigger(
                dateProvider,
                scheduler,
                    10
            );
    }

    private static Stream<Arguments> aipLrScheduleAppeal() {
        return Stream.of(
            Arguments.of(Optional.of(JourneyType.AIP), Optional.empty(), Optional.of(RemissionOption.NO_REMISSION), Optional.empty(), EA), // AIP = chose no remission
            Arguments.of(Optional.of(JourneyType.AIP), Optional.empty(), Optional.empty(), Optional.empty(), HU),
            Arguments.of(Optional.of(JourneyType.AIP), Optional.empty(), Optional.empty(), Optional.empty(), EU),
            Arguments.of(Optional.empty(), Optional.of(RemissionType.NO_REMISSION), Optional.empty(), Optional.empty(), EA), // LR = chose no remissions
            Arguments.of(Optional.empty(), Optional.of(RemissionType.NO_REMISSION), Optional.empty(), Optional.empty(), HU),
            Arguments.of(Optional.of(JourneyType.REP), Optional.of(RemissionType.NO_REMISSION), Optional.empty(), Optional.empty(), EU),
            Arguments.of(Optional.of(JourneyType.AIP), Optional.empty(), Optional.of(RemissionOption.NO_REMISSION), Optional.of(HelpWithFeesOption.WILL_PAY_FOR_APPEAL), EU)
        );
    }

    @ParameterizedTest
    @MethodSource("aipLrScheduleAppeal")
    void should_schedule_automatic_end_appeal_14_days_from_now_ea_hu_eu_after_submission(
            Optional<JourneyType> journeyType,
            Optional<RemissionType> remissionType,
            Optional<RemissionOption> aipRemissionOption,
            Optional<HelpWithFeesOption> aipHelpWithFeesOption,
            AppealType appealType
    ) {
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(caseDetails.getId()).thenReturn(caseId);
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(journeyType);
        when(asylumCase.read(REMISSION_TYPE, RemissionType.class)).thenReturn(remissionType);
        when(asylumCase.read(REMISSION_OPTION, RemissionOption.class)).thenReturn(aipRemissionOption);
        when(asylumCase.read(HELP_WITH_FEES_OPTION, HelpWithFeesOption.class)).thenReturn(aipHelpWithFeesOption);
        when(dateProvider.nowWithTime()).thenReturn(now);
        TimedEvent timedEvent = new TimedEvent(
            id,
            Event.END_APPEAL_AUTOMATICALLY,
            ZonedDateTime.of(dateProvider.nowWithTime(), ZoneId.systemDefault()).plusMinutes(20160),
            jurisdiction,
            caseType,
            caseId
        );
        when(scheduler.schedule(any(TimedEvent.class))).thenReturn(timedEvent);

        when(asylumCase.read(APPEAL_TYPE, AppealType.class))
            .thenReturn(Optional.of(appealType));

        automaticEndAppealForNonPaymentEaHuTrigger.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        verify(scheduler).schedule(timedEventArgumentCaptor.capture());

        TimedEvent result = timedEventArgumentCaptor.getValue();

        assertEquals(timedEvent.getCaseId(), result.getCaseId());
        assertEquals(timedEvent.getJurisdiction(), result.getJurisdiction());
        assertEquals(timedEvent.getCaseType(), result.getCaseType());
        assertEquals(timedEvent.getEvent(), result.getEvent());
        assertEquals("", result.getId());
    }

    @Test
    void should_rethrow_exception_when_scheduler_failed() {
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(caseDetails.getId()).thenReturn(caseId);
        when(dateProvider.nowWithTime()).thenReturn(now);
        when(asylumCase.read(REMISSION_TYPE, RemissionType.class))
            .thenReturn(Optional.of(RemissionType.NO_REMISSION));
        when(asylumCase.read(APPEAL_TYPE, AppealType.class))
            .thenReturn(Optional.of(EA));

        when(scheduler.schedule(any(TimedEvent.class))).thenThrow(AsylumCaseServiceResponseException.class);

        assertThatThrownBy(() -> automaticEndAppealForNonPaymentEaHuTrigger.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .isExactlyInstanceOf(AsylumCaseServiceResponseException.class);
    }

    @Test
    void handling_should_throw_if_null_callback() {

        assertThatThrownBy(() -> automaticEndAppealForNonPaymentEaHuTrigger.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> automaticEndAppealForNonPaymentEaHuTrigger.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    private void dataSetUp() {
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(caseDetails.getId()).thenReturn(caseId);

        when(asylumCase.read(REMISSION_TYPE, RemissionType.class))
                .thenReturn(Optional.of(RemissionType.NO_REMISSION));

        when(dateProvider.nowWithTime()).thenReturn(now);

        when(asylumCase.read(APPEAL_TYPE, AppealType.class))
                .thenReturn(Optional.of(AppealType.HU));

        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class))
                .thenReturn(Optional.of(YesOrNo.NO));

        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class))
                .thenReturn(Optional.of(YesOrNo.NO));

        when(asylumCase.read(AGE_ASSESSMENT, YesOrNo.class))
                .thenReturn(Optional.of(YesOrNo.NO));
    }

    @ParameterizedTest
    @EnumSource(value = AppealType.class, names = {"EA", "HU", "EU", "AG"})
    void should_end_appeal_after_14_days_detained_age_assessment_appeal(AppealType appealType) {
        dataSetUp();
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(appealType));


        TimedEvent timedEvent = new TimedEvent(
                id,
                Event.END_APPEAL_AUTOMATICALLY,
                ZonedDateTime.of(dateProvider.nowWithTime(), ZoneId.systemDefault()).plusMinutes(20160),
                jurisdiction,
                caseType,
                caseId
        );
        when(scheduler.schedule(any(TimedEvent.class))).thenReturn(timedEvent);

        automaticEndAppealForNonPaymentEaHuTrigger.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        verify(scheduler).schedule(timedEventArgumentCaptor.capture());

        TimedEvent result = timedEventArgumentCaptor.getValue();

        assertEquals(timedEvent.getCaseId(), result.getCaseId());
        assertEquals(timedEvent.getJurisdiction(), result.getJurisdiction());
        assertEquals(timedEvent.getCaseType(), result.getCaseType());
        assertEquals(timedEvent.getEvent(), result.getEvent());
        assertEquals("", result.getId());
    }

    @ParameterizedTest
    @EnumSource(value = AppealType.class, names = {"EA", "HU", "EU", "AG"})
    void should_end_appeal_after_14_days_detained_non_age_assessment_non_accelerated_detained(AppealType appealType) {
        dataSetUp();
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(appealType));

        TimedEvent timedEvent = new TimedEvent(
                id,
                Event.END_APPEAL_AUTOMATICALLY,
                ZonedDateTime.of(dateProvider.nowWithTime(), ZoneId.systemDefault()).plusMinutes(20160),
                jurisdiction,
                caseType,
                caseId
        );
        when(scheduler.schedule(any(TimedEvent.class))).thenReturn(timedEvent);

        automaticEndAppealForNonPaymentEaHuTrigger.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        verify(scheduler).schedule(timedEventArgumentCaptor.capture());

        TimedEvent result = timedEventArgumentCaptor.getValue();

        assertEquals(timedEvent.getCaseId(), result.getCaseId());
        assertEquals(timedEvent.getJurisdiction(), result.getJurisdiction());
        assertEquals(timedEvent.getCaseType(), result.getCaseType());
        assertEquals(timedEvent.getEvent(), result.getEvent());
        assertEquals("", result.getId());
    }

    @ParameterizedTest
    @EnumSource(value = AppealType.class, names = {"EA", "HU", "EU", "AG"})
    void should_end_appeal_after_14_days_nonDetained_age_assessment(AppealType appealType) {
        dataSetUp();
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class))
                .thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(APPEAL_TYPE, AppealType.class))
                .thenReturn(Optional.of(appealType));

        TimedEvent timedEvent = new TimedEvent(
                id,
                Event.END_APPEAL_AUTOMATICALLY,
                ZonedDateTime.of(dateProvider.nowWithTime(), ZoneId.systemDefault()).plusMinutes(20160),
                jurisdiction,
                caseType,
                caseId
        );
        when(scheduler.schedule(any(TimedEvent.class))).thenReturn(timedEvent);

        automaticEndAppealForNonPaymentEaHuTrigger.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        verify(scheduler).schedule(timedEventArgumentCaptor.capture());

        TimedEvent result = timedEventArgumentCaptor.getValue();

        assertEquals(timedEvent.getCaseId(), result.getCaseId());
        assertEquals(timedEvent.getJurisdiction(), result.getJurisdiction());
        assertEquals(timedEvent.getCaseType(), result.getCaseType());
        assertEquals(timedEvent.getEvent(), result.getEvent());
        assertEquals("", result.getId());
    }

    @ParameterizedTest
    @EnumSource(value = AppealType.class, names = {"EA", "HU", "EU", "AG"})
    void should_end_appeal_after_14_days_nonDetained_non_age_assessment(AppealType appealType) {
        dataSetUp();
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class))
                .thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(APPEAL_TYPE, AppealType.class))
                .thenReturn(Optional.of(appealType));

        TimedEvent timedEvent = new TimedEvent(
                id,
                Event.END_APPEAL_AUTOMATICALLY,
                ZonedDateTime.of(dateProvider.nowWithTime(), ZoneId.systemDefault()).plusMinutes(20160),
                jurisdiction,
                caseType,
                caseId
        );
        when(scheduler.schedule(any(TimedEvent.class))).thenReturn(timedEvent);

        automaticEndAppealForNonPaymentEaHuTrigger.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        verify(scheduler).schedule(timedEventArgumentCaptor.capture());

        TimedEvent result = timedEventArgumentCaptor.getValue();

        assertEquals(timedEvent.getCaseId(), result.getCaseId());
        assertEquals(timedEvent.getJurisdiction(), result.getJurisdiction());
        assertEquals(timedEvent.getCaseType(), result.getCaseType());
        assertEquals(timedEvent.getEvent(), result.getEvent());
        assertEquals("", result.getId());
    }
    
    @Test
    void handling_should_throw_if_can_not_handle() {
        when(callback.getEvent()).thenReturn(Event.RECORD_REMISSION_DECISION); // unqualified event
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        assertThatThrownBy(() -> automaticEndAppealForNonPaymentEaHuTrigger.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback for auto end appeal for remission rejection")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void handling_should_throw_if_aip_with_remission_appeal_can_not_handle() {
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL); // unqualified event
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.AIP));
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(EU));
        when(asylumCase.read(REMISSION_OPTION, RemissionOption.class))
                .thenReturn(Optional.of(RemissionOption.ASYLUM_SUPPORT_FROM_HOME_OFFICE));

        assertThatThrownBy(() -> automaticEndAppealForNonPaymentEaHuTrigger.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback for auto end appeal for remission rejection")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void handling_should_throw_if_aip_with_hwf_remission_appeal_can_not_handle() {
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL); // unqualified event
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.AIP));
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(EU));
        when(asylumCase.read(REMISSION_OPTION, RemissionOption.class))
                .thenReturn(Optional.of(RemissionOption.NO_REMISSION));
        when(asylumCase.read(HELP_WITH_FEES_OPTION, HelpWithFeesOption.class))
                .thenReturn(Optional.of(HelpWithFeesOption.ALREADY_APPLIED));

        assertThatThrownBy(() -> automaticEndAppealForNonPaymentEaHuTrigger.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback for auto end appeal for remission rejection")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_fail_can_handle_for_ada_appeals() {
        dataSetUp();
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class))
                .thenReturn(Optional.of(YesOrNo.YES));

        assertThatThrownBy(() -> automaticEndAppealForNonPaymentEaHuTrigger.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
                .hasMessage("Cannot handle callback for auto end appeal for remission rejection")
                .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_not_trigger_event_if_the_appeal_is_rehydrated() {
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(caseDetails.getId()).thenReturn(caseId);

        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(EA));

        when(asylumCase.read(REMISSION_TYPE, RemissionType.class))
                .thenReturn(Optional.of(RemissionType.NO_REMISSION));

        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class))
                .thenReturn(Optional.of(YesOrNo.NO));

        when(asylumCase.read(IS_NOTIFICATION_TURNED_OFF, YesOrNo.class))
                .thenReturn(Optional.of(YesOrNo.YES));

        verifyNoInteractions(scheduler);
    }

    @Test
    void canHandle_should_return_true_for_rehydrated_appeals() {
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(EA));
        when(asylumCase.read(REMISSION_TYPE, RemissionType.class)).thenReturn(Optional.of(RemissionType.NO_REMISSION));
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(IS_NOTIFICATION_TURNED_OFF, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        boolean result = automaticEndAppealForNonPaymentEaHuTrigger
                .canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertTrue(result);
    }



}
