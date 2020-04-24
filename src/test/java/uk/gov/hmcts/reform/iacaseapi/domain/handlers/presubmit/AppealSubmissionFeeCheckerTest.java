package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.math.BigDecimal;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.FeeType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.fee.FeeDto;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.FeeService;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class AppealSubmissionFeeCheckerTest {

    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;
    @Mock private FeeService feeService;

    private AppealSubmissionFeeChecker appealSubmissionFeeChecker;

    @Before
    public void setUp() {

        appealSubmissionFeeChecker = new AppealSubmissionFeeChecker(feeService);

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
    }

    @Test
    public void should_retrieve_the_oral_fee_for_appeal_type_EA() {

        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.EA));
        when(feeService.getFee(FeeType.ORAL_FEE))
                .thenReturn(new FeeDto(new BigDecimal("140.00"), "Appeal determined with a hearing", 2, "FEE0238"));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse = appealSubmissionFeeChecker.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertThat(callbackResponse).isNotNull();
        verify(asylumCase, times(1)).write(FEE_AMOUNT_FOR_DISPLAY, "£140.00");
        verify(asylumCase, times(1)).write(APPEAL_FEE_DESC, "The fee for this type of appeal with a hearing is £140.00");
        verify(asylumCase, times(1)).write(PAYMENT_STATUS, "Payment due");
    }

    @Test
    public void should_throw_when_no_appeal_type_is_present() {

        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appealSubmissionFeeChecker.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
                .isExactlyInstanceOf(IllegalStateException.class)
                .hasMessage("AppealType is not present");
    }

    @Test
    public void should_return_error_when_fee_does_not_exists() {

        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.EA));
        when(feeService.getFee(FeeType.ORAL_FEE)).thenReturn(null);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse = appealSubmissionFeeChecker.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());
        assertTrue(callbackResponse.getErrors().contains("Cannot retrieve the fee from fees-register."));

    }

    @Test
    public void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> appealSubmissionFeeChecker.canHandle(null, callback))
                .hasMessage("callbackStage must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> appealSubmissionFeeChecker.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
                .hasMessage("callback must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> appealSubmissionFeeChecker.handle(null, callback))
                .hasMessage("callbackStage must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> appealSubmissionFeeChecker.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
                .hasMessage("callback must not be null")
                .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    public void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = appealSubmissionFeeChecker.canHandle(callbackStage, callback);

                if ((event == Event.SUBMIT_APPEAL)
                        && (callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT || callbackStage == PreSubmitCallbackStage.ABOUT_TO_START)) {

                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }
        }
    }
}
