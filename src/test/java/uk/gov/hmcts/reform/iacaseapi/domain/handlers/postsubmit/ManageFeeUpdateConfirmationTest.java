package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.*;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class ManageFeeUpdateConfirmationTest {

    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseCaseDetails;

    private ManageFeeUpdateConfirmation manageFeeUpdateConfirmation;

    @BeforeEach
    void setUp() {

        manageFeeUpdateConfirmation = new ManageFeeUpdateConfirmation();
    }

    @org.junit.Test
    public void should_return_confirmation() {

        when(callback.getCaseDetails()).thenReturn(caseCaseDetails);
        when(callback.getEvent()).thenReturn(Event.MANAGE_FEE_UPDATE);

        PostSubmitCallbackResponse callbackResponse =
            manageFeeUpdateConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(
            callbackResponse.getConfirmationHeader().get(),
            containsString("# You have recorded a fee update")
        );

        assertThat(
            callbackResponse.getConfirmationBody().get(),
            containsString("#### What happens next\n\n"
                           + "The appropriate team will be notified to review the fee update and take the next steps.")
        );
    }


    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> manageFeeUpdateConfirmation.handle(callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            boolean canHandle = manageFeeUpdateConfirmation.canHandle(callback);

            if (event == Event.MANAGE_FEE_UPDATE) {

                assertTrue(canHandle);
            } else {
                assertFalse(canHandle);
            }

            reset(callback);
        }
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> manageFeeUpdateConfirmation.canHandle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> manageFeeUpdateConfirmation.handle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }


}
