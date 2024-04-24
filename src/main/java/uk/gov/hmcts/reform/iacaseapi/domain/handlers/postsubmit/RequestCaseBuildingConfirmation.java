package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.infrastructure.utils.DirectionFinder.resolvePartiesForConfirmationBody;

import java.util.Arrays;
import java.util.List;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DirectionTag;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PostSubmitCallbackHandler;

@Component
public class RequestCaseBuildingConfirmation implements PostSubmitCallbackHandler<AsylumCase> {

    public boolean canHandle(Callback<AsylumCase> callback) {
        requireNonNull(callback, "callback must not be null");
        List<Event> validEventList = Arrays.asList(Event.FORCE_REQUEST_CASE_BUILDING, Event.REQUEST_CASE_BUILDING);
        return validEventList.contains(callback.getEvent());
    }

    public PostSubmitCallbackResponse handle(Callback<AsylumCase> callback) {
        if (!canHandle(callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        PostSubmitCallbackResponse postSubmitResponse =
                new PostSubmitCallbackResponse();

        postSubmitResponse.setConfirmationHeader("# You have sent a direction");

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        postSubmitResponse.setConfirmationBody(
                "#### What happens next\n\n"
                        + resolvePartiesForConfirmationBody(asylumCase, DirectionTag.REQUEST_CASE_BUILDING)
                        + " will be notified by email."
        );

        return postSubmitResponse;
    }
}
