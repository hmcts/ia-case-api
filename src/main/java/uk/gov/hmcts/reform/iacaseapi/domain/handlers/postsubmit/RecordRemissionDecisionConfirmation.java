package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.REMISSION_DECISION;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionDecision;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PostSubmitCallbackHandler;

@Component
public class RecordRemissionDecisionConfirmation implements PostSubmitCallbackHandler<AsylumCase> {

    public boolean canHandle(
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callback, "callback must not be null");

        return callback.getEvent() == Event.RECORD_REMISSION_DECISION;
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

        String whatHappenNextLabel = "#### What happens next\n\n";

        asylumCase.read(REMISSION_DECISION, RemissionDecision.class)
            .ifPresent(remissionDecision -> {
                switch (remissionDecision) {
                    case APPROVED:
                        postSubmitResponse.setConfirmationHeader("# You have approved this remission application");
                        postSubmitResponse.setConfirmationBody(
                            whatHappenNextLabel
                            + "The appellant will be notified that you have approved this remission application. "
                            + "The appeal will progress as usual.<br>"
                        );
                        break;

                    case PARTIALLY_APPROVED:
                        postSubmitResponse.setConfirmationHeader("# You have partially approved this remission application");
                        postSubmitResponse.setConfirmationBody(
                            whatHappenNextLabel
                            + "The appellant will be notified that they need to pay the outstanding fee. "
                            + "Once payment is made you will need to mark the appeal as paid.<br>"
                        );
                        break;

                    case REJECTED:
                        postSubmitResponse.setConfirmationHeader("# You have rejected this remission application");
                        postSubmitResponse.setConfirmationBody(
                            whatHappenNextLabel
                            + "The appellant will be notified that they must pay the full fee for this appeal.<br>"
                        );
                        break;

                    default:
                        break;
                }
            });

        return postSubmitResponse;
    }
}
