package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.postsubmit;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.bailcaseapi.domain.handlers.PostSubmitCallbackHandler;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.clients.CcdSupplementaryUpdater;


@Component
public class BailApplicationSavedConfirmation implements PostSubmitCallbackHandler<BailCase> {

    private final CcdSupplementaryUpdater ccdSupplementaryUpdater;

    public BailApplicationSavedConfirmation(CcdSupplementaryUpdater ccdSupplementaryUpdater) {
        this.ccdSupplementaryUpdater = ccdSupplementaryUpdater;
    }

    @Override
    public boolean canHandle(Callback<BailCase> callback) {
        return (callback.getEvent() == Event.START_APPLICATION);
    }

    @Override
    public PostSubmitCallbackResponse handle(Callback<BailCase> callback) {
        if (!canHandle(callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        PostSubmitCallbackResponse postSubmitResponse =
            new PostSubmitCallbackResponse();

        ccdSupplementaryUpdater.setHmctsServiceIdSupplementary(callback);

        postSubmitResponse.setConfirmationHeader("# You have saved this application");
        postSubmitResponse.setConfirmationBody(
            "### Do this next\n\n"
                + "If you're ready to submit your application, select 'Submit your application' in "
                + "the 'Next step' dropdown list from your case details page.\n\n"
                + "#### Not ready to submit your application yet?\n"
                + "You can return to the case details page to make changes from the ‘Next step’ dropdown list."
        );

        return postSubmitResponse;
    }
}
