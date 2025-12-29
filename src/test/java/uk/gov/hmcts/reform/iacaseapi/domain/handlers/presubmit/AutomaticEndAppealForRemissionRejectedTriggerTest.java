package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

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
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionDecision;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.PaymentStatus;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.service.Scheduler;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.ServiceResponseException;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.TimedEvent;


@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class AutomaticEndAppealForRemissionRejectedTriggerTest {

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

    private AutomaticEndAppealForRemissionRejectedTrigger autoEndAppealTrigger;

    @BeforeEach
    void setUp() {

        autoEndAppealTrigger =
            new AutomaticEndAppealForRemissionRejectedTrigger(
                dateProvider,
                scheduler
            );
    }

    @Test
    void should_schedule_hu_appeal_automatic_end_appeal_14_days_from_now() {

        when(callback.getEvent()).thenReturn(Event.RECORD_REMISSION_DECISION);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(caseDetails.getId()).thenReturn(caseId);
        when(asylumCase.read(REMISSION_DECISION, RemissionDecision.class))
            .thenReturn(Optional.of(RemissionDecision.REJECTED));
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

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            autoEndAppealTrigger.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        verify(scheduler).schedule(timedEventArgumentCaptor.capture());

        TimedEvent result = timedEventArgumentCaptor.getValue();

        assertEquals(timedEvent.getCaseId(), result.getCaseId());
        assertEquals(timedEvent.getJurisdiction(), result.getJurisdiction());
        assertEquals(timedEvent.getCaseType(), result.getCaseType());
        assertEquals(timedEvent.getEvent(), result.getEvent());
        assertEquals("", result.getId());
    }

    @Test
    void should_schedule_ea_appeal_automatic_end_appeal_14_days_from_now() {

        when(callback.getEvent()).thenReturn(Event.RECORD_REMISSION_DECISION);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(caseDetails.getId()).thenReturn(caseId);
        when(asylumCase.read(REMISSION_DECISION, RemissionDecision.class))
            .thenReturn(Optional.of(RemissionDecision.REJECTED));
        when(asylumCase.read(APPEAL_TYPE, AppealType.class))
            .thenReturn(Optional.of(AppealType.EA));
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

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            autoEndAppealTrigger.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        verify(scheduler).schedule(timedEventArgumentCaptor.capture());

        TimedEvent result = timedEventArgumentCaptor.getValue();

        assertEquals(timedEvent.getCaseId(), result.getCaseId());
        assertEquals(timedEvent.getJurisdiction(), result.getJurisdiction());
        assertEquals(timedEvent.getCaseType(), result.getCaseType());
        assertEquals(timedEvent.getEvent(), result.getEvent());
        assertEquals("", result.getId());
    }

    @Test
    void should_fail_can_handle_for_pa_appeal_type() {

        when(callback.getEvent()).thenReturn(Event.RECORD_REMISSION_DECISION);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(REMISSION_DECISION, RemissionDecision.class))
            .thenReturn(Optional.of(RemissionDecision.REJECTED));
        when(asylumCase.read(APPEAL_TYPE, AppealType.class))
            .thenReturn(Optional.of(AppealType.PA));

        assertThatThrownBy(() -> autoEndAppealTrigger.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_fail_can_handle_for_paid_appeals() {

        when(callback.getEvent()).thenReturn(Event.RECORD_REMISSION_DECISION);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(REMISSION_DECISION, RemissionDecision.class))
            .thenReturn(Optional.of(RemissionDecision.REJECTED));
        when(asylumCase.read(APPEAL_TYPE, AppealType.class))
            .thenReturn(Optional.of(AppealType.HU));
        when(asylumCase.read(PAYMENT_STATUS, PaymentStatus.class))
            .thenReturn(Optional.of(PaymentStatus.PAID));

        assertThatThrownBy(() -> autoEndAppealTrigger.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_rethrow_exception_when_scheduler_failed() {
        when(callback.getEvent()).thenReturn(Event.RECORD_REMISSION_DECISION);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(caseDetails.getId()).thenReturn(caseId);
        when(dateProvider.nowWithTime()).thenReturn(now);
        when(asylumCase.read(REMISSION_DECISION, RemissionDecision.class))
            .thenReturn(Optional.of(RemissionDecision.REJECTED));
        when(asylumCase.read(APPEAL_TYPE, AppealType.class))
            .thenReturn(Optional.of(AppealType.HU));
        when(scheduler.schedule(any(TimedEvent.class))).thenThrow(ServiceResponseException.class);

        assertThatThrownBy(() -> autoEndAppealTrigger.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .isExactlyInstanceOf(ServiceResponseException.class);
    }

    @Test
    void handling_should_throw_if_null_callback() {

        assertThatThrownBy(() -> autoEndAppealTrigger.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> autoEndAppealTrigger.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void should_fail_can_handle_for_ada_appeals() {
        when(callback.getEvent()).thenReturn(Event.RECORD_REMISSION_DECISION);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(asylumCase.read(REMISSION_DECISION, RemissionDecision.class))
                .thenReturn(Optional.of(RemissionDecision.REJECTED));
        when(asylumCase.read(APPEAL_TYPE, AppealType.class))
                .thenReturn(Optional.of(AppealType.HU));
        when(asylumCase.read(PAYMENT_STATUS, PaymentStatus.class))
                .thenReturn(Optional.of(PaymentStatus.PAYMENT_PENDING));
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class))
                .thenReturn(Optional.of(YesOrNo.YES));

        assertThatThrownBy(() -> autoEndAppealTrigger.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
                .hasMessage("Cannot handle callback for auto end appeal for remission rejection")
                .isExactlyInstanceOf(IllegalStateException.class);
    }
}
