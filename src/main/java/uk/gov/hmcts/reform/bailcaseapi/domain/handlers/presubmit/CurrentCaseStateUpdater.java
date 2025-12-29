package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition.CURRENT_CASE_STATE_VISIBLE_TO_ADMIN_OFFICER;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition.CURRENT_CASE_STATE_VISIBLE_TO_ALL_USERS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition.CURRENT_CASE_STATE_VISIBLE_TO_HOME_OFFICE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition.CURRENT_CASE_STATE_VISIBLE_TO_JUDGE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition.CURRENT_CASE_STATE_VISIBLE_TO_LEGAL_REPRESENTATIVE;


import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

@Component
public class CurrentCaseStateUpdater implements PreSubmitCallbackHandler<BailCase> {

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<BailCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT;
    }

    public PreSubmitCallbackResponse<BailCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<BailCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        State currentCaseState = callback.getCaseDetails().getState();

        BailCase bailCase =
            callback
                .getCaseDetails()
                .getCaseData();

        bailCase.write(CURRENT_CASE_STATE_VISIBLE_TO_LEGAL_REPRESENTATIVE, currentCaseState);
        bailCase.write(CURRENT_CASE_STATE_VISIBLE_TO_JUDGE, currentCaseState);
        bailCase.write(CURRENT_CASE_STATE_VISIBLE_TO_ADMIN_OFFICER, currentCaseState);
        bailCase.write(CURRENT_CASE_STATE_VISIBLE_TO_HOME_OFFICE, currentCaseState);
        bailCase.write(CURRENT_CASE_STATE_VISIBLE_TO_ALL_USERS, currentCaseState);

        return new PreSubmitCallbackResponse<>(bailCase);
    }
}
