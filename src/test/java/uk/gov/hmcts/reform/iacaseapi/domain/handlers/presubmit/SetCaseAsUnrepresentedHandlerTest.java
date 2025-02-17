package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.util.Optional;
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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class SetCaseAsUnrepresentedHandlerTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;

    private SetCaseAsUnrepresentedHandler setCaseAsUnrepresentedHandler;

    @BeforeEach
    public void setup() {
        setCaseAsUnrepresentedHandler = new SetCaseAsUnrepresentedHandler();
    }

    @Test
    void set_case_as_unrepresented_using_stop_representing_a_client_event() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.REMOVE_REPRESENTATION);
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(APPEAL_OUT_OF_COUNTRY, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            setCaseAsUnrepresentedHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase).write(IS_ADMIN, YesOrNo.YES);
        verify(asylumCase).clear(LEGAL_REP_COMPANY);
        verify(asylumCase).clear(LEGAL_REP_COMPANY_ADDRESS);
        verify(asylumCase).clear(LEGAL_REP_NAME);
        verify(asylumCase).clear(LEGAL_REPRESENTATIVE_NAME);
        verify(asylumCase).clear(LEGAL_REP_REFERENCE_NUMBER);
    }

    @Test
    void set_case_as_unrepresented_using_remove_legal_rep_event() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.REMOVE_LEGAL_REPRESENTATIVE);
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(APPEAL_OUT_OF_COUNTRY, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            setCaseAsUnrepresentedHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase).write(IS_ADMIN, YesOrNo.YES);
        verify(asylumCase).clear(LEGAL_REP_COMPANY);
        verify(asylumCase).clear(LEGAL_REP_COMPANY_ADDRESS);
        verify(asylumCase).clear(LEGAL_REP_NAME);
        verify(asylumCase).clear(LEGAL_REPRESENTATIVE_NAME);
        verify(asylumCase).clear(LEGAL_REP_REFERENCE_NUMBER);
    }

    @Test
    void should_not_set_case_as_unrepresented_using_remove_legal_rep_event_out_of_country() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.REMOVE_LEGAL_REPRESENTATIVE);
        when(asylumCase.read(APPEAL_OUT_OF_COUNTRY, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            setCaseAsUnrepresentedHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(0)).write(IS_ADMIN, YesOrNo.YES);
        verify(asylumCase, times(0)).clear(LEGAL_REP_COMPANY);
        verify(asylumCase, times(0)).clear(LEGAL_REP_COMPANY_ADDRESS);
        verify(asylumCase, times(0)).clear(LEGAL_REP_NAME);
        verify(asylumCase, times(0)).clear(LEGAL_REPRESENTATIVE_NAME);
        verify(asylumCase, times(0)).clear(LEGAL_REP_REFERENCE_NUMBER);
        verify(asylumCase).write(HAS_ADDED_LEGAL_REP_DETAILS, YesOrNo.NO);
    }

    @Test
    void should_not_set_case_as_unrepresented_using_stop_representing_a_client_not_detained() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.REMOVE_REPRESENTATION);
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(APPEAL_OUT_OF_COUNTRY, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            setCaseAsUnrepresentedHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(0)).write(IS_ADMIN, YesOrNo.YES);
        verify(asylumCase, times(0)).clear(LEGAL_REP_COMPANY);
        verify(asylumCase, times(0)).clear(LEGAL_REP_COMPANY_ADDRESS);
        verify(asylumCase, times(0)).clear(LEGAL_REP_NAME);
        verify(asylumCase, times(0)).clear(LEGAL_REPRESENTATIVE_NAME);
        verify(asylumCase, times(0)).clear(LEGAL_REP_REFERENCE_NUMBER);
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        assertThatThrownBy(
            () -> setCaseAsUnrepresentedHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {
        for (Event event : Event.values()) {
            when(callback.getEvent()).thenReturn(event);
            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {
                boolean canHandle = setCaseAsUnrepresentedHandler.canHandle(callbackStage, callback);
                if (callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && (event == Event.REMOVE_REPRESENTATION
                        || event == Event.REMOVE_LEGAL_REPRESENTATIVE)) {
                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }
        }
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> setCaseAsUnrepresentedHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(
            () -> setCaseAsUnrepresentedHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> setCaseAsUnrepresentedHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> setCaseAsUnrepresentedHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

}