package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class EditAppealAfterSubmitConfirmationTest {

    @Mock
    private Callback<AsylumCase> callback;

    private EditAppealAfterSubmitConfirmation editAppealAfterSubmitConfirmation =
        new EditAppealAfterSubmitConfirmation();

    @Test
    void should_return_confirmation() {

        when(callback.getEvent()).thenReturn(Event.EDIT_APPEAL_AFTER_SUBMIT);

        PostSubmitCallbackResponse callbackResponse =
            editAppealAfterSubmitConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertTrue(callbackResponse.getConfirmationHeader().get().contains("You've updated the application"));

        assertTrue(callbackResponse.getConfirmationBody().get().contains("What happens next"));

        assertTrue(callbackResponse.getConfirmationBody().get().contains("Both parties have been notified and the service will be updated."));

        assertTrue(callbackResponse.getConfirmationBody().get().contains("The new details will be used on all future correspondence and documents."));

    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {
        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        assertThatThrownBy(() -> editAppealAfterSubmitConfirmation.handle(callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {"EDIT_APPEAL_AFTER_SUBMIT", "EDIT_APPELLANT_PERSONAL_DATA"})
    void it_can_handle_callback(Event event) {
        when(callback.getEvent()).thenReturn(event);
        assertTrue(editAppealAfterSubmitConfirmation.canHandle(callback));
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {"EDIT_APPEAL_AFTER_SUBMIT", "EDIT_APPELLANT_PERSONAL_DATA"}, mode = EnumSource.Mode.EXCLUDE)
    void it_cannot_handle_callback_invalid_event(Event event) {
        when(callback.getEvent()).thenReturn(event);
        assertFalse(editAppealAfterSubmitConfirmation.canHandle(callback));
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> editAppealAfterSubmitConfirmation.canHandle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> editAppealAfterSubmitConfirmation.handle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
