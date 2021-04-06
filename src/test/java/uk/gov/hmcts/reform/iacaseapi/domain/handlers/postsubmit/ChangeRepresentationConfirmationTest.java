package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClientResponseException;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.service.PostNotificationSender;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.CcdCaseAssignment;


@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class ChangeRepresentationConfirmationTest {

    @Mock private Callback<AsylumCase> callback;
    @Mock private CcdCaseAssignment ccdCaseAssignment;
    @Mock PostNotificationSender<AsylumCase> postNotificationSender;
    @Mock private CaseDetails<AsylumCase> caseDetails;

    public static final long CASE_ID = 1234567890L;
    private ChangeRepresentationConfirmation changeRepresentationConfirmation;

    @BeforeEach
    public void setUp() throws Exception {

        changeRepresentationConfirmation = new ChangeRepresentationConfirmation(
            ccdCaseAssignment,
            postNotificationSender
        );
    }

    @Test
    void should_apply_noc_for_remove_representation() {

        when(callback.getEvent()).thenReturn(Event.REMOVE_REPRESENTATION);

        PostSubmitCallbackResponse callbackResponse =
            changeRepresentationConfirmation.handle(callback);

        assertNotNull(callbackResponse);

        verify(ccdCaseAssignment, times(1)).applyNoc(callback);

        assertThat(
            callbackResponse.getConfirmationHeader().get())
            .contains("You have stopped representing this client");

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains(
                "We've sent you an email confirming you're no longer representing this client.\n"
                + "You have been removed from this case and no longer have access to it.\n\n"
                + "[View case list](/cases)"
            );
    }

    @Test
    void should_apply_noc_for_remove_legal_representative() {

        when(callback.getEvent()).thenReturn(Event.REMOVE_LEGAL_REPRESENTATIVE);

        PostSubmitCallbackResponse callbackResponse =
            changeRepresentationConfirmation.handle(callback);

        assertNotNull(callbackResponse);

        verify(ccdCaseAssignment, times(1)).applyNoc(callback);

        assertThat(
            callbackResponse.getConfirmationHeader().get())
            .contains("You have removed the legal representative from this appeal");

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains("All parties will be notified.");
    }

    @Test
    void should_apply_noc_for_change_legal_representative() {

        when(callback.getEvent()).thenReturn(Event.NOC_REQUEST);

        PostSubmitCallbackResponse callbackResponse =
            changeRepresentationConfirmation.handle(callback);

        assertNotNull(callbackResponse);

        verify(ccdCaseAssignment, times(1)).applyNoc(callback);

        assertThat(
            callbackResponse.getConfirmationHeader().get())
            .contains("You have started representing this client");

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains("All parties will be notified.");
    }

    @Test
    void should_handle_when_rest_exception_thrown_for_apply_noc() {

        when(callback.getEvent()).thenReturn(Event.REMOVE_REPRESENTATION);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getId()).thenReturn(CASE_ID);

        RestClientResponseException restClientResponseEx = mock(RestClientResponseException.class);
        doThrow(restClientResponseEx).when(ccdCaseAssignment).applyNoc(callback);

        PostSubmitCallbackResponse callbackResponse =
            changeRepresentationConfirmation.handle(callback);

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains("Something went wrong");
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> changeRepresentationConfirmation.handle(callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            boolean canHandle = changeRepresentationConfirmation.canHandle(callback);

            if (event == Event.REMOVE_REPRESENTATION
                || event == Event.REMOVE_LEGAL_REPRESENTATIVE
                || event == Event.NOC_REQUEST) {

                assertTrue(canHandle);
            } else {
                assertFalse(canHandle);
            }

            reset(callback);
        }
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> changeRepresentationConfirmation.canHandle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> changeRepresentationConfirmation.handle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
