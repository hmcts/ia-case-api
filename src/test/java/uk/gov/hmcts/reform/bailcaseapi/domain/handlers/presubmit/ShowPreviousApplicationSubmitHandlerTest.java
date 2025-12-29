package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition.PREV_APP_APPLICANT_DOCS_DETAILS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition.PREV_APP_APPLICANT_INFO;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition.PREV_APP_DECISION_DETAILS_LABEL;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition.PREV_APP_DIRECTION_DETAILS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition.PREV_APP_FINANCIAL_COND_COMMITMENT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition.PREV_APP_FINANCIAL_COND_SUPPORTER1;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition.PREV_APP_FINANCIAL_COND_SUPPORTER2;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition.PREV_APP_FINANCIAL_COND_SUPPORTER3;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition.PREV_APP_FINANCIAL_COND_SUPPORTER4;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition.PREV_APP_GROUNDS_FOR_BAIL;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition.PREV_APP_HEARING_REQ_DETAILS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition.PREV_APP_LEGAL_REP_DETAILS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition.PREV_APP_PERSONAL_INFO_DETAILS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition.PREV_APP_PROBATION_OFFENDER_MANAGER;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition.PREV_APP_SUBMISSION_DETAILS;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
public class ShowPreviousApplicationSubmitHandlerTest {

    private ShowPreviousApplicationSubmitHandler showPreviousApplicationSubmitHandler;
    @Mock
    private Callback<BailCase> callback;
    @Mock
    private CaseDetails<BailCase> caseDetails;
    @Mock
    private BailCase bailCase;

    @BeforeEach
    void setUp() {
        showPreviousApplicationSubmitHandler = new ShowPreviousApplicationSubmitHandler();
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(bailCase);
        when(callback.getEvent()).thenReturn(Event.VIEW_PREVIOUS_APPLICATIONS);
    }

    @Test
    void should_remove_fields() {
        PreSubmitCallbackResponse<BailCase> response = showPreviousApplicationSubmitHandler
            .handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);
        assertNotNull(response);
        verify(bailCase, times(1)).remove(PREV_APP_DECISION_DETAILS_LABEL);
        verify(bailCase, times(1)).remove(PREV_APP_SUBMISSION_DETAILS);
        verify(bailCase, times(1)).remove(PREV_APP_HEARING_REQ_DETAILS);
        verify(bailCase, times(1)).remove(PREV_APP_APPLICANT_DOCS_DETAILS);
        verify(bailCase, times(1)).remove(PREV_APP_DIRECTION_DETAILS);
        verify(bailCase, times(1)).remove(PREV_APP_PERSONAL_INFO_DETAILS);
        verify(bailCase, times(1)).remove(PREV_APP_APPLICANT_INFO);
        verify(bailCase, times(1)).remove(PREV_APP_FINANCIAL_COND_COMMITMENT);
        verify(bailCase, times(1)).remove(PREV_APP_FINANCIAL_COND_SUPPORTER1);
        verify(bailCase, times(1)).remove(PREV_APP_FINANCIAL_COND_SUPPORTER2);
        verify(bailCase, times(1)).remove(PREV_APP_FINANCIAL_COND_SUPPORTER3);
        verify(bailCase, times(1)).remove(PREV_APP_FINANCIAL_COND_SUPPORTER4);
        verify(bailCase, times(1)).remove(PREV_APP_GROUNDS_FOR_BAIL);
        verify(bailCase, times(1)).remove(PREV_APP_LEGAL_REP_DETAILS);
        verify(bailCase, times(1)).remove(PREV_APP_PROBATION_OFFENDER_MANAGER);
    }

    @Test
    public void should_only_handle_valid_event_state() {
        for (Event event: Event.values()) {
            when(callback.getEvent()).thenReturn(event);
            for (PreSubmitCallbackStage stage: PreSubmitCallbackStage.values()) {
                boolean canHandle = showPreviousApplicationSubmitHandler.canHandle(stage, callback);
                if (stage.equals(PreSubmitCallbackStage.ABOUT_TO_SUBMIT) && event == Event.VIEW_PREVIOUS_APPLICATIONS) {
                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }
            reset(callback);
        }
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(
            () -> showPreviousApplicationSubmitHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> showPreviousApplicationSubmitHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(
            () -> showPreviousApplicationSubmitHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> showPreviousApplicationSubmitHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> showPreviousApplicationSubmitHandler
            .handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
