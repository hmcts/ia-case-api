package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.JOURNEY_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.JourneyType.AIP;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.JourneyType.REP;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.JourneyType;

import java.util.Optional;


@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class ForceCaseProgressionToCaseUnderReviewConfirmationTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock private AsylumCase asylumCase;

    private ForceCaseProgressionToCaseUnderReviewConfirmation forceCaseProgressionToCaseUnderReviewConfirmation =
        new ForceCaseProgressionToCaseUnderReviewConfirmation();

    @Test
    void should_return_LR_confirmation() {

        when(callback.getEvent()).thenReturn(Event.FORCE_CASE_TO_CASE_UNDER_REVIEW);
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(REP));
        PostSubmitCallbackResponse callbackResponse =
            forceCaseProgressionToCaseUnderReviewConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(
            callbackResponse.getConfirmationHeader().get())
            .contains("# You have forced the case progression to case under review");

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains("Legal representative will be notified by email.");
    }

    @Test
    void should_return_AIP_confirmation() {

        when(callback.getEvent()).thenReturn(Event.FORCE_CASE_TO_CASE_UNDER_REVIEW);
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(AIP));
        PostSubmitCallbackResponse callbackResponse =
                forceCaseProgressionToCaseUnderReviewConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(
                callbackResponse.getConfirmationHeader().get())
                .contains("# You have forced the case progression to case under review");

        assertThat(
                callbackResponse.getConfirmationBody().get())
                .contains("Appellant will be notified by email.");
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> forceCaseProgressionToCaseUnderReviewConfirmation.handle(callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            boolean canHandle = forceCaseProgressionToCaseUnderReviewConfirmation.canHandle(callback);

            if (event == Event.FORCE_CASE_TO_CASE_UNDER_REVIEW) {

                assertTrue(canHandle);
            } else {
                assertFalse(canHandle);
            }

            reset(callback);
        }
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> forceCaseProgressionToCaseUnderReviewConfirmation.canHandle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> forceCaseProgressionToCaseUnderReviewConfirmation.handle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
