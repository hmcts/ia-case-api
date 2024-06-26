package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.UserDetailsHelper;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserRoleLabel;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class GenerateServiceRequestMakeAPaymentConfirmationTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private UserDetails userDetails;
    @Mock
    private UserDetailsHelper userDetailsHelper;
    private GenerateServiceRequestMakeAPaymentConfirmation generateServiceRequestMakeAPaymentConfirmation;

    @BeforeEach
    public void setUp() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.GENERATE_SERVICE_REQUEST);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        generateServiceRequestMakeAPaymentConfirmation = new GenerateServiceRequestMakeAPaymentConfirmation(userDetails, userDetailsHelper);
    }

    @Test
    void should_return_legal_rep_confirmation_page() {
        when(userDetailsHelper.getLoggedInUserRoleLabel(userDetails)).thenReturn(UserRoleLabel.LEGAL_REPRESENTATIVE);
        PostSubmitCallbackResponse callbackResponse =
            generateServiceRequestMakeAPaymentConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertThat(callbackResponse.getConfirmationHeader()).isPresent();
        assertThat(callbackResponse.getConfirmationBody()).isPresent();
        assertThat(callbackResponse.getConfirmationHeader()).contains("# You have created a service request");
        assertThat(callbackResponse.getConfirmationBody())
            .contains("### What happens next\n\n"
                + "You can now pay for this appeal in the 'Service Request' tab on the case details screen.\n\n"
                + "[Service requests](cases/case-details/"
                + callback.getCaseDetails().getId() + "#Service%20Request)\n\n");
    }

    @Test
    void should_return_admin_confirmation_page() {
        when(userDetailsHelper.getLoggedInUserRoleLabel(userDetails)).thenReturn(UserRoleLabel.ADMIN_OFFICER);
        PostSubmitCallbackResponse callbackResponse =
            generateServiceRequestMakeAPaymentConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertThat(callbackResponse.getConfirmationHeader()).isPresent();
        assertThat(callbackResponse.getConfirmationBody()).isPresent();
        assertThat(callbackResponse.getConfirmationHeader()).contains("# You have created a service request");
        assertThat(callbackResponse.getConfirmationBody())
            .contains("### What happens next\n\n"
                + "The legal representative can now pay for this appeal in the 'Service Request' tab on the case details screen.\n\n");
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> generateServiceRequestMakeAPaymentConfirmation.canHandle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> generateServiceRequestMakeAPaymentConfirmation.handle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            boolean canHandle = generateServiceRequestMakeAPaymentConfirmation.canHandle(callback);

            if (event == Event.GENERATE_SERVICE_REQUEST) {

                assertTrue(canHandle);
            } else {
                assertFalse(canHandle);
            }

            reset(callback);
        }
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);

        assertThatThrownBy(() -> generateServiceRequestMakeAPaymentConfirmation.handle(callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

}