package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PostSubmitCallbackHandler;

import static java.util.Objects.requireNonNull;

@Component
@Slf4j
public class HearingsUpdateHearingRequestConfirmation implements PostSubmitCallbackHandler<AsylumCase> {

    @Override
    public boolean canHandle(Callback<AsylumCase> callback) {
        requireNonNull(callback, "callback must not be null");

        return Event.UPDATE_HEARING_REQUEST == callback.getEvent();
    }

    public PostSubmitCallbackResponse handle(
            Callback<AsylumCase> callback
    ) {
        requireNonNull(callback, "callback must not be null");

        PostSubmitCallbackResponse postSubmitResponse =
                new PostSubmitCallbackResponse();
        postSubmitResponse.setConfirmationHeader("# You've updated the hearing");
        postSubmitResponse.setConfirmationBody(
                """
                        #### What happens next
                        The hearing will be updated as directed.
                        
                        If required, parties will be informed of the changes to the hearing."""
        );
        return postSubmitResponse;
    }
}
