package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;

import java.util.Arrays;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.CaseDataMap;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

@Component
public class SendDirectionActionAvailableUpdater implements PreSubmitCallbackHandler<CaseDataMap> {

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<CaseDataMap> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT;
    }

    public PreSubmitCallbackResponse<CaseDataMap> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<CaseDataMap> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        CaseDetails<CaseDataMap> caseDetails =
            callback
                .getCaseDetails();

        CaseDataMap CaseDataMap = caseDetails.getCaseData();

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
        ).contains(caseDetails.getState())) {
            CaseDataMap.setSendDirectionActionAvailable(YesOrNo.YES);
        } else {
            CaseDataMap.setSendDirectionActionAvailable(YesOrNo.NO);
        }

        return new PreSubmitCallbackResponse<>(CaseDataMap);
    }
}
