package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.FeeTribunalAction;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class ManageFeeUpdateConfirmationTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseCaseDetails;
    @Mock
    private AsylumCase asylumCase;

    private ManageFeeUpdateConfirmation manageFeeUpdateConfirmation;

    @BeforeEach
    void setUp() {

        manageFeeUpdateConfirmation = new ManageFeeUpdateConfirmation();
    }

    @Test
    void should_return_confirmation_for_fee_recorded() {

        when(callback.getCaseDetails()).thenReturn(caseCaseDetails);
        when(caseCaseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.MANAGE_FEE_UPDATE);

        PostSubmitCallbackResponse callbackResponse =
            manageFeeUpdateConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(
            callbackResponse.getConfirmationHeader().get())
            .contains("# You have recorded a fee update");

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains("#### What happens next\n\n"
                + "The appropriate team will be notified to review the fee update and take the next steps.");
    }

    @Test
    void should_return_confirmation_for_progressing_fee_update_status() {
        final List<String> completedStages =
            Arrays.asList(
                "feeUpdateRecorded",
                "feeUpdateAdditionalFeeRequested"
            );

        when(callback.getCaseDetails()).thenReturn(caseCaseDetails);
        when(caseCaseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.MANAGE_FEE_UPDATE);
        when(asylumCase.read(FEE_UPDATE_COMPLETED_STAGES)).thenReturn(Optional.of(completedStages));

        PostSubmitCallbackResponse callbackResponse =
            manageFeeUpdateConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(
            callbackResponse.getConfirmationHeader().get())
            .contains("# You have progressed a fee update");

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains("#### What happens next\n\n"
                + "If you have recorded that a refund has been approved, you must now instruct the refund.\n\n"
                + "If you have recorded that an additional fee has been requested, "
                + "the legal representative will be instructed to pay the fee.\n\n"
                + "If you have recorded that no fee update is required, you need to contact "
                + "the legal representative and tell them why the fee update is no longer required.\n\n");
    }

    @Test
    void should_return_confirmation_for_refund_instructed_fee_update_status() {
        final List<String> completedStages =
            Arrays.asList(
                "feeUpdateRecorded",
                "feeUpdateRefundApproved",
                "feeUpdateRefundInstructed"
            );

        when(callback.getCaseDetails()).thenReturn(caseCaseDetails);
        when(caseCaseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.MANAGE_FEE_UPDATE);
        when(asylumCase.read(FEE_UPDATE_COMPLETED_STAGES)).thenReturn(Optional.of(completedStages));

        PostSubmitCallbackResponse callbackResponse =
            manageFeeUpdateConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(
            callbackResponse.getConfirmationHeader().get())
            .contains("# You have marked the refund as instructed");

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains("#### What happens next\n\n"
                + "The legal representative will be notified that the refund has been instructed.\n\n");
    }

    @ParameterizedTest
    @EnumSource(value = FeeTribunalAction.class, names = {"REFUND", "ADDITIONAL_PAYMENT", "NO_ACTION"})
    void should_return_confirmation_for_dlrm_fee_update_tribunal_action_all_scenarios(FeeTribunalAction tribunalAction) {
        when(callback.getCaseDetails()).thenReturn(caseCaseDetails);
        when(caseCaseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.MANAGE_FEE_UPDATE);
        when(asylumCase.read(FEE_UPDATE_COMPLETED_STAGES)).thenReturn(Optional.empty());
        when(asylumCase.read(IS_DLRM_FEE_REFUND_ENABLED, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(FEE_UPDATE_TRIBUNAL_ACTION, FeeTribunalAction.class)).thenReturn(Optional.of(tribunalAction));

        PostSubmitCallbackResponse callbackResponse =
            manageFeeUpdateConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(
            callbackResponse.getConfirmationHeader().get())
            .contains("# You have recorded a fee update");

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains(
                switch (tribunalAction) {
                    case REFUND -> "The appropriate team will be notified to review the fee update and process a refund.";
                    case ADDITIONAL_PAYMENT -> "A payment request will be sent to the appellant.";
                    case NO_ACTION -> "The appeal fee has been updated. No further action is required.";
                }
            );
    }


    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> manageFeeUpdateConfirmation.handle(callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            boolean canHandle = manageFeeUpdateConfirmation.canHandle(callback);

            if (event == Event.MANAGE_FEE_UPDATE) {

                assertTrue(canHandle);
            } else {
                assertFalse(canHandle);
            }

            reset(callback);
        }
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> manageFeeUpdateConfirmation.canHandle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> manageFeeUpdateConfirmation.handle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }


}
