package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static java.util.Objects.*;

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
public class RemoveAppealFromOnlineConfirmation implements PostSubmitCallbackHandler<AsylumCase> {

    private final TimeToLiveDataService timeToLiveDataService;

    public RemoveAppealFromOnlineConfirmation(TimeToLiveDataService timeToLiveDataService) {
        this.timeToLiveDataService = timeToLiveDataService;
    }

    @Override
    public boolean canHandle(Callback<AsylumCase> callback) {
        requireNonNull(callback, "callback must not be null");

        return callback.getEvent() == Event.REMOVE_APPEAL_FROM_ONLINE;
    }

    @Override
    public PostSubmitCallbackResponse handle(Callback<AsylumCase> callback) {

        if (!canHandle(callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        PostSubmitCallbackResponse postSubmitResponse =
            new PostSubmitCallbackResponse();

        postSubmitResponse.setConfirmationHeader("# You've removed this appeal from the online service");
        postSubmitResponse.setConfirmationBody(
            "## Do this next\n"
            + "You now need to:</br>"
            + "1.Contact the appellant and the respondent to inform them that the case will proceed offline.</br>"
            + "2.Save all files associated with the appeal to the shared drive.</br>"
            + "3.Email a link to the saved files with the appeal reference number to: BAUArnhemHouse@justice.gov.uk"
        );

        // CCD doesn't set the "ttl.suspended" to NO (clock is active) unless it's null.
        // When the clock has been stopped "ttl.suspended" gets populated with "YES" and isn't null anymore
        // So it requires manual intervention to set "ttl.suspended = NO" when starting the clock again
        if (wasSuccessful(callback) && !isClockActive(callback.getCaseDetails().getCaseData())) {
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
