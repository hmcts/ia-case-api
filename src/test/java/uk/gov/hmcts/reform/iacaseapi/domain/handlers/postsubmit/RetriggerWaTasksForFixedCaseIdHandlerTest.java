package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.service.CcdDataService;

import java.time.ZonedDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RetriggerWaTasksForFixedCaseIdHandlerTest {


    @Mock
    private CcdDataService ccdDataService;
    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Captor
    private ArgumentCaptor<String> caseIdCaptor;
    @Captor
    private ArgumentCaptor<ZonedDateTime> scheduledDateCaptor;
    @Captor
    private ArgumentCaptor<Event> eventCaptor;
    @Captor
    private ArgumentCaptor<String> timedEventIdCaptor;

    private long caseId = Long.parseLong("1677132005196104");
    private String jurisdiction = "IA";
    private String caseType = "Asylum";
    private PreSubmitCallbackStage callbackStage = PreSubmitCallbackStage.ABOUT_TO_SUBMIT;
    private RetriggerWaTasksForFixedCaseIdHandler retriggerWaTasksForFixedCaseIdHandler;

    @BeforeEach
    public void setUp() {
        retriggerWaTasksForFixedCaseIdHandler = new RetriggerWaTasksForFixedCaseIdHandler(ccdDataService);
    }

    @Test
    void handle_callback_should_return_expected() {
        when(callback.getEvent()).thenReturn(Event.RE_TRIGGER_WA_BULK_TASKS);
        boolean canHandle = retriggerWaTasksForFixedCaseIdHandler.canHandle(callbackStage, callback);
        assertThat(canHandle).isEqualTo(true);

        canHandle = retriggerWaTasksForFixedCaseIdHandler.canHandle(PreSubmitCallbackStage.MID_EVENT, callback);
        assertThat(canHandle).isEqualTo(false);

        when(callback.getEvent()).thenReturn(Event.PAYMENT_APPEAL);
        canHandle = retriggerWaTasksForFixedCaseIdHandler.canHandle(callbackStage, callback);
        assertThat(canHandle).isEqualTo(false);
    }

    @Test
    void handle_callback_should_return_false_timed_event_service_disabled() {
        boolean canHandle = retriggerWaTasksForFixedCaseIdHandler.canHandle(callbackStage, callback);
        assertThat(canHandle).isEqualTo(false);
    }

    @Test
    void handling_should_throw_if_cannot_handle() {
        assertThatThrownBy(
            () -> retriggerWaTasksForFixedCaseIdHandler.handle(callbackStage, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void handling_should_throw_if_null_callback() {
        assertThatThrownBy(
            () -> retriggerWaTasksForFixedCaseIdHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
        assertThatThrownBy(
            () -> retriggerWaTasksForFixedCaseIdHandler.handle(callbackStage, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void should_not_trigger_anything_when_case_list_field_null() {
        when(callback.getEvent()).thenReturn(Event.RE_TRIGGER_WA_BULK_TASKS);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        retriggerWaTasksForFixedCaseIdHandler.handle(callbackStage, callback);

        verify(ccdDataService, times(0)).retriggerWaTasks(anyString());
        verify(asylumCase, times(0)).clear(AsylumCaseFieldDefinition.CASE_ID_LIST);
    }

    @Test
    void should_not_trigger_anything_when_case_list_field_empty() {
        when(callback.getEvent()).thenReturn(Event.RE_TRIGGER_WA_BULK_TASKS);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(AsylumCaseFieldDefinition.CASE_ID_LIST, String.class))
            .thenReturn(java.util.Optional.of(""));

        retriggerWaTasksForFixedCaseIdHandler.handle(callbackStage, callback);

        verify(ccdDataService, times(0)).retriggerWaTasks(anyString());
        verify(asylumCase, times(0)).clear(AsylumCaseFieldDefinition.CASE_ID_LIST);

    }

    @Test
    void should_only_trigger_for_valid_case_ids() {
        when(callback.getEvent()).thenReturn(Event.RE_TRIGGER_WA_BULK_TASKS);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(AsylumCaseFieldDefinition.CASE_ID_LIST, String.class))
            .thenReturn(java.util.Optional.of("1,2,3,5,6,8,2,1,26,8,1677132005196104"));
        retriggerWaTasksForFixedCaseIdHandler.handle(callbackStage, callback);

        verify(ccdDataService, times(1)).retriggerWaTasks(caseIdCaptor.capture());
        verify(asylumCase, times(1)).clear(AsylumCaseFieldDefinition.CASE_ID_LIST);

        String finalCaseId = caseIdCaptor.getValue();
        assertEquals("1677132005196104", finalCaseId);
        List<String> capturedCaseIds = caseIdCaptor.getAllValues();
        List<String> expectedCaseIds = List.of("1677132005196104");
        assertEquals(expectedCaseIds, capturedCaseIds);
    }

    @Test
    void should_trigger_re_trigger_wa_tasks_for_all_case_ids() {
        when(callback.getEvent()).thenReturn(Event.RE_TRIGGER_WA_BULK_TASKS);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(AsylumCaseFieldDefinition.CASE_ID_LIST, String.class))
            .thenReturn(java.util.Optional.of("5260728023204485, 7829484608979593, 3007004947258233," +
                "4719620009252072,6797092066725243,9301281768878771,8509676174519453," +
                "1682542357170697,3673342967892569,1677132005196104"));
        retriggerWaTasksForFixedCaseIdHandler.handle(callbackStage, callback);

        verify(ccdDataService, times(10)).retriggerWaTasks(caseIdCaptor.capture());
        verify(asylumCase, times(1)).clear(AsylumCaseFieldDefinition.CASE_ID_LIST);

        String finalCaseId = caseIdCaptor.getValue();

        assertEquals("1677132005196104", finalCaseId);
        List<String> capturedCaseIds = caseIdCaptor.getAllValues();
        List<String> expectedCaseIds = List.of(
            "5260728023204485",
            "7829484608979593",
            "3007004947258233",
            "4719620009252072",
            "6797092066725243",
            "9301281768878771",
            "8509676174519453",
            "1682542357170697",
            "3673342967892569",
            "1677132005196104"
        );

        assertEquals(expectedCaseIds, capturedCaseIds);
    }


    @Test
    void should_trigger_re_trigger_wa_tasks_for_one_case_ids() {
        when(callback.getEvent()).thenReturn(Event.RE_TRIGGER_WA_BULK_TASKS);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(AsylumCaseFieldDefinition.CASE_ID_LIST, String.class))
            .thenReturn(java.util.Optional.of("5260728023204485"));
        retriggerWaTasksForFixedCaseIdHandler.handle(callbackStage, callback);

        verify(ccdDataService, times(1)).retriggerWaTasks(caseIdCaptor.capture());
        verify(asylumCase, times(1)).clear(AsylumCaseFieldDefinition.CASE_ID_LIST);

        String finalCaseId = caseIdCaptor.getValue();

        assertEquals("5260728023204485", finalCaseId);
        List<String> capturedCaseIds = caseIdCaptor.getAllValues();
        List<String> expectedCaseIds = List.of("5260728023204485");

        assertEquals(expectedCaseIds, capturedCaseIds);
    }
}
