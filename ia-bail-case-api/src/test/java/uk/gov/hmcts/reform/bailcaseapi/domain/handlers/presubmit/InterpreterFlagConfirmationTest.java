package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.CaseFlagDetail;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.CaseFlagValue;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.StrategicCaseFlag;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.SubmitEventDetails;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.service.CcdDataService;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class InterpreterFlagConfirmationTest {

    @Mock
    private Callback<BailCase> callback;
    @Mock
    private CaseDetails<BailCase> caseDetails;
    @Mock
    private CaseDetails<BailCase> caseDetailsBefore;
    @Mock
    private BailCase bailCase;
    @Mock
    private BailCase bailCaseBefore;
    @Mock
    private CcdDataService ccdDataService;
    @Mock
    private StrategicCaseFlag strategicCaseFlagsNow;
    @Mock
    private StrategicCaseFlag strategicCaseFlagsBefore;
    @Mock
    private List<CaseFlagDetail> caseFlagDetailsNow;
    @Mock
    private List<CaseFlagDetail> caseFlagDetailsBefore;
    @Mock
    private Stream<CaseFlagDetail> caseFlagDetailStreamNow;
    @Mock
    private Stream<CaseFlagDetail> caseFlagDetailStreamBefore;
    @Mock
    private Stream<CaseFlagDetail> caseFlagDetailFilteredNow;
    @Mock
    private Stream<CaseFlagDetail> caseFlagDetailFilteredBefore;
    private InterpreterFlagConfirmation interpreterFlagConfirmation;

    private final CaseFlagValue activeInterpreterFlag = CaseFlagValue.builder().name("Interpreter").status("Active").build();
    private final CaseFlagValue inactiveInterpreterFlag = CaseFlagValue.builder().name("Interpreter").status("Inactive").build();

    @BeforeEach
    public void setUp() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(bailCase);
        interpreterFlagConfirmation = new InterpreterFlagConfirmation(ccdDataService);
    }

    @Test
    void should_trigger_setActiveInterpreterFlag_when_interpreter_flag_status_changes() {
        when(callback.getEvent()).thenReturn(Event.CREATE_FLAG);
        when(callback.getCaseDetailsBefore()).thenReturn(Optional.of(caseDetailsBefore));
        when(caseDetailsBefore.getCaseData()).thenReturn(bailCaseBefore);
        // Interpreter flag active now, not before
        StrategicCaseFlag now = new StrategicCaseFlag("Applicant", "Applicant",
                                                      List.of(new CaseFlagDetail("id1", activeInterpreterFlag)));
        StrategicCaseFlag before = new StrategicCaseFlag("Applicant", "Applicant",
                                                         List.of(new CaseFlagDetail("id2", inactiveInterpreterFlag)));
        when(bailCase.read(any(), eq(StrategicCaseFlag.class))).thenReturn(Optional.of(now));
        when(bailCaseBefore.read(any(), eq(StrategicCaseFlag.class))).thenReturn(Optional.of(before));

        interpreterFlagConfirmation.handle(callback);

        verify(ccdDataService, times(1)).setActiveInterpreterFlag(callback);
    }

    @Test
    void should_not_trigger_setActiveInterpreterFlag_when_no_status_change() {
        when(callback.getEvent()).thenReturn(Event.CREATE_FLAG);
        when(callback.getCaseDetailsBefore()).thenReturn(Optional.of(caseDetailsBefore));
        when(caseDetailsBefore.getCaseData()).thenReturn(bailCaseBefore);

        // Interpreter flag active both now and before
        StrategicCaseFlag now = new StrategicCaseFlag("Applicant", "Applicant",
                                                      List.of(new CaseFlagDetail("id1", activeInterpreterFlag)));
        StrategicCaseFlag before = new StrategicCaseFlag("Applicant", "Applicant",
                                                         List.of(new CaseFlagDetail("id2", activeInterpreterFlag)));
        when(bailCase.read(any(), eq(StrategicCaseFlag.class))).thenReturn(Optional.of(now));
        when(bailCaseBefore.read(any(), eq(StrategicCaseFlag.class))).thenReturn(Optional.of(before));

        PostSubmitCallbackResponse response = interpreterFlagConfirmation.handle(callback);

        verify(ccdDataService, times(0)).setActiveInterpreterFlag(callback);
        assertTrue(response.getConfirmationBody().isEmpty());
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {"CREATE_FLAG", "MANAGE_FLAGS"})
    void should_handle_event(Event event) {
        when(callback.getEvent()).thenReturn(event);
        when(callback.getCaseDetailsBefore()).thenReturn(Optional.of(caseDetailsBefore));
        when(caseDetailsBefore.getCaseData()).thenReturn(bailCaseBefore);

        // Simulate flags added
        StrategicCaseFlag now = new StrategicCaseFlag("Applicant", "Applicant", List.of());
        when(bailCase.read(any(), eq(StrategicCaseFlag.class))).thenReturn(Optional.of(now));
        when(bailCaseBefore.read(any(), eq(StrategicCaseFlag.class))).thenReturn(Optional.empty());

        interpreterFlagConfirmation.handle(callback);

        verify(ccdDataService, times(1)).setActiveInterpreterFlag(callback);
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {"CREATE_FLAG", "MANAGE_FLAGS"}, mode = EnumSource.Mode.EXCLUDE)
    void should_not_handle_event(Event event) {
        when(callback.getEvent()).thenReturn(event);

        assertFalse(interpreterFlagConfirmation.canHandle(callback));
        assertThatThrownBy(() -> interpreterFlagConfirmation.handle(callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @ParameterizedTest
    @CsvSource({
        "0,0",
        "1,1",
        "2,1",
        "2,2"
    })
    void should_not_trigger_setActiveInterpreterFlag(int beforeCount, int nowCount) {
        when(callback.getEvent()).thenReturn(Event.CREATE_FLAG);
        when(callback.getCaseDetailsBefore()).thenReturn(Optional.of(caseDetailsBefore));
        when(caseDetailsBefore.getCaseData()).thenReturn(bailCaseBefore);
        setupFlagMocks(beforeCount, nowCount);

        PostSubmitCallbackResponse response = interpreterFlagConfirmation.handle(callback);

        verify(ccdDataService, times(0)).setActiveInterpreterFlag(callback);
        assertTrue(response.getConfirmationBody().isEmpty());
    }

    @ParameterizedTest
    @CsvSource({
        "0,1",
        "1,0",
        "1,2"
    })
    void should_trigger_setActiveInterpreterFlag(int beforeCount, int nowCount) {
        when(callback.getEvent()).thenReturn(Event.CREATE_FLAG);
        when(callback.getCaseDetailsBefore()).thenReturn(Optional.of(caseDetailsBefore));
        when(caseDetailsBefore.getCaseData()).thenReturn(bailCaseBefore);
        setupFlagMocks(beforeCount, nowCount);

        interpreterFlagConfirmation.handle(callback);

        verify(ccdDataService, times(1)).setActiveInterpreterFlag(callback);
    }

    @Test
    void should_return_empty_response_when_no_flags_present() {
        when(callback.getEvent()).thenReturn(Event.CREATE_FLAG);
        when(callback.getCaseDetailsBefore()).thenReturn(Optional.of(caseDetailsBefore));
        when(caseDetailsBefore.getCaseData()).thenReturn(bailCaseBefore);

        when(bailCase.read(any(), eq(StrategicCaseFlag.class))).thenReturn(Optional.empty());
        when(bailCaseBefore.read(any(), eq(StrategicCaseFlag.class))).thenReturn(Optional.empty());

        PostSubmitCallbackResponse response = interpreterFlagConfirmation.handle(callback);

        verify(ccdDataService, times(0)).setActiveInterpreterFlag(callback);
        assertTrue(response.getConfirmationBody().isEmpty());
    }

    @Test
    void should_confirmation_body_error_message_if_failed_event() {
        when(callback.getEvent()).thenReturn(Event.CREATE_FLAG);
        when(callback.getCaseDetailsBefore()).thenReturn(Optional.of(caseDetailsBefore));
        when(caseDetailsBefore.getCaseData()).thenReturn(bailCaseBefore);

        StrategicCaseFlag now = new StrategicCaseFlag("Applicant", "Applicant", List.of());
        when(bailCase.read(any(), eq(StrategicCaseFlag.class))).thenReturn(Optional.of(now));
        when(bailCaseBefore.read(any(), eq(StrategicCaseFlag.class))).thenReturn(Optional.empty());

        when(ccdDataService.setActiveInterpreterFlag(callback)).thenThrow((new RuntimeException("some error")));

        PostSubmitCallbackResponse response = interpreterFlagConfirmation.handle(callback);
        verify(ccdDataService, times(1)).setActiveInterpreterFlag(callback);
        assertTrue(response.getConfirmationBody().isPresent());
        assertEquals("### Something went wrong\n\n", response.getConfirmationBody().get());
    }

    @Test
    void should_not_confirmation_body_error_message_if_event_worked() {
        when(callback.getEvent()).thenReturn(Event.CREATE_FLAG);
        when(callback.getCaseDetailsBefore()).thenReturn(Optional.of(caseDetailsBefore));
        when(caseDetailsBefore.getCaseData()).thenReturn(bailCaseBefore);

        StrategicCaseFlag now = new StrategicCaseFlag("Applicant", "Applicant", List.of());
        when(bailCase.read(any(), eq(StrategicCaseFlag.class))).thenReturn(Optional.of(now));
        when(bailCaseBefore.read(any(), eq(StrategicCaseFlag.class))).thenReturn(Optional.empty());

        SubmitEventDetails submitEventDetails = new SubmitEventDetails();
        when(ccdDataService.setActiveInterpreterFlag(callback)).thenReturn(submitEventDetails);

        PostSubmitCallbackResponse response = interpreterFlagConfirmation.handle(callback);
        verify(ccdDataService, times(1)).setActiveInterpreterFlag(callback);
        assertTrue(response.getConfirmationBody().isEmpty());
    }

    private void setupFlagMocks(int beforeCount, int nowCount) {
        when(strategicCaseFlagsNow.getDetails()).thenReturn(caseFlagDetailsNow);
        when(strategicCaseFlagsBefore.getDetails()).thenReturn(caseFlagDetailsBefore);
        when(caseFlagDetailsNow.stream()).thenReturn(caseFlagDetailStreamNow);
        when(caseFlagDetailsBefore.stream()).thenReturn(caseFlagDetailStreamBefore);
        when(caseFlagDetailStreamNow.filter(any())).thenReturn(caseFlagDetailFilteredNow);
        when(caseFlagDetailStreamBefore.filter(any())).thenReturn(caseFlagDetailFilteredBefore);
        when(caseFlagDetailFilteredNow.count()).thenReturn(Long.valueOf(nowCount));
        when(caseFlagDetailFilteredBefore.count()).thenReturn(Long.valueOf(beforeCount));
        when(bailCase.read(any(), eq(StrategicCaseFlag.class))).thenReturn(Optional.of(strategicCaseFlagsNow));
        when(bailCaseBefore.read(any(), eq(StrategicCaseFlag.class))).thenReturn(Optional.of(strategicCaseFlagsBefore));
    }
}
