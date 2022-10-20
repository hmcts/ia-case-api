package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static java.util.Objects.requireNonNull;

import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.TTL;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PostSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.ccddataservice.TimeToLiveDataService;

@Component
public class EndAppealConfirmation implements PostSubmitCallbackHandler<AsylumCase> {

    private final TimeToLiveDataService timeToLiveDataService;

    public EndAppealConfirmation(TimeToLiveDataService timeToLiveDataService) {
        this.timeToLiveDataService = timeToLiveDataService;
    }

    @Override
    public boolean canHandle(
        Callback<AsylumCase> callback
    ) {

        requireNonNull(callback, "callback must not be null");
        return callback.getEvent() == Event.END_APPEAL;
    }

    @Override
    public PostSubmitCallbackResponse handle(
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        final String hoEndAppealInstructStatus =
            asylumCase.read(AsylumCaseFieldDefinition.HOME_OFFICE_END_APPEAL_INSTRUCT_STATUS, String.class).orElse("");

        PostSubmitCallbackResponse postSubmitResponse =
            new PostSubmitCallbackResponse();

        if (hoEndAppealInstructStatus.equalsIgnoreCase("FAIL")) {
            postSubmitResponse.setConfirmationBody(
                "![Respondent notification failed confirmation]"
                + "(https://raw.githubusercontent.com/hmcts/ia-appeal-frontend/master/app/assets/images/respondent_notification_failed.svg)\n"
                + "#### Do this next\n\n"
                + "Contact the respondent to tell them what has changed, including any action they need to take.\n"
            );
        } else {

            postSubmitResponse.setConfirmationHeader("# You have ended the appeal");
            postSubmitResponse.setConfirmationBody(
                "#### What happens next\n\n"
                + "A notification has been sent to all parties.<br>"
            );
        }

        // CCD doesn't set the "ttl.suspended" to NO (clock is active) unless it's null.
        // When the clock has been stopped "ttl.suspended" gets populated with "YES" and isn't null anymore
        // So it requires manual intervention to set "ttl.suspended = NO" when starting the clock again
        if (wasSuccessful(callback) && !isClockActive(asylumCase)) {
            // stop the clock
            timeToLiveDataService.updateTheClock(callback, false);
        }

        return postSubmitResponse;
    }

    private boolean wasSuccessful(Callback<AsylumCase> callback) {
        CaseDetails<AsylumCase> caseDetails = callback.getCaseDetails();
        return caseDetails.getState().equals(State.ENDED);
    }

    private boolean isClockActive(AsylumCase asylumCase) {
        return asylumCase.read(AsylumCaseFieldDefinition.TTL, TTL.class)
            .map(ttl -> ttl.getSuspended().equals(YesOrNo.NO))
            .orElse(false);
    }
}
