package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.*;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;



@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class CustomiseHearingBundleConfirmationTest {

    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;

    private CustomiseHearingBundleConfirmation customiseHearingBundleConfirmation =
        new CustomiseHearingBundleConfirmation();

    @Test
    public void should_return_confirmation() {

        when(callback.getEvent()).thenReturn(Event.CUSTOMISE_HEARING_BUNDLE);

        PostSubmitCallbackResponse callbackResponse =
            customiseHearingBundleConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(
            callbackResponse.getConfirmationHeader().get(),
            containsString("The hearing bundle is being generated")
        );

        assertThat(
            callbackResponse.getConfirmationBody().get(),
            containsString("What happens next")
        );

        assertThat(
            callbackResponse.getConfirmationBody().get(),
            containsString(
                "You will soon be able to view the hearing bundle in the documents tab.</br>"
                + "You and the other parties will be notified when the hearing bundle is available.</br>"
                + "If the bundle fails to generate, you will be notified and will need to generate the bundle again."
            )
        );
    }

    @Test
    public void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> customiseHearingBundleConfirmation.handle(callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            boolean canHandle = customiseHearingBundleConfirmation.canHandle(callback);

            if (event == Event.CUSTOMISE_HEARING_BUNDLE) {

                assertTrue(canHandle);
            } else {
                assertFalse(canHandle);
            }

            reset(callback);
        }
    }

    @Test
    public void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> customiseHearingBundleConfirmation.canHandle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> customiseHearingBundleConfirmation.handle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }


}
