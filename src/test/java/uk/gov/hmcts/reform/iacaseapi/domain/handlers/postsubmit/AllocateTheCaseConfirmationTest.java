package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
public class AllocateTheCaseConfirmationTest {

    public static final long CASE_ID = 1234567890L;
    @Mock
    private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;

    private AllocateTheCaseConfirmation allocateTheCaseConfirmation =
            new AllocateTheCaseConfirmation();

    @Test
    public void should_return_confirmation() {
        when(callback.getEvent()).thenReturn(Event.ALLOCATE_THE_CASE);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getId()).thenReturn(CASE_ID);

        PostSubmitCallbackResponse callbackResponse = allocateTheCaseConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(
                callbackResponse.getConfirmationHeader().get(),
                containsString("# You have allocated the case")
        );

        assertThat(
                callbackResponse.getConfirmationBody().get(),
                containsString("#### What happens next\n\n"
                        + "The tasks for this case will now appear in your task list. Should you wish, you can write "
                        + "[a case note](/case/IA/Asylum/" + CASE_ID + "/trigger/addCaseNote).")
        );
    }

    @Test
    public void handling_should_throw_if_cannot_actually_handle() {
        when(callback.getEvent()).thenReturn(Event.BUILD_CASE);

        assertThatThrownBy(() -> allocateTheCaseConfirmation.handle(callback))
                .hasMessage("Cannot handle callback")
                .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void it_can_handle_callback() {
        for (Event event : Event.values()) {
            when(callback.getEvent()).thenReturn(event);
            boolean canHandle = allocateTheCaseConfirmation.canHandle(callback);
            if (event == Event.ALLOCATE_THE_CASE) {
                assertTrue(canHandle);
            } else {
                assertFalse(canHandle);
            }

            reset(callback);
        }
    }

    @Test
    public void should_not_allow_null_arguments() {
        assertThatThrownBy(() -> allocateTheCaseConfirmation.canHandle(null))
                .hasMessage("callback must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> allocateTheCaseConfirmation.handle(null))
                .hasMessage("callback must not be null")
                .isExactlyInstanceOf(NullPointerException.class);
    }
}

