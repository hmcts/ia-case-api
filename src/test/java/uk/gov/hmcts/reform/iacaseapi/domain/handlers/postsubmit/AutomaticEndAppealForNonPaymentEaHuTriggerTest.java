package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
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
                        scheduler
                );
    }

    @Test
    void should_schedule_automatic_end_appeal_14_days_from_now_ea_hu_after_submission() {

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(caseDetails.getId()).thenReturn(caseId);
        when(asylumCase.read(REMISSION_TYPE, RemissionType.class))
                .thenReturn(Optional.of(RemissionType.NO_REMISSION));
        when(asylumCase.read(APPEAL_TYPE, AppealType.class))
                .thenReturn(Optional.of(AppealType.HU));
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

        automaticEndAppealForNonPaymentEaHuTrigger.handle(callback);

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
                .thenReturn(Optional.of(AppealType.EA));

        when(scheduler.schedule(any(TimedEvent.class))).thenThrow(AsylumCaseServiceResponseException.class);

        assertThatThrownBy(() -> automaticEndAppealForNonPaymentEaHuTrigger.handle(callback))
                .isExactlyInstanceOf(AsylumCaseServiceResponseException.class);
    }

    @Test
    void handling_should_throw_if_null_callback() {

        assertThatThrownBy(() -> automaticEndAppealForNonPaymentEaHuTrigger.handle(null))
                .hasMessage("callback must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> automaticEndAppealForNonPaymentEaHuTrigger.canHandle(null))
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

    @Test
    void should_end_appeal_after_14_days_detained_age_assessment_appeal() {
        dataSetUp();
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.AG));


        TimedEvent timedEvent = new TimedEvent(
                id,
                Event.END_APPEAL_AUTOMATICALLY,
                ZonedDateTime.of(dateProvider.nowWithTime(), ZoneId.systemDefault()).plusMinutes(20160),
                jurisdiction,
                caseType,
                caseId
        );
        when(scheduler.schedule(any(TimedEvent.class))).thenReturn(timedEvent);

        automaticEndAppealForNonPaymentEaHuTrigger.handle(callback);

        verify(scheduler).schedule(timedEventArgumentCaptor.capture());

        TimedEvent result = timedEventArgumentCaptor.getValue();

        assertEquals(timedEvent.getCaseId(), result.getCaseId());
        assertEquals(timedEvent.getJurisdiction(), result.getJurisdiction());
        assertEquals(timedEvent.getCaseType(), result.getCaseType());
        assertEquals(timedEvent.getEvent(), result.getEvent());
        assertEquals("", result.getId());
    }

    @Test
    void should_end_appeal_after_14_days_detained_non_age_assessment_non_accelerated_detained_HU() {
        dataSetUp();
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        TimedEvent timedEvent = new TimedEvent(
                id,
                Event.END_APPEAL_AUTOMATICALLY,
                ZonedDateTime.of(dateProvider.nowWithTime(), ZoneId.systemDefault()).plusMinutes(20160),
                jurisdiction,
                caseType,
                caseId
        );
        when(scheduler.schedule(any(TimedEvent.class))).thenReturn(timedEvent);

        automaticEndAppealForNonPaymentEaHuTrigger.handle(callback);

        verify(scheduler).schedule(timedEventArgumentCaptor.capture());

        TimedEvent result = timedEventArgumentCaptor.getValue();

        assertEquals(timedEvent.getCaseId(), result.getCaseId());
        assertEquals(timedEvent.getJurisdiction(), result.getJurisdiction());
        assertEquals(timedEvent.getCaseType(), result.getCaseType());
        assertEquals(timedEvent.getEvent(), result.getEvent());
        assertEquals("", result.getId());
    }

    @Test
    void should_end_appeal_after_14_days_detained_non_age_assessment_non_accelerated_detained_EA() {
        dataSetUp();
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(APPEAL_TYPE, AppealType.class))
                .thenReturn(Optional.of(AppealType.EA));

        TimedEvent timedEvent = new TimedEvent(
                id,
                Event.END_APPEAL_AUTOMATICALLY,
                ZonedDateTime.of(dateProvider.nowWithTime(), ZoneId.systemDefault()).plusMinutes(20160),
                jurisdiction,
                caseType,
                caseId
        );
        when(scheduler.schedule(any(TimedEvent.class))).thenReturn(timedEvent);

        automaticEndAppealForNonPaymentEaHuTrigger.handle(callback);

        verify(scheduler).schedule(timedEventArgumentCaptor.capture());

        TimedEvent result = timedEventArgumentCaptor.getValue();

        assertEquals(timedEvent.getCaseId(), result.getCaseId());
        assertEquals(timedEvent.getJurisdiction(), result.getJurisdiction());
        assertEquals(timedEvent.getCaseType(), result.getCaseType());
        assertEquals(timedEvent.getEvent(), result.getEvent());
        assertEquals("", result.getId());
    }

    @Test
    void should_end_appeal_after_14_days_nonDetained_non_age_assessment_appeal() {
        dataSetUp();
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class))
                .thenReturn(Optional.of(YesOrNo.NO));

        TimedEvent timedEvent = new TimedEvent(
                id,
                Event.END_APPEAL_AUTOMATICALLY,
                ZonedDateTime.of(dateProvider.nowWithTime(), ZoneId.systemDefault()).plusMinutes(20160),
                jurisdiction,
                caseType,
                caseId
        );
        when(scheduler.schedule(any(TimedEvent.class))).thenReturn(timedEvent);

        automaticEndAppealForNonPaymentEaHuTrigger.handle(callback);

        verify(scheduler).schedule(timedEventArgumentCaptor.capture());

        TimedEvent result = timedEventArgumentCaptor.getValue();

        assertEquals(timedEvent.getCaseId(), result.getCaseId());
        assertEquals(timedEvent.getJurisdiction(), result.getJurisdiction());
        assertEquals(timedEvent.getCaseType(), result.getCaseType());
        assertEquals(timedEvent.getEvent(), result.getEvent());
        assertEquals("", result.getId());
    }

    @Test
    void should_end_appeal_after_14_days_nonDetained_HU() {
        dataSetUp();
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class))
                .thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(AGE_ASSESSMENT, YesOrNo.class))
                .thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(APPEAL_TYPE, AppealType.class))
                .thenReturn(Optional.of(AppealType.HU));

        TimedEvent timedEvent = new TimedEvent(
                id,
                Event.END_APPEAL_AUTOMATICALLY,
                ZonedDateTime.of(dateProvider.nowWithTime(), ZoneId.systemDefault()).plusMinutes(20160),
                jurisdiction,
                caseType,
                caseId
        );
        when(scheduler.schedule(any(TimedEvent.class))).thenReturn(timedEvent);

        automaticEndAppealForNonPaymentEaHuTrigger.handle(callback);

        verify(scheduler).schedule(timedEventArgumentCaptor.capture());

        TimedEvent result = timedEventArgumentCaptor.getValue();

        assertEquals(timedEvent.getCaseId(), result.getCaseId());
        assertEquals(timedEvent.getJurisdiction(), result.getJurisdiction());
        assertEquals(timedEvent.getCaseType(), result.getCaseType());
        assertEquals(timedEvent.getEvent(), result.getEvent());
        assertEquals("", result.getId());
    }

    @Test
    void should_end_appeal_after_14_days_nonDetained_EA() {
        dataSetUp();
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class))
                .thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(AGE_ASSESSMENT, YesOrNo.class))
                .thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(APPEAL_TYPE, AppealType.class))
                .thenReturn(Optional.of(AppealType.EA));

        TimedEvent timedEvent = new TimedEvent(
                id,
                Event.END_APPEAL_AUTOMATICALLY,
                ZonedDateTime.of(dateProvider.nowWithTime(), ZoneId.systemDefault()).plusMinutes(20160),
                jurisdiction,
                caseType,
                caseId
        );
        when(scheduler.schedule(any(TimedEvent.class))).thenReturn(timedEvent);

        automaticEndAppealForNonPaymentEaHuTrigger.handle(callback);

        verify(scheduler).schedule(timedEventArgumentCaptor.capture());

        TimedEvent result = timedEventArgumentCaptor.getValue();

        assertEquals(timedEvent.getCaseId(), result.getCaseId());
        assertEquals(timedEvent.getJurisdiction(), result.getJurisdiction());
        assertEquals(timedEvent.getCaseType(), result.getCaseType());
        assertEquals(timedEvent.getEvent(), result.getEvent());
        assertEquals("", result.getId());
    }
}