package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static java.util.Objects.requireNonNull;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AdaSuitabilityReviewDecision;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PostSubmitCallbackHandler;

@Component
public class AdaSuitabilityReviewConfirmation implements PostSubmitCallbackHandler<AsylumCase> {

    @Override
    public boolean canHandle(
        Callback<AsylumCase> callback
    ) {

        requireNonNull(callback, "callback must not be null");
        return callback.getEvent() == Event.ADA_SUITABILITY_REVIEW;
    }

    @Override
    public PostSubmitCallbackResponse handle(
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        final AdaSuitabilityReviewDecision decision =
            asylumCase.read(AsylumCaseFieldDefinition.SUITABILITY_REVIEW_DECISION, AdaSuitabilityReviewDecision.class)
                .orElseThrow(() -> new RequiredFieldMissingException("ADA suitability review decision unavailable."));
        String transferOutOfAdaUrl = "/trigger/transferOutOfAda";


        PostSubmitCallbackResponse postSubmitResponse =
            new PostSubmitCallbackResponse();

        if (decision.equals(AdaSuitabilityReviewDecision.SUITABLE)) {
            postSubmitResponse.setConfirmationHeader("# Appeal determined suitable to continue as ADA");
            postSubmitResponse.setConfirmationBody(
                "#### What happens next\n\n"
                    + "All parties have been notified. The Accelerated Detained Appeal Suitability Decision is available to view in the documents tab.<br>"
            );
        } else {
            postSubmitResponse.setConfirmationHeader("# Appeal determined unsuitable to continue as ADA");
            postSubmitResponse.setConfirmationBody(
                "#### What happens next\n\n"
                    + "All parties have been notified. The Accelerated Detained Appeal Suitability Decision is available to view in the documents tab.<br>"
                + "\n\nYou must [transfer this appeal out of the accelerated detained appeal process.](/case/IA/Asylum/"
                    + callback.getCaseDetails().getId() + transferOutOfAdaUrl + ")."
            );
        }

        return postSubmitResponse;
    }
}
