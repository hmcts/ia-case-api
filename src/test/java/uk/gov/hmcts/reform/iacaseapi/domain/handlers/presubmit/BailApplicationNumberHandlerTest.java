package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.BAIL_APPLICATION_NUMBER;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HAS_PENDING_BAIL_APPLICATIONS;

import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.BailApplicationStatus;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
public class BailApplicationNumberHandlerTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;

    private final BailApplicationNumberHandler bailApplicationNumberHandler = new BailApplicationNumberHandler();

    @Test
    void should_infer_bail_case_from_application_number() {
        String bailCaseReferenceNumber = "1111-2222-3333-4444";

        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getPageId()).thenReturn(HAS_PENDING_BAIL_APPLICATIONS.value());
        when(asylumCase.read(HAS_PENDING_BAIL_APPLICATIONS, BailApplicationStatus.class))
            .thenReturn(Optional.of(BailApplicationStatus.YES));
        when(asylumCase.read(BAIL_APPLICATION_NUMBER, String.class))
            .thenReturn(Optional.of(bailCaseReferenceNumber));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            bailApplicationNumberHandler.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertThat(callbackResponse.getErrors()).hasSize(0);
    }

    @Test
    void should_infer_aria_case_from_application_number() {
        String ariaCaseReferenceNumber = "Jk/09876";

        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getPageId()).thenReturn(HAS_PENDING_BAIL_APPLICATIONS.value());
        when(asylumCase.read(HAS_PENDING_BAIL_APPLICATIONS, BailApplicationStatus.class))
            .thenReturn(Optional.of(BailApplicationStatus.YES));
        when(asylumCase.read(BAIL_APPLICATION_NUMBER, String.class))
            .thenReturn(Optional.of(ariaCaseReferenceNumber));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            bailApplicationNumberHandler.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertThat(callbackResponse.getErrors()).hasSize(0);
    }

    @Test
    void should_add_error_if_number_format_wrong() {
        String wrongCaseReferenceNumber = "000jss";

        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getPageId()).thenReturn(HAS_PENDING_BAIL_APPLICATIONS.value());
        when(asylumCase.read(HAS_PENDING_BAIL_APPLICATIONS, BailApplicationStatus.class))
            .thenReturn(Optional.of(BailApplicationStatus.YES));
        when(asylumCase.read(BAIL_APPLICATION_NUMBER, String.class))
            .thenReturn(Optional.of(wrongCaseReferenceNumber));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            bailApplicationNumberHandler.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        String expectedError = "Invalid bail number provided. The bail number must be either 16 digits with dashes "
            + "(e.g. 1111-2222-3333-4444) or 8 characters long (e.g. HW/12345)";

        final Set<String> errors = callbackResponse.getErrors();
        assertThat(errors).hasSize(1);
        assertThat(errors).contains(expectedError);
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> bailApplicationNumberHandler.handle(PreSubmitCallbackStage.MID_EVENT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }


    @Test
    void it_can_handle_callback_for_all_events() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);
            when(callback.getPageId()).thenReturn(HAS_PENDING_BAIL_APPLICATIONS.value());

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = bailApplicationNumberHandler.canHandle(callbackStage, callback);

                if (callbackStage == PreSubmitCallbackStage.MID_EVENT
                    && (callback.getEvent() == Event.START_APPEAL
                    || callback.getEvent() == Event.EDIT_APPEAL
                    || callback.getEvent() == Event.EDIT_APPEAL_AFTER_SUBMIT
                    || callback.getEvent() == Event.MARK_APPEAL_AS_DETAINED)
                    && callback.getPageId().equals(HAS_PENDING_BAIL_APPLICATIONS.value())) {

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

        assertThatThrownBy(() -> bailApplicationNumberHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> bailApplicationNumberHandler
            .canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> bailApplicationNumberHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> bailApplicationNumberHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
