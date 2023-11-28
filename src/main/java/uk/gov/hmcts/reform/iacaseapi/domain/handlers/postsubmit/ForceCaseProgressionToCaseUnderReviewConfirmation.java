package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.JOURNEY_TYPE;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.JourneyType;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PostSubmitCallbackHandler;

@Component
public class ForceCaseProgressionToCaseUnderReviewConfirmation implements PostSubmitCallbackHandler<AsylumCase> {

    public boolean canHandle(
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callback, "callback must not be null");

        return callback.getEvent() == Event.FORCE_CASE_TO_CASE_UNDER_REVIEW;
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
        postSubmitResponse.setConfirmationHeader("# You have forced the case progression to case under review");
        asylumCase.read(JOURNEY_TYPE, JourneyType.class)
            .ifPresent(journeyType -> {
                if (journeyType == JourneyType.AIP) {
                    postSubmitResponse.setConfirmationBody(
                            "#### What happens next\n\n"
                                    + "Appellant will be notified by email."
                    );
                } else {
                    postSubmitResponse.setConfirmationBody(
                            "#### What happens next\n\n"
                                    + "Legal representative will be notified by email."
                    );
                }
            });

        return postSubmitResponse;
    }
}
