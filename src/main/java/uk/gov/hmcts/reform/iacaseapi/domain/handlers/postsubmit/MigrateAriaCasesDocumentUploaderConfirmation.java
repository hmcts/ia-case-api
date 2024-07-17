package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PostSubmitCallbackHandler;

import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.ARIA_DESIRED_STATE;

@Component
public class MigrateAriaCasesDocumentUploaderConfirmation implements PostSubmitCallbackHandler<AsylumCase> {

    public boolean canHandle(
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callback, "callback must not be null");

        return callback.getEvent() == Event.PROGRESS_MIGRATED_CASE;
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

        final Optional<State> maybeAriaDesiredState = asylumCase.read(ARIA_DESIRED_STATE, State.class);
        final State ariaDesiredState = maybeAriaDesiredState.get();

        postSubmitResponse.setConfirmationHeader(String.format("# You have progressed this case \n## New state: \n## '%s'", ariaDesiredState.getDescription()));
        postSubmitResponse.setConfirmationBody(
            "#### What happens next\n\n"
                + "You can add or edit documents at any time through the 'Next step' \n"
                + "dropdown list in your case details, using 'Edit documents'.\n\n"
                + "#### If this case was listed for a hearing\n\n"
                + "Listings have not been migrated. If this case was listed for a hearing,     \n"
                + "you must transfer the listing over to the hearing management \n"
                + "component (HMC) as part of the migration process."
        );

        return postSubmitResponse;
    }
}
