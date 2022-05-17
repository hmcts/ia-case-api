package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.CURRENT_CASE_STATE_VISIBLE_TO_ADMIN_OFFICER;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.CURRENT_CASE_STATE_VISIBLE_TO_ALL_USERS;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.CURRENT_CASE_STATE_VISIBLE_TO_HOME_OFFICE;

import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.CURRENT_CASE_STATE_VISIBLE_TO_JUDGE;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.CURRENT_CASE_STATE_VISIBLE_TO_LEGAL_REPRESENTATIVE;

import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.RECORD_DECISION_TYPE;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.State.DECISION_CONDITIONAL_BAIL;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.DecisionType;

import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.IntermediateState;

import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.bailcaseapi.domain.handlers.PreSubmitCallbackHandler;

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

        String currentCaseState = callback.getCaseDetails().getState().toString();

        BailCase bailCase =
            callback
                .getCaseDetails()
                .getCaseData();

        if (bailCase.read(RECORD_DECISION_TYPE, String.class).orElse("")
            .equals(DecisionType.CONDITIONAL_GRANT.toString()) && (callback.getEvent() == Event.RECORD_THE_DECISION)) {
            currentCaseState = DECISION_CONDITIONAL_BAIL.toString();
        }
        if (callback.getEvent().equals(Event.UPLOAD_SIGNED_DECISION_NOTICE)) {
            //Setting the field to intermediate state in order to use it in the caseTypeTab FieldShowConditions
            currentCaseState = IntermediateState.SIGNED_DECISION_NOTICE_UPLOADED.toString();
        }

        bailCase.write(CURRENT_CASE_STATE_VISIBLE_TO_LEGAL_REPRESENTATIVE, currentCaseState);
        bailCase.write(CURRENT_CASE_STATE_VISIBLE_TO_JUDGE, currentCaseState);
        bailCase.write(CURRENT_CASE_STATE_VISIBLE_TO_ADMIN_OFFICER, currentCaseState);
        bailCase.write(CURRENT_CASE_STATE_VISIBLE_TO_HOME_OFFICE, currentCaseState);
        bailCase.write(CURRENT_CASE_STATE_VISIBLE_TO_ALL_USERS, currentCaseState);

        return new PreSubmitCallbackResponse<>(bailCase);
    }
}
