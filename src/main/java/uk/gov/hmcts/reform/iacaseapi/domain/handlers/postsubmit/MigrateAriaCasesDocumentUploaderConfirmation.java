package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.ARIA_DESIRED_STATE;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PostSubmitCallbackHandler;

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

        final State ariaDesiredState = asylumCase.read(ARIA_DESIRED_STATE, State.class)
            .orElseThrow(() -> new IllegalStateException("ariaDesiredState is not present"));

        postSubmitResponse.setConfirmationHeader("# You have progressed this case \n## New state: \n## '%s'".formatted(ariaDesiredState.getDescription()));
        postSubmitResponse.setConfirmationBody(
            """
            #### What happens next
            
            You can add or edit documents at any time through the 'Next step'\s
            dropdown list in your case details, using 'Edit documents'.
            
            #### If this case was listed for a hearing
            
            Listings have not been migrated. If this case was listed for a hearing,    \s
            you must transfer the listing over to the hearing management    \s
            component (HMC) as part of the migration process."""
        );

        return postSubmitResponse;
    }
}
