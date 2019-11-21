package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_DATE_OF_BIRTH;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HOME_OFFICE_DECISION_DATE;

import java.time.LocalDate;
import java.util.Optional;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;

import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;

@RunWith(MockitoJUnitRunner.class)
public class AppealDateFieldsHandlerTest {

    private final LocalDate now = LocalDate.now();
    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private DateProvider dateProvider;
    private AppealDateFieldsHandler appealDateFieldsHandler;

    @Before
    public void setUp() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(dateProvider.now()).thenReturn(now);

        appealDateFieldsHandler = new AppealDateFieldsHandler(dateProvider);

    }

    @Test
    public void it_can_handle_valid_dates() {

        LocalDate decisionDate = LocalDate.now().minusDays(10);
        LocalDate dob = LocalDate.now().minusYears(20);

        when(asylumCase.read(HOME_OFFICE_DECISION_DATE)).thenReturn(Optional.of(decisionDate.toString()));
        when(asylumCase.read(APPELLANT_DATE_OF_BIRTH)).thenReturn(Optional.of(dob.toString()));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            appealDateFieldsHandler.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertThat(callbackResponse.getErrors()).isEmpty();

        verify(asylumCase).read(HOME_OFFICE_DECISION_DATE);
        verify(asylumCase).read(APPELLANT_DATE_OF_BIRTH);

    }

    @Test
    public void it_can_handle_valid_dates_on_edit_appeal() {

        LocalDate decisionDate = LocalDate.now().minusDays(10);
        LocalDate dob = LocalDate.now().minusYears(20);

        when(callback.getEvent()).thenReturn(Event.EDIT_APPEAL);
        when(asylumCase.read(HOME_OFFICE_DECISION_DATE)).thenReturn(Optional.of(decisionDate.toString()));
        when(asylumCase.read(APPELLANT_DATE_OF_BIRTH)).thenReturn(Optional.of(dob.toString()));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            appealDateFieldsHandler.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertThat(callbackResponse.getErrors()).isEmpty();

        verify(asylumCase).read(HOME_OFFICE_DECISION_DATE);
        verify(asylumCase).read(APPELLANT_DATE_OF_BIRTH);

    }

    @Test
    public void handling_should_throw_error_for_ho_decision_date_in_future() {

        LocalDate decisionDate = LocalDate.now().plusDays(10);

        when(asylumCase.read(HOME_OFFICE_DECISION_DATE)).thenReturn(Optional.of(decisionDate.toString()));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            appealDateFieldsHandler.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertThat(callbackResponse.getErrors()).isNotEmpty();
        assertThat(callbackResponse.getErrors()).hasSize(1);
        assertThat(callbackResponse.getErrors()).contains("You've entered an invalid date. You cannot enter a date in the future.");

        verify(asylumCase).read(HOME_OFFICE_DECISION_DATE);
        verify(asylumCase, never()).read(APPELLANT_DATE_OF_BIRTH);

    }

    @Test
    public void handling_should_throw_error_for_ho_decision_date_in_future_on_edit_appeal() {

        LocalDate decisionDate = LocalDate.now().plusDays(10);

        when(callback.getEvent()).thenReturn(Event.EDIT_APPEAL);
        when(asylumCase.read(HOME_OFFICE_DECISION_DATE)).thenReturn(Optional.of(decisionDate.toString()));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            appealDateFieldsHandler.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertThat(callbackResponse.getErrors()).isNotEmpty();
        assertThat(callbackResponse.getErrors()).hasSize(1);
        assertThat(callbackResponse.getErrors()).contains("You've entered an invalid date. You cannot enter a date in the future.");

        verify(asylumCase).read(HOME_OFFICE_DECISION_DATE);
        verify(asylumCase, never()).read(APPELLANT_DATE_OF_BIRTH);

    }

    @Test
    public void handling_should_not_throw_error_for_ho_decision_date_is_current_day() {

        LocalDate decisionDate = LocalDate.now();

        when(asylumCase.read(HOME_OFFICE_DECISION_DATE)).thenReturn(Optional.of(decisionDate.toString()));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            appealDateFieldsHandler.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertThat(callbackResponse.getErrors()).isEmpty();

        verify(asylumCase).read(HOME_OFFICE_DECISION_DATE);
        verify(asylumCase).read(APPELLANT_DATE_OF_BIRTH);

    }

    @Test
    public void handling_should_throw_error_for_appellant_dob_in_future() {

        LocalDate decisionDate = LocalDate.now().minusDays(10);
        LocalDate dob = LocalDate.now().plusYears(1);

        when(asylumCase.read(HOME_OFFICE_DECISION_DATE)).thenReturn(Optional.of(decisionDate.toString()));
        when(asylumCase.read(APPELLANT_DATE_OF_BIRTH)).thenReturn(Optional.of(dob.toString()));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            appealDateFieldsHandler.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertThat(callbackResponse.getErrors()).isNotEmpty();
        assertThat(callbackResponse.getErrors()).hasSize(1);
        assertThat(callbackResponse.getErrors()).contains("You've entered an invalid date. You cannot enter a date in the future.");

        verify(asylumCase).read(HOME_OFFICE_DECISION_DATE);
        verify(asylumCase).read(APPELLANT_DATE_OF_BIRTH);

    }

    @Test
    public void handling_should_throw_error_for_appellant_dob_in_future_on_edit_appeal() {

        LocalDate decisionDate = LocalDate.now().minusDays(10);
        LocalDate dob = LocalDate.now().plusYears(1);

        when(callback.getEvent()).thenReturn(Event.EDIT_APPEAL);
        when(asylumCase.read(HOME_OFFICE_DECISION_DATE)).thenReturn(Optional.of(decisionDate.toString()));
        when(asylumCase.read(APPELLANT_DATE_OF_BIRTH)).thenReturn(Optional.of(dob.toString()));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            appealDateFieldsHandler.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertThat(callbackResponse.getErrors()).isNotEmpty();
        assertThat(callbackResponse.getErrors()).hasSize(1);
        assertThat(callbackResponse.getErrors()).contains("You've entered an invalid date. You cannot enter a date in the future.");

        verify(asylumCase).read(HOME_OFFICE_DECISION_DATE);
        verify(asylumCase).read(APPELLANT_DATE_OF_BIRTH);

    }

    @Test
    public void handling_should_not_throw_error_for_appellant_dob_is_current_day() {

        LocalDate decisionDate = LocalDate.now().minusDays(10);
        LocalDate dob = LocalDate.now();

        when(asylumCase.read(HOME_OFFICE_DECISION_DATE)).thenReturn(Optional.of(decisionDate.toString()));
        when(asylumCase.read(APPELLANT_DATE_OF_BIRTH)).thenReturn(Optional.of(dob.toString()));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            appealDateFieldsHandler.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertThat(callbackResponse.getErrors()).isEmpty();

        verify(asylumCase).read(HOME_OFFICE_DECISION_DATE);
        verify(asylumCase).read(APPELLANT_DATE_OF_BIRTH);

    }

    @Test
    public void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> appealDateFieldsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = appealDateFieldsHandler.canHandle(callbackStage, callback);

                if ((event == Event.START_APPEAL || event == Event.EDIT_APPEAL)
                    && (callbackStage == PreSubmitCallbackStage.MID_EVENT)) {

                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }
        }
    }

    @Test
    public void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> appealDateFieldsHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> appealDateFieldsHandler.canHandle(PreSubmitCallbackStage.MID_EVENT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> appealDateFieldsHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> appealDateFieldsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

}
