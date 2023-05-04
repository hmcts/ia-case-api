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

        postSubmitResponse.setConfirmationBody(
            "### Do this next\n\n"
                    + "Review and [edit the application](/case/IA/Bail/"
                    + callback.getCaseDetails().getId()
                    + "/trigger/editBailApplication) if necessary. [Submit the application](/case/IA/Bail/"
                    + callback.getCaseDetails().getId()
                    + "/trigger/submitApplication) when you’re ready."
        );

        postSubmitResponse.setConfirmationHeader("# You have saved this application");

        return postSubmitResponse;
    }
}
