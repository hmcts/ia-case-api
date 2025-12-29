package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.postsubmit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;

import static junit.framework.TestCase.assertTrue;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
public class ChangeTribunalCentreConfirmationTest {

    @Mock
    private Callback<BailCase> callback;

    @Mock
    private CaseDetails<BailCase> caseDetails;

    @Mock
    private BailCase bailCase;

    private ChangeTribunalCentreConfirmation changeTribunalCentreConfirmation = new
        ChangeTribunalCentreConfirmation();

    @Test
    void should_return_confirmation() {
        when(callback.getEvent()).thenReturn(Event.CHANGE_TRIBUNAL_CENTRE);

        PostSubmitCallbackResponse callbackResponse =
            changeTribunalCentreConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        Assertions.assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        Assertions.assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(
            callbackResponse.getConfirmationHeader().get())
            .contains("# You've updated the designated Tribunal centre");

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains("### What happens next");

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains("The parties will be notified and the service will be updated.");
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> changeTribunalCentreConfirmation.handle(callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            boolean canHandle = changeTribunalCentreConfirmation.canHandle(callback);

            if (event == Event.CHANGE_TRIBUNAL_CENTRE) {

                assertTrue(canHandle);
            } else {
                assertFalse(canHandle);
            }

            reset(callback);
        }
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> changeTribunalCentreConfirmation.canHandle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> changeTribunalCentreConfirmation.handle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
