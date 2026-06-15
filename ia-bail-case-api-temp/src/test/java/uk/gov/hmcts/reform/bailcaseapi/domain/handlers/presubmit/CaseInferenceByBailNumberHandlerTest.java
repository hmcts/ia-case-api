package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.HAS_PREVIOUS_BAIL_APPLICATION;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.PREVIOUS_BAIL_APPLICATION_NUMBER;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.PREVIOUS_APPLICATION_DONE_VIA_ARIA;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.PREVIOUS_APPLICATION_DONE_VIA_CCD;

import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.YesOrNo;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
public class CaseInferenceByBailNumberHandlerTest {

    @Mock
    private Callback<BailCase> callback;
    @Mock
    private CaseDetails<BailCase> caseDetails;
    @Mock
    private BailCase bailCase;

    private CaseInferenceByBailNumberHandler caseInferenceByBailNumberHandler = new CaseInferenceByBailNumberHandler();

    @Test
    void should_infer_bail_case_from_application_number() {
        String bailCaseReferenceNumber = "1111222233334444";

        when(callback.getEvent()).thenReturn(Event.START_APPLICATION);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(bailCase);
        when(callback.getPageId()).thenReturn(HAS_PREVIOUS_BAIL_APPLICATION.value());
        when(bailCase.read(HAS_PREVIOUS_BAIL_APPLICATION, String.class))
            .thenReturn(Optional.of("Yes"));
        when(bailCase.read(PREVIOUS_BAIL_APPLICATION_NUMBER, String.class))
            .thenReturn(Optional.of(bailCaseReferenceNumber));

        PreSubmitCallbackResponse<BailCase> callbackResponse =
            caseInferenceByBailNumberHandler.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        verify(bailCase, times(1)).write(PREVIOUS_APPLICATION_DONE_VIA_CCD, YesOrNo.YES);
        verify(bailCase, times(1)).write(PREVIOUS_APPLICATION_DONE_VIA_ARIA, YesOrNo.NO);

        assertThat(callbackResponse.getErrors()).hasSize(0);
    }

    @Test
    void should_infer_aria_case_from_application_number() {
        String ariaCaseReferenceNumber = "Jk/09876";

        when(callback.getEvent()).thenReturn(Event.START_APPLICATION);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(bailCase);
        when(callback.getPageId()).thenReturn(HAS_PREVIOUS_BAIL_APPLICATION.value());

        when(bailCase.read(HAS_PREVIOUS_BAIL_APPLICATION, String.class))
            .thenReturn(Optional.of("Yes"));
        when(bailCase.read(PREVIOUS_BAIL_APPLICATION_NUMBER, String.class))
            .thenReturn(Optional.of(ariaCaseReferenceNumber));

        PreSubmitCallbackResponse<BailCase> callbackResponse =
            caseInferenceByBailNumberHandler.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        verify(bailCase, times(1)).write(PREVIOUS_APPLICATION_DONE_VIA_CCD, YesOrNo.NO);
        verify(bailCase, times(1)).write(PREVIOUS_APPLICATION_DONE_VIA_ARIA, YesOrNo.YES);

        assertThat(callbackResponse.getErrors()).hasSize(0);
    }

    @Test
    void should_add_error_if_number_format_wrong() {
        String wrongCaseReferenceNumber = "000jss";

        when(callback.getEvent()).thenReturn(Event.START_APPLICATION);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(bailCase);
        when(callback.getPageId()).thenReturn(HAS_PREVIOUS_BAIL_APPLICATION.value());
        when(bailCase.read(HAS_PREVIOUS_BAIL_APPLICATION, String.class))
            .thenReturn(Optional.of("Yes"));
        when(bailCase.read(PREVIOUS_BAIL_APPLICATION_NUMBER, String.class))
            .thenReturn(Optional.of(wrongCaseReferenceNumber));

        PreSubmitCallbackResponse<BailCase> callbackResponse =
            caseInferenceByBailNumberHandler.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        verify(bailCase, never()).write(PREVIOUS_APPLICATION_DONE_VIA_CCD, YesOrNo.NO);
        verify(bailCase,  never()).write(PREVIOUS_APPLICATION_DONE_VIA_ARIA, YesOrNo.YES);

        String expectedError = "Invalid bail number provided. The bail number must be either 16 digits long "
                               + "(e.g. 1111222233334444) or 8 characters long (e.g. HW/12345)";

        final Set<String> errors = callbackResponse.getErrors();
        assertThat(errors).hasSize(1);
        assertThat(errors).contains(expectedError);
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> caseInferenceByBailNumberHandler.handle(PreSubmitCallbackStage.MID_EVENT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_set_case_not_done_via_ccd_or_aria_if_applicant_has_no_previous_case() {

        when(callback.getEvent()).thenReturn(Event.START_APPLICATION);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(bailCase);
        when(callback.getPageId()).thenReturn(HAS_PREVIOUS_BAIL_APPLICATION.value());

        caseInferenceByBailNumberHandler.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        verify(bailCase, times(1)).write(PREVIOUS_APPLICATION_DONE_VIA_CCD, YesOrNo.NO);
        verify(bailCase,  times(1)).write(PREVIOUS_APPLICATION_DONE_VIA_ARIA, YesOrNo.NO);
    }

    @Test
    void it_can_handle_callback_for_all_events() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);
            when(callback.getPageId()).thenReturn(HAS_PREVIOUS_BAIL_APPLICATION.value());

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = caseInferenceByBailNumberHandler.canHandle(callbackStage, callback);

                if (callbackStage == PreSubmitCallbackStage.MID_EVENT
                    && (callback.getEvent() == Event.START_APPLICATION
                        || callback.getEvent() == Event.EDIT_BAIL_APPLICATION
                        || callback.getEvent() == Event.EDIT_BAIL_APPLICATION_AFTER_SUBMIT)
                    && callback.getPageId().equals(HAS_PREVIOUS_BAIL_APPLICATION.value())) {

                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }

            reset(callback);
        }
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> caseInferenceByBailNumberHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> caseInferenceByBailNumberHandler
            .canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> caseInferenceByBailNumberHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> caseInferenceByBailNumberHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
