package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;

import java.util.Arrays;
import java.util.Collections;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

@Component
public class ChangeDirectionDueDateActionAvailableUpdater implements PreSubmitCallbackHandler<AsylumCase> {

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT;
    }

    @Override
    public DispatchPriority getDispatchPriority() {
        return DispatchPriority.LATEST;
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        CaseDetails<AsylumCase> caseDetails =
            callback
                .getCaseDetails();

        AsylumCase asylumCase = caseDetails.getCaseData();

        if (Arrays.asList(
            State.APPEAL_SUBMITTED,
            State.APPEAL_SUBMITTED_OUT_OF_TIME,
            State.AWAITING_RESPONDENT_EVIDENCE,
            State.CASE_BUILDING,
            State.CASE_UNDER_REVIEW,
            State.RESPONDENT_REVIEW,
            State.SUBMIT_HEARING_REQUIREMENTS,
            State.LISTING,
            State.PREPARE_FOR_HEARING,
            State.FINAL_BUNDLING,
            State.PRE_HEARING
        ).contains(caseDetails.getState())
            && !asylumCase.getDirections().orElse(Collections.emptyList()).isEmpty()) {

            asylumCase.setChangeDirectionDueDateActionAvailable(YesOrNo.YES);
        } else {
            asylumCase.setChangeDirectionDueDateActionAvailable(YesOrNo.NO);
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
