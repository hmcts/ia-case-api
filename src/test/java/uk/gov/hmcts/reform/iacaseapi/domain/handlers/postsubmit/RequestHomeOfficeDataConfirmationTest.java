package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static junit.framework.TestCase.assertTrue;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class RequestHomeOfficeDataConfirmationTest {

    @Mock
    private Callback<AsylumCase> callback;

    private RequestHomeOfficeDataConfirmation requestHomeOfficeDataConfirmation;

    @BeforeEach
    void setUp() {

        requestHomeOfficeDataConfirmation = new RequestHomeOfficeDataConfirmation();
    }

    @Test
    void should_return_the_confirmation() {

        when(callback.getEvent()).thenReturn(Event.REQUEST_HOME_OFFICE_DATA);

        PostSubmitCallbackResponse callbackResponse =
                requestHomeOfficeDataConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertSame("# You have matched the appellant details", callbackResponse.getConfirmationHeader().get());
        assertSame("#### Do this next\n\nYou must review the appeal data and cross reference it with "
                        + "Home Office data in the validation tab. If the appeal looks valid, you must tell the "
                        + "respondent to supply their evidence.<br>", callbackResponse.getConfirmationBody().get());
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> requestHomeOfficeDataConfirmation.handle(callback))
                .hasMessage("Cannot handle callback")
                .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            boolean canHandle = requestHomeOfficeDataConfirmation.canHandle(callback);

            if (event == Event.REQUEST_HOME_OFFICE_DATA) {

                assertTrue(canHandle);
            } else {
                assertFalse(canHandle);
            }

            reset(callback);
        }
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> requestHomeOfficeDataConfirmation.canHandle(null))
                .hasMessage("callback must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> requestHomeOfficeDataConfirmation.handle(null))
                .hasMessage("callback must not be null")
                .isExactlyInstanceOf(NullPointerException.class);
    }
}
