package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PostSubmitCallbackHandler;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_ACCELERATED_DETAINED_APPEAL;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.STF_24W_PREVIOUS_STATUS_WAS_YES_AUTO_GENERATED;

@Component
public class SubmitHearingRequirementsConfirmation implements PostSubmitCallbackHandler<AsylumCase> {

    public boolean canHandle(
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callback, "callback must not be null");

        return callback.getEvent() == Event.DRAFT_HEARING_REQUIREMENTS;
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

        boolean isAcceleratedDetainedAppeal = asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)
            .orElse(YesOrNo.NO)
            .equals(YesOrNo.YES);

        postSubmitResponse.setConfirmationHeader("# You've submitted your hearing requirements");

        boolean is24w = asylumCase.read(STF_24W_PREVIOUS_STATUS_WAS_YES_AUTO_GENERATED, YesOrNo.class)
            .orElse(YesOrNo.NO)
            .equals(YesOrNo.YES);

        if (isAcceleratedDetainedAppeal) {
            postSubmitResponse.setConfirmationBody(
                """
                    #### What happens next
                    
                    The Tribunal will review your hearing requirements and any additional requests for adjustments.<br><br>\
                    You’ll be able to see any agreed adjustments in the hearing and appointment tab."""
            );

        } else if (is24w) {
            postSubmitResponse.setConfirmationBody(
                """
                    #### What happens next
                    
                    The Tribunal will review your hearing requirements and any additional requests for adjustments."""
            );
        } else {
            postSubmitResponse.setConfirmationBody(
                """
                    #### What happens next
                    
                    The Tribunal will review your hearing requirements and any additional requests for adjustments.<br><br>\
                    We'll notify you when the hearing is listed. You'll then be able to review the hearing requirements."""
            );
        }


        return postSubmitResponse;
    }
}
