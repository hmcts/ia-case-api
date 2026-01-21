package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

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
class UploadBailHearingRecordingConfirmationTest {

    @Mock private Callback<BailCase> callback;

    private uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit.UploadBailHearingRecordingConfirmation uploadBailHearingRecordingConfirmation =
        new uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit.UploadBailHearingRecordingConfirmation();

    @Test
    void should_return_confirmation() {

        when(callback.getEvent()).thenReturn(Event.UPLOAD_HEARING_RECORDING);

        PostSubmitCallbackResponse callbackResponse =
            uploadBailHearingRecordingConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertTrue(
            callbackResponse.getConfirmationHeader().orElse("").contains("You’ve uploaded the hearing recording"),
            "Confirmation header should contain 'You’ve uploaded the hearing recording'"
        );

        assertTrue(
            callbackResponse.getConfirmationBody().orElse("").contains("This file is now available in the Documents tab and the Hearing and appointment tab."),
            "Confirmation body should contain 'This file is now available in the Documents tab and the Hearing and appointment tab.'"
        );
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> uploadBailHearingRecordingConfirmation.handle(callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            boolean canHandle = uploadBailHearingRecordingConfirmation.canHandle(callback);

            if (event == Event.UPLOAD_HEARING_RECORDING) {

                assertTrue(canHandle);
            } else {
                assertFalse(canHandle);
            }

            reset(callback);
        }
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> uploadBailHearingRecordingConfirmation.canHandle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> uploadBailHearingRecordingConfirmation.handle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
