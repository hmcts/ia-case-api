package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.TimeExtensionReview;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CcdEvent;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CcdEventPostSubmitResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.EventId;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Stage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.CcdEventPostSubmitHandler;

@Component
public class TimeExtensionReviewConfirmation implements CcdEventPostSubmitHandler<AsylumCase> {

    public boolean canHandle(
        Stage stage,
        CcdEvent<AsylumCase> ccdEvent
    ) {
        return stage == Stage.SUBMITTED
               && ccdEvent.getEventId() == EventId.REVIEW_TIME_EXTENSION;
    }

    public CcdEventPostSubmitResponse handle(
        Stage stage,
        CcdEvent<AsylumCase> ccdEvent
    ) {
        if (!canHandle(stage, ccdEvent)) {
            throw new IllegalStateException("Cannot handle ccd event");
        }

        AsylumCase asylumCase =
            ccdEvent
                .getCaseDetails()
                .getCaseData();

        CcdEventPostSubmitResponse postSubmitResponse =
            new CcdEventPostSubmitResponse();

        TimeExtensionReview timeExtensionReview =
            asylumCase
                .getTimeExtensionReview()
                .orElseThrow(() -> new IllegalStateException("timeExtensionReview not present"));

        if (timeExtensionReview
            .getGrantOrDeny()
            .orElse("")
            .equals("grant")) {

            postSubmitResponse.setConfirmationHeader("# You have granted a time extension");
            postSubmitResponse.setConfirmationBody(
                "#### What happens next\n\n"
                + "Your decision has been sent to the legal rep who requested the time extension. "
                + "You should now update any direction due dates to reflect the extension."
            );

        } else {
            postSubmitResponse.setConfirmationHeader("# You have denied a time extension");
            postSubmitResponse.setConfirmationBody(
                "#### What happens next\n\n"
                + "Your decision has been sent to the legal rep who requested the time extension."
            );

        }

        return postSubmitResponse;
    }
}
