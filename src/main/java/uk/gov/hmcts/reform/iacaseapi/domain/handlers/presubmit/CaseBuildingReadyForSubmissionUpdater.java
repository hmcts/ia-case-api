package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumExtractor.CASE_ARGUMENT_DOCUMENT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumExtractor.CASE_BUILDING_READY_FOR_SUBMISSION;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.CaseDataMap;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

@Component
public class CaseBuildingReadyForSubmissionUpdater implements PreSubmitCallbackHandler<CaseDataMap> {

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

        final State caseState =
            callback
                .getCaseDetails()
                .getState();

        final CaseDataMap caseDataMap =
            callback
                .getCaseDetails()
                .getCaseData();

        if (caseState == State.CASE_BUILDING) {

            if (caseDataMap.get(CASE_ARGUMENT_DOCUMENT).isPresent()) {
                caseDataMap.write(CASE_BUILDING_READY_FOR_SUBMISSION, YesOrNo.YES);
            } else {
                caseDataMap.write(CASE_BUILDING_READY_FOR_SUBMISSION, YesOrNo.NO);
            }

        } else {
            caseDataMap.clear(CASE_BUILDING_READY_FOR_SUBMISSION);
        }

        return new PreSubmitCallbackResponse<>(caseDataMap);
    }
}
