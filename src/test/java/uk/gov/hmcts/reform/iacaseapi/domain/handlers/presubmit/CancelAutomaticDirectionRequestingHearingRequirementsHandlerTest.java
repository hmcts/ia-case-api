package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.AUTOMATIC_DIRECTION_REQUESTING_HEARING_REQUIREMENTS;

import java.time.*;
import java.util.Arrays;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.service.Scheduler;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.TimedEvent;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class CancelAutomaticDirectionRequestingHearingRequirementsHandlerTest {


    @Mock
    private DateProvider dateProvider;
    @Mock
    private Scheduler scheduler;

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private AsylumCase asylumCase;

    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Captor private ArgumentCaptor<TimedEvent> timedEventArgumentCaptor;

    private boolean timedEventServiceEnabled = true;
    private LocalDate now = LocalDate.now();
    private String id = "someId";
    private String timedEventId = "1234567";
    private long caseId = 12345;
    private String jurisdiction = "IA";
    private String caseType = "Asylum";

    private CancelAutomaticDirectionRequestingHearingRequirementsHandler cancelAutomaticDirectionHandler;

    @Before
    public void setUp() {

        cancelAutomaticDirectionHandler =
            new CancelAutomaticDirectionRequestingHearingRequirementsHandler(
                timedEventServiceEnabled,
                dateProvider,
                scheduler
            );
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
    }

    @Test
    public void should_schedule_automatic_direction_100_years_from_now() {
        when(callback.getEvent()).thenReturn(Event.SEND_DIRECTION);

        TimedEvent timedEvent = new TimedEvent(
            timedEventId,
            Event.REQUEST_HEARING_REQUIREMENTS_FEATURE,
            ZonedDateTime.of(now.plusDays(52560000), LocalTime.MIDNIGHT, ZoneId.systemDefault()),
            jurisdiction,
            caseType,
            caseId
        );
        when(asylumCase.read(AUTOMATIC_DIRECTION_REQUESTING_HEARING_REQUIREMENTS)).thenReturn(Optional.of(timedEventId));
        when(scheduler.schedule(any(TimedEvent.class))).thenReturn(timedEvent);

        when(dateProvider.nowWithTime()).thenReturn(LocalDateTime.now());
        when(callback.getCaseDetails().getId()).thenReturn(12345L);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                cancelAutomaticDirectionHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertThat(asylumCase).isEqualTo(callbackResponse.getData());


        verify(scheduler).schedule(timedEventArgumentCaptor.capture());

        TimedEvent result = timedEventArgumentCaptor.getValue();

        assertEquals(timedEvent.getCaseId(), result.getCaseId());
        assertEquals(timedEvent.getJurisdiction(), result.getJurisdiction());
        assertEquals(timedEvent.getCaseType(), result.getCaseType());
        assertEquals(timedEvent.getEvent(), result.getEvent());
        assertEquals(timedEvent.getId(), result.getId());
    }

    @Test
    public void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> cancelAutomaticDirectionHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        assertThatThrownBy(() -> cancelAutomaticDirectionHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = cancelAutomaticDirectionHandler.canHandle(callbackStage, callback);

                if (timedEventServiceEnabled
                    && callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && Arrays.asList(
                            Event.SEND_DIRECTION,
                            Event.RECORD_APPLICATION)
                        .contains(event)) {

                    assertThat(canHandle).isEqualTo(true);
                } else {
                    assertThat(canHandle).isEqualTo(false);
                }
            }

            reset(callback);
        }
    }

}
