package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit.applyforcosts;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_APPLY_FOR_COSTS_OOT;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PostSubmitCallbackHandler;

@Component
public class ApplyForCostsConfirmation implements PostSubmitCallbackHandler<AsylumCase> {

    @Override
    public boolean canHandle(
            Callback<AsylumCase> callback
    ) {

        requireNonNull(callback, "callback must not be null");
        return callback.getEvent() == Event.APPLY_FOR_COSTS;
    }

    @Override
    public PostSubmitCallbackResponse handle(
            Callback<AsylumCase> callback
    ) {
        if (!canHandle(callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        PostSubmitCallbackResponse postSubmitResponse =
                new PostSubmitCallbackResponse();

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        YesOrNo isApplyForCostsOot = asylumCase.read(IS_APPLY_FOR_COSTS_OOT, YesOrNo.class).orElse(YesOrNo.NO);

        String confirmationHeader = "";
        String confirmationBody = "";

        if (isApplyForCostsOot.equals(YesOrNo.YES)) {
            confirmationBody =
                    "![Out of time confirmation](https://raw.githubusercontent.com/hmcts/ia-appeal-frontend/master/app/assets/images/outOfTimeApplyForCostsConfirmation.svg)\n\n"
                    +
                    "## What happens next\n\n"
                    + "The Tribunal will consider the reason it has been submitted out of time.\n\n"
                    + "If the Tribunal accepts your reason, it will consider your application and make a decision shortly.";
        } else {
            confirmationHeader = "# You've made a costs application'";
            confirmationBody =
                    "## What happens next\n\n"
                            + "Both you and the other party will receive an email notification confirming your application.\n\n"
                            + "The other party has 14 days to respond to the claim.\n\n"
                            + "If you have requested a hearing, the Tribunal will consider your request.\n\n"
                            + "You can review the details of your application in the [Costs tab](/cases/case-details/" + callback.getCaseDetails().getId() + "#Costs). ";
        }

        postSubmitResponse.setConfirmationHeader(confirmationHeader);
        postSubmitResponse.setConfirmationBody(confirmationBody);

        return postSubmitResponse;
    }
}
