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
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.CcdCaseAssignment;


@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class RemoveRepresentationConfirmationTest {

    @Mock private Callback<AsylumCase> callback;
    @Mock private CcdCaseAssignment ccdCaseAssignment;
    @Mock private RestTemplate restTemplate;
    @Mock private CaseDetails<AsylumCase> caseDetails;

    public static final long CASE_ID = 1234567890L;
    private final String aacUrl = "some-aac-host";
    private final String applyNocAssignmentsApiPath = "some-path";
    private RemoveRepresentationConfirmation removeRepresentationConfirmation;

    @BeforeEach
    public void setUp() throws Exception {

        removeRepresentationConfirmation = new RemoveRepresentationConfirmation(
            ccdCaseAssignment
        );
    }

    @Test
    void should_apply_noc_and_return_confirmation() {

        when(callback.getEvent()).thenReturn(Event.REMOVE_REPRESENTATION);

        PostSubmitCallbackResponse callbackResponse =
            removeRepresentationConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        verify(ccdCaseAssignment, times(1)).applyNoc(callback);

        assertThat(
            callbackResponse.getConfirmationHeader().get())
            .contains("You have stopped representing this client");

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains("You have been removed from this case and no longer have access to it.");
    }

    @Test
    void should_handle_when_rest_exception_thrown_for_apply_noc() {

        when(callback.getEvent()).thenReturn(Event.REMOVE_REPRESENTATION);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getId()).thenReturn(CASE_ID);

        RestClientResponseException restClientResponseEx = mock(RestClientResponseException.class);
        doThrow(restClientResponseEx).when(ccdCaseAssignment).applyNoc(callback);

        PostSubmitCallbackResponse callbackResponse =
            removeRepresentationConfirmation.handle(callback);

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains("A problem has occurred");
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> removeRepresentationConfirmation.handle(callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            boolean canHandle = removeRepresentationConfirmation.canHandle(callback);

            if (event == Event.REMOVE_REPRESENTATION) {

                assertTrue(canHandle);
            } else {
                assertFalse(canHandle);
            }

            reset(callback);
        }
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> removeRepresentationConfirmation.canHandle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> removeRepresentationConfirmation.handle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
