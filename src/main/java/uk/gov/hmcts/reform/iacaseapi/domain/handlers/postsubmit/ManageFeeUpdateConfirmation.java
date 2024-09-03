package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FEE_UPDATE_COMPLETED_STAGES;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FEE_UPDATE_TRIBUNAL_ACTION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_DLRM_FEE_REFUND_ENABLED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.FeeTribunalAction;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PostSubmitCallbackHandler;

@Component
public class ManageFeeUpdateConfirmation implements PostSubmitCallbackHandler<AsylumCase> {

    private static final String WHAT_HAPPENS_NEXT = "#### What happens next\n\n";

    public boolean canHandle(
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callback, "callback must not be null");

        return callback.getEvent() == Event.MANAGE_FEE_UPDATE;
    }

    public PostSubmitCallbackResponse handle(
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        PostSubmitCallbackResponse postSubmitResponse = new PostSubmitCallbackResponse();

        AsylumCase asylumCase =
            callback
                .getCaseDetails()
                .getCaseData();

        Optional<List<String>> completedStages = asylumCase.read(FEE_UPDATE_COMPLETED_STAGES);
        Optional<YesOrNo> dlrmRefundFlag = asylumCase.read(IS_DLRM_FEE_REFUND_ENABLED, YesOrNo.class);

        if (dlrmRefundFlag.isPresent() && dlrmRefundFlag.get() == YES) {
            FeeTribunalAction tribunalAction = asylumCase.read(FEE_UPDATE_TRIBUNAL_ACTION, FeeTribunalAction.class)
                .orElseThrow(() -> new IllegalStateException("Fee update tribunal action is not present"));

            postSubmitResponse.setConfirmationHeader("# You have recorded a fee update");
            postSubmitResponse.setConfirmationBody(
                WHAT_HAPPENS_NEXT
                + switch (tribunalAction) {
                    case REFUND -> "The appropriate team will be notified to review the fee update and process a refund.";
                    case ADDITIONAL_PAYMENT -> "A payment request will be sent to the appellant.";
                    case NO_ACTION -> "The appeal fee has been updated. No further action is required.";
                }
            );
            return postSubmitResponse;
        } else if (completedStages.isPresent() && completedStages.get().size() > 1) {
            String lastCompletedStep = completedStages.get().get(completedStages.get().size() - 1);

            if (lastCompletedStep.equals("feeUpdateRefundInstructed")) {

                postSubmitResponse.setConfirmationHeader("# You have marked the refund as instructed");
                postSubmitResponse.setConfirmationBody(
                    WHAT_HAPPENS_NEXT
                    + "The legal representative will be notified that the refund has been instructed.\n\n");
            } else {
                postSubmitResponse.setConfirmationHeader("# You have progressed a fee update");
                postSubmitResponse.setConfirmationBody(
                    WHAT_HAPPENS_NEXT
                    + "If you have recorded that a refund has been approved, you must now instruct the refund.\n\n"
                    + "If you have recorded that an additional fee has been requested, "
                    + "the legal representative will be instructed to pay the fee.\n\n"
                    + "If you have recorded that no fee update is required, you need to contact "
                    + "the legal representative and tell them why the fee update is no longer required.\n\n"
                );
            }
            return postSubmitResponse;
        } else {
            postSubmitResponse.setConfirmationHeader("# You have recorded a fee update");
            postSubmitResponse.setConfirmationBody(
                WHAT_HAPPENS_NEXT
                + "The appropriate team will be notified to review the fee update and take the next steps."
            );
            return postSubmitResponse;
        }
    }
}
