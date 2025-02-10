package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HAS_SERVICE_REQUEST_ALREADY;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.PAYMENT_STATUS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.REFUND_CONFIRMATION_APPLIED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.SERVICE_REQUEST_REFERENCE;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.PaymentStatus;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
@MockitoSettings(strictness = Strictness.LENIENT)
class GenerateServiceRequestPreparerTest {

    @Mock
    private Callback<AsylumCase> callback;

    @Mock
    private CaseDetails<AsylumCase> caseDetails;

    @Mock
    private AsylumCase asylumCase;

    private GenerateServiceRequestPreparer serviceRequestPreparer;

    @BeforeEach
    public void setUp() {
        serviceRequestPreparer = new GenerateServiceRequestPreparer(true);
    }

    @Test
    void should_return_no_errors_for_new_case_with_no_existing_service_request() {

        when(callback.getEvent()).thenReturn(Event.GENERATE_SERVICE_REQUEST);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);


        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            serviceRequestPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getErrors().isEmpty());
    }

    @Test
    void should_return_no_errors_for_case_with_no_existing_service_request() {

        when(callback.getEvent()).thenReturn(Event.GENERATE_SERVICE_REQUEST);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(SERVICE_REQUEST_REFERENCE, String.class)).thenReturn(Optional.of(""));
        when(asylumCase.read(HAS_SERVICE_REQUEST_ALREADY, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(PAYMENT_STATUS, PaymentStatus.class)).thenReturn(Optional.of(PaymentStatus.PAYMENT_PENDING));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            serviceRequestPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getErrors().isEmpty());
    }

    @Test
    void should_return_error_for_old_case_with_existing_service_request() {

        when(callback.getEvent()).thenReturn(Event.GENERATE_SERVICE_REQUEST);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(SERVICE_REQUEST_REFERENCE, String.class)).thenReturn(Optional.of(""));
        when(asylumCase.read(HAS_SERVICE_REQUEST_ALREADY, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(PAYMENT_STATUS, PaymentStatus.class)).thenReturn(Optional.of(PaymentStatus.PAYMENT_PENDING));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            serviceRequestPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertNotNull(callbackResponse);
        assertEquals(1, callbackResponse.getErrors().size());
        assertTrue(callbackResponse.getErrors().contains("A service request has already been created for this case. Pay via the 'Service Request' tab."));
    }

    @Test
    void should_return_error_for_new_case_with_existing_service_request() {

        when(callback.getEvent()).thenReturn(Event.GENERATE_SERVICE_REQUEST);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(SERVICE_REQUEST_REFERENCE, String.class)).thenReturn(Optional.of("aServiceRequestReference"));
        when(asylumCase.read(PAYMENT_STATUS, PaymentStatus.class)).thenReturn(Optional.of(PaymentStatus.PAYMENT_PENDING));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            serviceRequestPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertNotNull(callbackResponse);
        assertEquals(1, callbackResponse.getErrors().size());
        assertTrue(callbackResponse.getErrors().contains("A service request has already been created for this case. Pay via the 'Service Request' tab."));
    }

    @Test
    void should_not_return_error_for_new_case_with_existing_service_request_if_refund_confirmation_applied() {

        when(callback.getEvent()).thenReturn(Event.GENERATE_SERVICE_REQUEST);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(SERVICE_REQUEST_REFERENCE, String.class)).thenReturn(Optional.of("aServiceRequestReference"));
        when(asylumCase.read(PAYMENT_STATUS, PaymentStatus.class)).thenReturn(Optional.of(PaymentStatus.PAYMENT_PENDING));
        when(asylumCase.read(REFUND_CONFIRMATION_APPLIED, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            serviceRequestPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertNotNull(callbackResponse);
        assertEquals(1, callbackResponse.getErrors().size());
        assertTrue(callbackResponse.getErrors().contains("A service request has already been created for this case. Pay via the 'Service Request' tab."));
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> serviceRequestPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        assertThatThrownBy(() -> serviceRequestPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = serviceRequestPreparer.canHandle(callbackStage, callback);

                if (callbackStage == PreSubmitCallbackStage.ABOUT_TO_START
                    && event == Event.GENERATE_SERVICE_REQUEST) {

                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }

            reset(callback);
        }
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> serviceRequestPreparer.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> serviceRequestPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void should_return_error_if_refund_confirmation_is_not_applied_for_next_service_request() {

        when(callback.getEvent()).thenReturn(Event.GENERATE_SERVICE_REQUEST);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(SERVICE_REQUEST_REFERENCE, String.class)).thenReturn(Optional.of(""));
        when(asylumCase.read(HAS_SERVICE_REQUEST_ALREADY, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(PAYMENT_STATUS, PaymentStatus.class)).thenReturn(Optional.of(PaymentStatus.PAID));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            serviceRequestPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertNotNull(callbackResponse);
        assertEquals(1, callbackResponse.getErrors().size());
        assertTrue(callbackResponse.getErrors().contains("Refund confirmation should be done first to make another service request."));
    }
}
