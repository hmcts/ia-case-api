package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.REMOVE_LEGAL_REPRESENTATIVE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.REMOVE_REPRESENTATION;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class RemoveRepresentationHandlerTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;

    private RemoveRepresentationHandler removeRepresentationHandler;

    @BeforeEach
    public void setup() {
        removeRepresentationHandler = new RemoveRepresentationHandler();
    }

    @Test
    void should_not_handle_callback_if_stage_is_not_about_to_submit() {
        when(callback.getEvent()).thenReturn(REMOVE_REPRESENTATION);
        boolean canHandle = removeRepresentationHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_START, callback);
        assertFalse(canHandle);
    }

    @Test
    void should_not_handle_callback_if_event_is_not_remove_representation_or_remove_legal_representative() {
        when(callback.getEvent()).thenReturn(Event.ADA_SUITABILITY_REVIEW);
        boolean canHandle = removeRepresentationHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);
        assertFalse(canHandle);
    }

    @Test
    void should_handle_callback_if_event_is_remove_representation_or_remove_legal_representative() {
        when(callback.getEvent()).thenReturn(REMOVE_REPRESENTATION);
        boolean canHandle = removeRepresentationHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);
        assertTrue(canHandle);
    }

    @Test
    void should_clear_legal_rep_details_when_handled_for_event_remove_representation() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(REMOVE_REPRESENTATION);
        removeRepresentationHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        verify(asylumCase).clear(LEGAL_REP_COMPANY);
        verify(asylumCase).clear(LEGAL_REP_COMPANY_ADDRESS);
        verify(asylumCase).clear(LEGAL_REP_NAME);
        verify(asylumCase).clear(LEGAL_REPRESENTATIVE_NAME);
        verify(asylumCase).clear(LEGAL_REP_REFERENCE_NUMBER);
        verify(asylumCase).clear(LEGAL_REP_MOBILE_PHONE_NUMBER);
        verify(asylumCase).clear(LEGAL_REPRESENTATIVE_EMAIL_ADDRESS);
    }

    @Test
    void should_clear_legal_rep_details_when_handled_for_event_remove_legal_representative() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(REMOVE_LEGAL_REPRESENTATIVE);
        removeRepresentationHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        verify(asylumCase).clear(LEGAL_REP_COMPANY);
        verify(asylumCase).clear(LEGAL_REP_COMPANY_ADDRESS);
        verify(asylumCase).clear(LEGAL_REP_NAME);
        verify(asylumCase).clear(LEGAL_REPRESENTATIVE_NAME);
        verify(asylumCase).clear(LEGAL_REP_REFERENCE_NUMBER);
        verify(asylumCase).clear(LEGAL_REP_MOBILE_PHONE_NUMBER);
        verify(asylumCase).clear(LEGAL_REPRESENTATIVE_EMAIL_ADDRESS);
    }
}