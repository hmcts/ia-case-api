package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.spy;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit.AutoRequestHearingConfirmation.WHAT_HAPPENS_NEXT_LABEL;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;

public class AutoRequestHearingConfirmationTest {

    private static final String EVENT = "This event";

    AutoRequestHearingConfirmation autoRequestHearingConfirmation;

    @BeforeEach
    void setup() {
        autoRequestHearingConfirmation = spy(AutoRequestHearingConfirmation.class);
    }

    @Test
    void should_build_auto_hearing_request_confirmation_response() {
        PostSubmitCallbackResponse postSubmitCallbackResponse = autoRequestHearingConfirmation
            .buildAutoHearingRequestConfirmationResponse(1L, false, true, EVENT);

        assertEquals("# Hearing listed", postSubmitCallbackResponse.getConfirmationHeader().orElse(""));
        assertEquals(WHAT_HAPPENS_NEXT_LABEL
                     + "The hearing request has been created and is visible on the [Hearings tab]"
                     + "(/cases/case-details/1/hearings)", postSubmitCallbackResponse.getConfirmationBody().orElse(""));
    }

    @Test
    void should_build_confirmation_response_when_no_panel_and_successful_call() {
        PostSubmitCallbackResponse postSubmitCallbackResponse = autoRequestHearingConfirmation
            .buildAutoHearingRequestConfirmationResponse(1L, false, true, EVENT);

        assertEquals("# Hearing listed", postSubmitCallbackResponse.getConfirmationHeader().orElse(""));
        assertEquals(WHAT_HAPPENS_NEXT_LABEL
                     + "The hearing request has been created and is visible on the [Hearings tab]"
                     + "(/cases/case-details/1/hearings)",
            postSubmitCallbackResponse.getConfirmationBody().orElse(""));
    }

    @Test
    void should_build_confirmation_response_with_panel() {
        PostSubmitCallbackResponse postSubmitCallbackResponse = autoRequestHearingConfirmation
            .buildAutoHearingRequestConfirmationResponse(1L, true, true, EVENT);

        assertEquals("# This event complete", postSubmitCallbackResponse.getConfirmationHeader().orElse(""));
        assertEquals(WHAT_HAPPENS_NEXT_LABEL
                     + "The listing team will now list the case. All parties will be notified when "
                     + "the Hearing Notice is available to view",
            postSubmitCallbackResponse.getConfirmationBody().orElse(""));
    }

    @Test
    void should_build_confirmation_response_with_no_panel_and_unsuccessful_call() {
        PostSubmitCallbackResponse postSubmitCallbackResponse = autoRequestHearingConfirmation
            .buildAutoHearingRequestConfirmationResponse(1L, false, false, EVENT);

        assertEquals("", postSubmitCallbackResponse.getConfirmationHeader().orElse(""));
        assertEquals("![Hearing could not be listed](https://raw.githubusercontent.com/hmcts/"
                     + "ia-appeal-frontend/master/app/assets/images/hearingCouldNotBeListed.png)"
                     + "\n\n"
                     + WHAT_HAPPENS_NEXT_LABEL
                     + "The hearing could not be auto-requested. Please manually request the "
                     + "hearing via the [Hearings tab](/cases/case-details/1/hearings)",
            postSubmitCallbackResponse.getConfirmationBody().orElse(""));
    }
}
