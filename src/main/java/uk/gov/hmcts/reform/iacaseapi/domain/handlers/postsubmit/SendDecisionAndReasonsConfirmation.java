package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static java.util.Objects.requireNonNull;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PostSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.HearingDecisionProcessor;

@Slf4j
@Component
public class SendDecisionAndReasonsConfirmation implements PostSubmitCallbackHandler<AsylumCase> {
    private final HearingDecisionProcessor hearingDecisionProcessor;

    public SendDecisionAndReasonsConfirmation(HearingDecisionProcessor hearingDecisionProcessor) {
        this.hearingDecisionProcessor = hearingDecisionProcessor;
    }

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

        PostSubmitCallbackResponse postSubmitResponse = new PostSubmitCallbackResponse();

        postSubmitResponse.setConfirmationHeader("# You've uploaded the Decision and Reasons document");
        postSubmitResponse.setConfirmationBody(
            "#### What happens next\n\n"
            + "Both parties have been notified of the decision. They'll also be able to access the Decision and Reasons document from the Documents tab."
        );

        log.info("------------- calling processHearingAppealDecision");
        hearingDecisionProcessor.processHearingAppealDecision(callback.getCaseDetails().getCaseData());

        return postSubmitResponse;
    }
}
