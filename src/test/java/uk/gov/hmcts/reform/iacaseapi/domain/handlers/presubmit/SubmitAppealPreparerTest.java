package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.PAY_FOR_THE_APPEAL_OPTION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.PA_APPEAL_TYPE_PAYMENT_OPTION;

import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class SubmitAppealPreparerTest {

    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;

    private SubmitAppealPreparer submitAppealPreparer;

    @Before
    public void setUp() {

        submitAppealPreparer =
                new SubmitAppealPreparer(true);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
    }

    @Test
    public void should_throw_error_on_submit_appeal_for_PA_payNow() {

        when(asylumCase.read(APPEAL_TYPE)).thenReturn(Optional.of(AppealType.PA));
        when(asylumCase.read(PA_APPEAL_TYPE_PAYMENT_OPTION, String.class)).thenReturn(Optional.of("payNow"));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            submitAppealPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertNotNull(callbackResponse);
        assertEquals("The Submit your appeal option is not available. "
                     + "Select Pay and submit to submit the appeal", callbackResponse.getErrors().iterator().next());
    }

    @Test
    public void should_return_true_for_PA_payNow() {

        when(asylumCase.read(APPEAL_TYPE)).thenReturn(Optional.of(AppealType.PA));
        when(asylumCase.read(PA_APPEAL_TYPE_PAYMENT_OPTION, String.class)).thenReturn(Optional.of("payNow"));

        assertTrue(submitAppealPreparer.isPaymentAppealTypePayNow(asylumCase));
    }

    @Test
    public void should_return_true_for_HU_payNow() {

        when(asylumCase.read(APPEAL_TYPE)).thenReturn(Optional.of(AppealType.HU));
        when(asylumCase.read(PAY_FOR_THE_APPEAL_OPTION, String.class)).thenReturn(Optional.of("payNow"));

        assertTrue(submitAppealPreparer.isPaymentAppealTypePayNow(asylumCase));
    }

    @Test
    public void should_return_false_for_HU_payLater() {

        when(asylumCase.read(APPEAL_TYPE)).thenReturn(Optional.of(AppealType.HU));
        when(asylumCase.read(PAY_FOR_THE_APPEAL_OPTION, String.class)).thenReturn(Optional.of("payLater"));

        assertFalse(submitAppealPreparer.isPaymentAppealTypePayNow(asylumCase));
    }

    @Test
    public void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);    

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = submitAppealPreparer.canHandle(callbackStage, callback);

                if ((event == Event.SUBMIT_APPEAL)
                        && callbackStage == PreSubmitCallbackStage.ABOUT_TO_START) {

                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }

            reset(callback);
        }
    }

    @Test
    public void should_not_check_for_feePaymentDisabled() {

        SubmitAppealPreparer submitAppealPreparer =
            new SubmitAppealPreparer(
                false
            );

        assertThatThrownBy(() -> submitAppealPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> submitAppealPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
                .hasMessage("Cannot handle callback")
                .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> submitAppealPreparer.canHandle(null, callback))
                .hasMessage("callbackStage must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> submitAppealPreparer.canHandle(PreSubmitCallbackStage.ABOUT_TO_START, null))
                .hasMessage("callback must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> submitAppealPreparer.handle(null, callback))
                .hasMessage("callbackStage must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> submitAppealPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, null))
                .hasMessage("callback must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

    }
}
