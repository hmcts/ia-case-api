package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.DECISION_HEARING_FEE_OPTION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.STF_24W_PREVIOUS_STATUS_WAS_YES_AUTO_GENERATED;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PostSubmitCallbackHandler;

@Component
public class CaseSubmittedConfirmation implements PostSubmitCallbackHandler<AsylumCase> {

    @Value("${featureFlag.isSaveAndContinueEnabled}")
    private boolean isSaveAndContinueEnabled;

    public boolean canHandle(
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callback, "callback must not be null");

        Event validEvent = isSaveAndContinueEnabled ? Event.SUBMIT_CASE : Event.BUILD_CASE;
        return callback.getEvent() == validEvent;
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

        boolean is24WeekCase = asylumCase.read(STF_24W_PREVIOUS_STATUS_WAS_YES_AUTO_GENERATED, YesOrNo.class)
            .orElse(YesOrNo.NO)
            .equals(YesOrNo.YES);

        boolean isDecisionWithHearing = asylumCase.read(DECISION_HEARING_FEE_OPTION, String.class)
            .map("decisionWithHearing"::equals)
            .orElse(false);

        postSubmitResponse.setConfirmationHeader("# You have submitted your case");

        if (is24WeekCase && isDecisionWithHearing) {
            postSubmitResponse.setConfirmationBody(
                """
                We have sent you a confirmation email

                #### What happens next
                The case officer will now review the appeal. \
                If it complies with the procedure rules and practice directions, you will be asked to provide any hearing requirements."""
            );
        } else {
            postSubmitResponse.setConfirmationBody(
                """
                We have sent you a confirmation email

                #### What happens next
                The case officer will now review your appeal. \
                If it complies with the procedure rules and practice directions, they will send it to the respondent for them to review."""
            );
        }

        return postSubmitResponse;
    }
}
