package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PostSubmitCallbackHandler;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.MANUAL_UPDATE_HEARING_REQUIRED;

@Component
@Slf4j
public class HearingsUpdateHearingRequestConfirmation implements PostSubmitCallbackHandler<AsylumCase> {

    public static final String HEARING_NEED_MANUAL_UPDATE =
        "<br>The hearing could not be automatically updated. You must manually update the hearing in the "
            + "[Hearings tab](/cases/case-details/%s/hearings)\n\n"
            + "If required, parties will be informed of the changes to the hearing.";

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

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        if (asylumCase.read(MANUAL_UPDATE_HEARING_REQUIRED).isPresent()) {
            postSubmitResponse.setConfirmationHeader("# Update hearing request complete");
            postSubmitResponse.setConfirmationBody(
                String.format(HEARING_NEED_MANUAL_UPDATE, callback.getCaseDetails().getId())
            );
        } else {
            postSubmitResponse.setConfirmationHeader("# You've updated the hearing");
            postSubmitResponse.setConfirmationBody(
                """
                        #### What happens next
                        The hearing will be updated as directed.
                        
                        If required, parties will be informed of the changes to the hearing."""
            );
        }

        return postSubmitResponse;
    }
}
