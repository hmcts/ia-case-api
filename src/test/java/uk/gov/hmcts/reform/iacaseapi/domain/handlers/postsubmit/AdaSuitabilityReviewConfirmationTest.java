package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static junit.framework.TestCase.assertTrue;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.SUITABILITY_REVIEW_DECISION;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AdaSuitabilityReviewDecision;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;


@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class AdaSuitabilityReviewConfirmationTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;

    private AdaSuitabilityReviewConfirmation adaSuitabilityReviewConfirmation = new AdaSuitabilityReviewConfirmation();

    @Test
    void should_return_confirmation_for_suitable() {

        when(callback.getEvent()).thenReturn(Event.ADA_SUITABILITY_REVIEW);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(SUITABILITY_REVIEW_DECISION, AdaSuitabilityReviewDecision.class)).thenReturn(Optional.of(AdaSuitabilityReviewDecision.SUITABLE));

        PostSubmitCallbackResponse callbackResponse =
                adaSuitabilityReviewConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(
                callbackResponse.getConfirmationHeader().get())
                .contains("# Appeal determined suitable to continue as ADA");

        assertThat(
                callbackResponse.getConfirmationBody().get())
                .contains("All parties have been notified. The Accelerated Detained Appeal Suitability Decision is available to view in the documents tab.<br>");
    }

    @Test
    void should_return_confirmation_for_unsuitable() {

        long caseId = 1234;

        when(callback.getEvent()).thenReturn(Event.ADA_SUITABILITY_REVIEW);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(SUITABILITY_REVIEW_DECISION, AdaSuitabilityReviewDecision.class)).thenReturn(Optional.of(AdaSuitabilityReviewDecision.UNSUITABLE));
        when(caseDetails.getId()).thenReturn(caseId);

        PostSubmitCallbackResponse callbackResponse =
                adaSuitabilityReviewConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(
                callbackResponse.getConfirmationHeader().get())
                .contains("# Appeal determined unsuitable to continue as ADA");

        assertThat(
                callbackResponse.getConfirmationBody().get())
                .contains("All parties have been notified. The Accelerated Detained Appeal Suitability Decision is available to view in the documents tab.<br>");

        assertThat(
                callbackResponse.getConfirmationBody().get())
                .contains("You must [transfer this appeal out of the accelerated detained appeal process.]"
                        + "(/case/IA/Asylum/" + caseId + "/trigger/transferOutOfAda)");

    }

    @Test
    void handling_should_throw_if_decision_is_missing() {

        when(callback.getEvent()).thenReturn(Event.ADA_SUITABILITY_REVIEW);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(SUITABILITY_REVIEW_DECISION, AdaSuitabilityReviewDecision.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adaSuitabilityReviewConfirmation.handle(callback))
                .hasMessage("ADA suitability review decision unavailable.")
                .isExactlyInstanceOf(RequiredFieldMissingException.class);
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> adaSuitabilityReviewConfirmation.handle(callback))
                .hasMessage("Cannot handle callback")
                .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            boolean canHandle = adaSuitabilityReviewConfirmation.canHandle(callback);

            if (event == Event.ADA_SUITABILITY_REVIEW) {

                assertTrue(canHandle);
            } else {
                assertFalse(canHandle);
            }

            reset(callback);
        }
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> adaSuitabilityReviewConfirmation.canHandle(null))
                .hasMessage("callback must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> adaSuitabilityReviewConfirmation.handle(null))
                .hasMessage("callback must not be null")
                .isExactlyInstanceOf(NullPointerException.class);
    }


}
