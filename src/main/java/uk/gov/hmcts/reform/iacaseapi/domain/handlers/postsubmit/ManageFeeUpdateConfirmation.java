package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FEE_UPDATE_COMPLETED_STAGES;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PostSubmitCallbackHandler;

@Component
public class ManageFeeUpdateConfirmation implements PostSubmitCallbackHandler<AsylumCase> {

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

        if (completedStages.isPresent()) {
            if (completedStages.get().size() > 1) {
                postSubmitResponse.setConfirmationHeader("# You have progressed a fee update");
                postSubmitResponse.setConfirmationBody(
                    "#### What happens next\n\n"
                        + "If you have recorded that a refund has been approved, you must now instruct the refund.\n\n"
                        + "If you have recorded that an additional fee has been requested, "
                        + "the legal representative will be instructed to pay the fee.\n\n"
                        + "If you have recorded that no fee update is required, you need to contact "
                        + "the legal representative and tell them why the fee update is no longer required.\n\n"
                );

                return postSubmitResponse;
            }
        }

        postSubmitResponse.setConfirmationHeader("# You have recorded a fee update");
        postSubmitResponse.setConfirmationBody(
            "#### What happens next\n\n"
                + "The appropriate team will be notified to review the fee update and take the next steps."
        );

        return postSubmitResponse;
    }
}
