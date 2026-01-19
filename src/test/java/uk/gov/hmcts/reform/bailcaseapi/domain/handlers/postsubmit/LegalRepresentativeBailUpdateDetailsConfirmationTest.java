package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;


@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class LegalRepresentativeBailUpdateDetailsConfirmationTest {

    @Mock
    private Callback<BailCase> callback;

    private final uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit.LegalRepresentativeBailUpdateDetailsConfirmation legalRepresentativeBailUpdateDetailsConfirmation =
        new uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit.LegalRepresentativeBailUpdateDetailsConfirmation();

    @Test
    void should_return_confirmation() {

        when(callback.getEvent()).thenReturn(Event.UPDATE_BAIL_LEGAL_REP_DETAILS);

        PostSubmitCallbackResponse callbackResponse =
            legalRepresentativeBailUpdateDetailsConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(
            callbackResponse.getConfirmationHeader().get())
            .contains("You've updated the legal representative's details");

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains(
                "#### What happens next\n\nThe service will be updated. The new details will be used on "
                    + "all future correspondence and documents.<br />");

    }

    @Test
    void should_not_handle_invalid_event() {
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPLICATION);
        AssertionsForClassTypes.assertThatThrownBy(() -> legalRepresentativeBailUpdateDetailsConfirmation.handle(callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_handle_only_valid_event() {
        for (Event event: Event.values()) {
            when(callback.getEvent()).thenReturn(event);
            boolean canHandle = legalRepresentativeBailUpdateDetailsConfirmation.canHandle(callback);
            if (event.equals(Event.UPDATE_BAIL_LEGAL_REP_DETAILS)) {
                assertTrue(canHandle);
            } else {
                assertFalse(canHandle);
            }
            reset(callback);
        }
    }


    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> legalRepresentativeBailUpdateDetailsConfirmation.canHandle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> legalRepresentativeBailUpdateDetailsConfirmation.handle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
