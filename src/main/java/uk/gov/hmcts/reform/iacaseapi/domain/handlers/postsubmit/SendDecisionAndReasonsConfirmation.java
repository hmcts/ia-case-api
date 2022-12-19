package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.ADA_HEARING_REQUIREMENTS_UPDATABLE;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PostSubmitCallbackHandler;

@Component
public class SendDecisionAndReasonsConfirmation implements PostSubmitCallbackHandler<AsylumCase> {

    public boolean canHandle(
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callback, "callback must not be null");

        return callback.getEvent() == Event.SEND_DECISION_AND_REASONS;
    }

    public PostSubmitCallbackResponse handle(
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        PostSubmitCallbackResponse postSubmitResponse =
            new PostSubmitCallbackResponse();

        final AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        boolean isAcceleratedDetainedAppeal = HandlerUtils.isAcceleratedDetainedAppeal(asylumCase);

        if (isAcceleratedDetainedAppeal) {
            asylumCase.write(ADA_HEARING_REQUIREMENTS_UPDATABLE, YesOrNo.NO);
        }

        postSubmitResponse.setConfirmationHeader("# You've uploaded the Decision and Reasons document");
        postSubmitResponse.setConfirmationBody(
            "#### What happens next\n\n"
            + "Both parties have been notified of the decision. They'll also be able to access the Decision and Reasons document from the Documents tab."
        );

        return postSubmitResponse;
    }
}
