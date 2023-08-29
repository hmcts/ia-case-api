package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.AUTOMATIC_END_APPEAL_TIMED_EVENT_ID;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.JOURNEY_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.PAYMENT_STATUS;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.JourneyType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.PaymentStatus;
import uk.gov.hmcts.reform.iacaseapi.domain.service.Scheduler;

@ExtendWith(MockitoExtension.class)
class CancelAutomaticEndAppealPaidConfirmationTest {

    private static final String timedEventId = "1234567";

    @Mock
    private Scheduler scheduler;
    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Captor
    private ArgumentCaptor<String> stringArgumentCaptor;

    private CancelAutomaticEndAppealPaidConfirmation cancelAutomaticEndAppealHandler;

    @BeforeEach
    public void setUp() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        cancelAutomaticEndAppealHandler =
            new CancelAutomaticEndAppealPaidConfirmation(true, scheduler);
    }

    private static Stream<Arguments> qualifyingEvent() {
        return Stream.of(
            Arguments.of(JourneyType.AIP, Event.PAYMENT_APPEAL),
            Arguments.of(null, Event.UPDATE_PAYMENT_STATUS)
        );
    }

    @ParameterizedTest
    @MethodSource("qualifyingEvent")
    void should_call_delete_schedule_automatic_end_appeal(JourneyType journeyType, Event event) {
        // Given: a paid asylum case
        given(callback.getEvent()).willReturn(event);
        given(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).willReturn(Optional.ofNullable(journeyType));
        given(asylumCase.read(PAYMENT_STATUS, PaymentStatus.class)).willReturn(Optional.of(PaymentStatus.PAID));
        given(asylumCase.read(AUTOMATIC_END_APPEAL_TIMED_EVENT_ID)).willReturn(Optional.of(timedEventId));

        // When: the handler is called
        when(scheduler.deleteSchedule(timedEventId)).thenReturn(true);
        cancelAutomaticEndAppealHandler.handle(callback);

        // Then: the deleteSchedule method is called with correct arguments
        verify(scheduler).deleteSchedule(stringArgumentCaptor.capture());
        String arg = stringArgumentCaptor.getValue();
        assertEquals(timedEventId, arg);
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(
                () -> cancelAutomaticEndAppealHandler.handle(callback))
                .hasMessage("Cannot handle callback")
                .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.UPDATE_PAYMENT_STATUS);
        assertThatThrownBy(
                () -> cancelAutomaticEndAppealHandler.handle(callback))
                .hasMessage("Cannot handle callback")
                .isExactlyInstanceOf(IllegalStateException.class);

        when(asylumCase.read(PAYMENT_STATUS, PaymentStatus.class)).thenReturn(Optional.of(PaymentStatus.PAID));
        assertThatThrownBy(
                () -> cancelAutomaticEndAppealHandler.handle(callback))
                .hasMessage("Cannot handle callback")
                .isExactlyInstanceOf(IllegalStateException.class);
    }

    private static Stream<Arguments> qualifyingEventCombinations() {
        List<Arguments> argumentsList = new ArrayList<>();

        List<String> timedEventIdValues = new ArrayList<>();
        timedEventIdValues.add(timedEventId);
        timedEventIdValues.add(null);

        for (Event currentEvent : Event.values()) {
            for (PaymentStatus paymentStatus : PaymentStatus.values()) {
                for (String timedEventIdValue : timedEventIdValues) {
                    argumentsList.add(Arguments.of(JourneyType.AIP, Event.PAYMENT_APPEAL, currentEvent, paymentStatus, timedEventIdValue));
                    argumentsList.add(Arguments.of(null, Event.UPDATE_PAYMENT_STATUS, currentEvent, paymentStatus, timedEventIdValue));
                }
            }
        }
        return argumentsList.stream();
    }


    @ParameterizedTest
    @MethodSource("qualifyingEventCombinations")
    void it_can_handle_callback(JourneyType journeyType, Event qualifyingEvent, Event currentEvent, PaymentStatus paymentStatus, String timedEventIdValue) {
        // Given
        given(callback.getEvent()).willReturn(currentEvent);
        given(asylumCase.read(PAYMENT_STATUS, PaymentStatus.class)).willReturn(Optional.of(paymentStatus));
        given(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).willReturn(Optional.ofNullable(journeyType));
        given(asylumCase.read(AUTOMATIC_END_APPEAL_TIMED_EVENT_ID)).willReturn(Optional.ofNullable(timedEventIdValue));

        // When
        boolean canHandle = cancelAutomaticEndAppealHandler.canHandle(callback);

        // Then
        if (currentEvent == qualifyingEvent && paymentStatus == PaymentStatus.PAID
            && timedEventIdValue != null) {
            assertThat(canHandle).isTrue();
        } else {
            assertThat(canHandle).isFalse();
        }
    }

}
