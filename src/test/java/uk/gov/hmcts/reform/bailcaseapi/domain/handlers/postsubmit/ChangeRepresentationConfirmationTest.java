package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.postsubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClientResponseException;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.bailcaseapi.domain.service.PostNotificationSender;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.service.CcdDataService;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.clients.CcdCaseAssignment;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.security.idam.IdentityManagerResponseException;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class ChangeRepresentationConfirmationTest {

    @Mock private Callback<BailCase> callback;
    @Mock private CcdCaseAssignment ccdCaseAssignment;

    @Mock
    PostNotificationSender<BailCase> postNotificationSender;

    @Mock private CcdDataService ccdDataService;

    @Mock private CaseDetails<BailCase> caseDetails;
    @Mock private BailCase bailCase;

    public static final long CASE_ID = 1234567890L;
    public static final String BAILCASE_REFERENCE_NUMBER = "1111222233334444";
    private ChangeRepresentationConfirmation changeRepresentationConfirmation;


    @BeforeEach
    public void setUp() throws Exception {

        changeRepresentationConfirmation = new ChangeRepresentationConfirmation(
            ccdCaseAssignment,
            postNotificationSender,
            ccdDataService
        );
    }

    @Test
    void should_apply_noc_for_change_legal_representative() {

        when(callback.getEvent()).thenReturn(Event.NOC_REQUEST);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(bailCase);
        when(bailCase.read(BailCaseFieldDefinition.BAIL_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.of(BAILCASE_REFERENCE_NUMBER));

        PostSubmitCallbackResponse callbackResponse =
            changeRepresentationConfirmation.handle(callback);

        assertNotNull(callbackResponse);

        verify(ccdCaseAssignment, times(1)).applyNoc(callback);

        assertThat(
            callbackResponse.getConfirmationHeader().get())
            .contains("# You're now representing a client on case 1111222233334444");
    }

    @Test
    void should_handle_removal_of_legal_representative() {

        when(callback.getEvent()).thenReturn(Event.REMOVE_BAIL_LEGAL_REPRESENTATIVE);

        PostSubmitCallbackResponse callbackResponse =
            changeRepresentationConfirmation.handle(callback);

        assertNotNull(callbackResponse);

        verify(ccdCaseAssignment, times(1)).applyNoc(callback);
        verify(ccdDataService, times(1)).clearLegalRepDetails(callback);

        assertThat(
            callbackResponse.getConfirmationHeader().get())
            .contains("# You have removed the legal representative from this case");

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains("### What happens next\n\n"
                      + "This legal representative will no longer have access to this case.");
    }

    @Test
    void should_handle_stop_legal_representing_by_legal_rep() {

        when(callback.getEvent()).thenReturn(Event.STOP_LEGAL_REPRESENTING);

        PostSubmitCallbackResponse callbackResponse =
            changeRepresentationConfirmation.handle(callback);

        assertNotNull(callbackResponse);

        verify(ccdCaseAssignment, times(1)).applyNoc(callback);
        verify(postNotificationSender, times(1)).send(callback);
        verify(ccdDataService, times(1)).clearLegalRepDetails(callback);

        assertThat(
            callbackResponse.getConfirmationHeader().get())
            .contains("# You have stopped representing this client");

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains("### What happens next\n\n"
                          + "We've sent you an email confirming you're no longer representing this client. "
                          + "You have been "
                          + "removed from this case and no longer have access to it.\n\n\n\n"
                          + "[View case list](/cases)");
    }

    @Test
    void should_handle_when_rest_exception_thrown_for_apply_noc() {

        when(callback.getEvent()).thenReturn(Event.NOC_REQUEST);
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
    void should_handle_when_exception_thrown_by_ccd_service() {

        when(callback.getEvent()).thenReturn(Event.REMOVE_BAIL_LEGAL_REPRESENTATIVE);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getId()).thenReturn(CASE_ID);

        IdentityManagerResponseException identityManagerResponseEx = mock(IdentityManagerResponseException.class);
        doThrow(identityManagerResponseEx).when(ccdDataService).clearLegalRepDetails(callback);

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

            if (event == Event.NOC_REQUEST
                || event == Event.REMOVE_BAIL_LEGAL_REPRESENTATIVE
                || event == Event.STOP_LEGAL_REPRESENTING) {

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

    @Test
    void should_send_notification() {
        reset(callback);
        when(callback.getEvent()).thenReturn(Event.NOC_REQUEST);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(bailCase);
        when(bailCase.read(BailCaseFieldDefinition.BAIL_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.of(BAILCASE_REFERENCE_NUMBER));

        PostSubmitCallbackResponse callbackResponse =
            changeRepresentationConfirmation.handle(callback);
        verify(ccdCaseAssignment, times(1)).applyNoc(callback);
        // In order to distinguish from asylum in notification-api,
        // we are changing the event name from NOC_REQUEST to NOC_REQUEST_BAIL
        // And NotificationSender uses the new callback with new event name.
        verify(postNotificationSender, times(1)).send(any(Callback.class));

        assertNotNull(callbackResponse);

        assertThat(
            callbackResponse.getConfirmationHeader().get())
            .contains("# You're now representing a client on case 1111222233334444");
    }
}

