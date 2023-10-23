package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HEARING_ADJOURNMENT_WHEN;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.RELIST_CASE_IMMEDIATELY;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.HearingAdjournmentDay.BEFORE_HEARING_DATE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.RECORD_ADJOURNMENT_DETAILS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.HearingAdjournmentDay;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PostSubmitCallbackHandler;

@Component
public class RecordAdjournmentDetailsConfirmation implements PostSubmitCallbackHandler<AsylumCase> {

    public boolean canHandle(
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callback, "callback must not be null");

        return callback.getEvent() == RECORD_ADJOURNMENT_DETAILS;
    }

    public PostSubmitCallbackResponse handle(
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        HearingAdjournmentDay  hearingAdjournmentDay = asylumCase.read(HEARING_ADJOURNMENT_WHEN, HearingAdjournmentDay.class)
                .orElseThrow(() -> new IllegalStateException("Hearing adjournment day is not present"));
        boolean relistCaseImmediately = asylumCase.read(RELIST_CASE_IMMEDIATELY, YesOrNo.class)
                .map(relist -> YES == relist)
                .orElseThrow(() -> new IllegalStateException("Response to relist case immediately is not present"));

        PostSubmitCallbackResponse postSubmitResponse =
                new PostSubmitCallbackResponse();

        postSubmitResponse.setConfirmationHeader("# You have recorded the adjournment details");

        long caseId = callback.getCaseDetails().getId();
        if (hearingAdjournmentDay == BEFORE_HEARING_DATE && relistCaseImmediately) {
            postSubmitResponse.setConfirmationBody(
                "#### Do this next\n\n"
                    + "The hearing will be adjourned using the details recorded.\n\n"
                    + "The adjournment details are available on the [Hearing requirements tab](/cases/case-details/"
                    + caseId + "#Hearing%20and%20appointment)."
            );
        } else if (hearingAdjournmentDay == BEFORE_HEARING_DATE) {
            postSubmitResponse.setConfirmationBody(
                "#### Do this next\n\n"
                    + "All parties will be informed of the decision to adjourn without a date.\n\n"
                    + "The existing hearing will be cancelled.\n\n"
                    + "The adjournment details are available on the [Hearing requirements tab](/cases/case-details/"
                    + caseId + "#Hearing%20and%20appointment)."
            );
        } else {
            postSubmitResponse.setConfirmationBody(
                "#### Do this next\n\n"
                    + "The hearing will be adjourned using the details recorded.\n\n"
                    + "The adjournment details are available on the [Hearing requirements tab](/cases/case-details/"
                    + caseId + "#Hearing%20and%20appointment).\n\n"
                    + "You must now [update the hearing actuals in the hearings tab](/cases/case-details/"
                    + caseId + "/hearings)."
            );
        }

        return postSubmitResponse;
    }
}
