package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.OutOfTimeDecisionType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PostSubmitCallbackHandler;

@Component
public class RecordOutOfTimeDecisionConfirmation implements PostSubmitCallbackHandler<AsylumCase> {

    public boolean canHandle(
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callback, "callback must not be null");

        return callback.getEvent() == Event.RECORD_OUT_OF_TIME_DECISION;
    }

    public PostSubmitCallbackResponse handle(
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase =
            callback
                .getCaseDetails()
                .getCaseData();

        PostSubmitCallbackResponse postSubmitResponse =
            new PostSubmitCallbackResponse();

        OutOfTimeDecisionType outOfTimeDecisionType =
            asylumCase.read(OUT_OF_TIME_DECISION_TYPE, OutOfTimeDecisionType.class)
                .orElseThrow(() -> new IllegalStateException("Out of time decision is not present"));

        switch (outOfTimeDecisionType) {
            case IN_TIME:
            case APPROVED:
                postSubmitResponse.setConfirmationHeader("# You have recorded an out of time decision");
                postSubmitResponse.setConfirmationBody(
                    "#### What happens next\n\n"
                    + "This appeal will proceed as usual.");
                break;

            case REJECTED:
                postSubmitResponse.setConfirmationHeader("# You have recorded that the appeal is out of time and cannot proceed");
                postSubmitResponse.setConfirmationBody(
                    "#### Do this next\n\n"
                    + "This appeal is out time and cannot proceed. "
                    + "You must [end the appeal](/case/IA/Asylum/" + callback.getCaseDetails().getId() + "/trigger/endAppeal).");
                break;

            default:
                break;
        }

        return postSubmitResponse;
    }

}
