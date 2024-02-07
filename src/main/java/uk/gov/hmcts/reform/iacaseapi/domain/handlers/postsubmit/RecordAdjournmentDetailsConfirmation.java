package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.MANUAL_CANCEL_HEARINGS_REQUIRED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.UPDATE_HMC_REQUEST_SUCCESS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.RECORD_ADJOURNMENT_DETAILS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.adjournedBeforeHearingDay;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.adjournedOnHearingDay;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.relistCaseImmediately;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PostSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.AutoRequestHearingService;

@Component
@RequiredArgsConstructor
public class RecordAdjournmentDetailsConfirmation implements PostSubmitCallbackHandler<AsylumCase> {

    private final AutoRequestHearingService autoRequestHearingService;

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

        long caseId = callback.getCaseDetails().getId();
        String confirmationHeader = "# You have recorded the adjournment details";
        PostSubmitCallbackResponse response = new PostSubmitCallbackResponse();

        response.setConfirmationHeader(confirmationHeader);

        if (shouldAutoRequestHearing(asylumCase)) {
            response = autoRequestHearingService
                .buildAutoHearingRequestConfirmation(asylumCase, confirmationHeader, caseId);
        } else if (shouldUpdateHearing(asylumCase)) {
            response.setConfirmationBody(buildUpdateHearingConfirmationBody(asylumCase, caseId));
        } else if (shouldDeleteHearing(asylumCase)) {
            response.setConfirmationBody(buildCancelHearingConfirmationBody(asylumCase, caseId));
        } else {
            response.setConfirmationBody("#### Do this next\n\n"
                                         + "The hearing will be adjourned using the details recorded.\n\n"
                                         + "The adjournment details are available on the [Hearing requirements tab](/cases/case-details/"
                                         + caseId + "#Hearing%20and%20appointment).\n\n"
                                         + "You must now [update the hearing actuals in the hearings tab](/cases/case-details/"
                                         + caseId + "/hearings).");
        }

        return response;
    }

    private boolean shouldUpdateHearing(AsylumCase asylumCase) {
        return relistCaseImmediately(asylumCase, true)
               && adjournedBeforeHearingDay(asylumCase);
    }

    private boolean shouldDeleteHearing(AsylumCase asylumCase) {
        return !relistCaseImmediately(asylumCase, true)
               && adjournedBeforeHearingDay(asylumCase);
    }

    private boolean shouldAutoRequestHearing(AsylumCase asylumCase) {

        boolean canAutoCreate = relistCaseImmediately(asylumCase, true)
                                 && adjournedOnHearingDay(asylumCase);
        return autoRequestHearingService.shouldAutoRequestHearing(asylumCase, canAutoCreate);
    }

    private String buildUpdateHearingConfirmationBody(AsylumCase asylumCase, long caseId) {

        boolean updateRequestSuccess = asylumCase.read(UPDATE_HMC_REQUEST_SUCCESS, YesOrNo.class)
            .map(requestSuccess -> YES == requestSuccess)
            .orElse(false);
        String hearingRequirementsTabUrl =
            "(/cases/case-details/" + caseId + "#Hearing%20and%20appointment)";

        if (updateRequestSuccess) {
            return "#### Do this next\n\n"
                   + "The hearing will be adjourned using the details recorded.\n\n"
                   + "The adjournment details are available on the [Hearing requirements tab](/cases/case-details/"
                   + caseId + "#Hearing%20and%20appointment).";
        } else {
            return "#### Do this next\n\n"
                   + "The hearing could not be automatically updated. You will need to update the "
                   + "[hearings manually](/cases/case-details/" + caseId + "/hearings )."
                   + "\n\nThe adjournment details are available on the "
                   + "[Hearing requirements]" + hearingRequirementsTabUrl
                   + " tab.";
        }
    }

    private String buildCancelHearingConfirmationBody(AsylumCase asylumCase, long caseId) {

        boolean cancelRequestSuccess = asylumCase.read(MANUAL_CANCEL_HEARINGS_REQUIRED, YesOrNo.class)
            .map(cancelHearingRequired -> NO == cancelHearingRequired)
            .orElse(false);

        if (cancelRequestSuccess) {
            return "#### Do this next\n\n"
                   + "All parties will be informed of the decision to adjourn without a date.\n\n"
                   + "The existing hearing will be cancelled.\n\n"
                   + "The adjournment details are available on the [Hearing requirements tab](/cases/case-details/"
                   + caseId + "#Hearing%20and%20appointment).";
        } else {
            return "#### Do this next\n\n"
                   + "All parties will be informed of the decision to adjourn without a date.\n\n"
                   + "The hearing could not be automatically cancelled. "
                   + "The hearing can be cancelled on the [Hearings tab](/cases/case-details/" + caseId + "/hearings)\n\n"
                   + "The adjournment details are available on the "
                   + "[Hearing requirements tab](/cases/case-details/" + caseId + "#Hearing%20and%20appointment).";
        }
    }

}
