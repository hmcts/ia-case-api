package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static java.util.Objects.requireNonNull;

import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PostSubmitCallbackHandler;


@Component
public class FtpaAppellantConfirmation implements PostSubmitCallbackHandler<AsylumCase> {

    public boolean canHandle(
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callback, "callback must not be null");

        return callback.getEvent() == Event.APPLY_FOR_FTPA_APPELLANT;
    }

    public PostSubmitCallbackResponse handle(
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final AsylumCase caseData = callback.getCaseDetails().getCaseData();
        final Optional<YesOrNo> mayBeOutOfTime = caseData.read(AsylumCaseFieldDefinition.FTPA_APPELLANT_SUBMISSION_OUT_OF_TIME, YesOrNo.class);

        PostSubmitCallbackResponse postSubmitResponse =
            new PostSubmitCallbackResponse();

        if (mayBeOutOfTime.isPresent() && mayBeOutOfTime.get().equals(YesOrNo.YES)) {
            postSubmitResponse.setConfirmationHeader("");
            postSubmitResponse.setConfirmationBody(
                "![FTPA Out of time confirmation](https://raw.githubusercontent.com/hmcts/ia-appeal-frontend/master/app/assets/images/ftpaOutOfTimeConfirmationSmall.png)\n"
                + "#### What happens next\n\n"
                + "The First-tier Tribunal will consider the reasons it has been submitted out of time. If the Tribunal accepts your reasons, it will consider your application and make a decision shortly.<br>"
            );

        } else {
            postSubmitResponse.setConfirmationHeader("# You've applied for permission to appeal to the Upper Tribunal");
            postSubmitResponse.setConfirmationBody(
                "#### What happens next\n\n"
                + "The First-tier Tribunal will review your application and decide shortly.<br>"
            );
        }

        return postSubmitResponse;
    }
}
