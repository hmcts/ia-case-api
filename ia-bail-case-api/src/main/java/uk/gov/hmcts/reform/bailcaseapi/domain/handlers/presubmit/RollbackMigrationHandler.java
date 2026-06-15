package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.presubmit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.bailcaseapi.domain.handlers.PreSubmitCallbackHandler;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event.ROLLBACK_MIGRATION;

@Slf4j
@Component
public class RollbackMigrationHandler implements PreSubmitCallbackHandler<BailCase> {

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<BailCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
            && callback.getEvent() == ROLLBACK_MIGRATION;
    }

    @Override
    public PreSubmitCallbackResponse<BailCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<BailCase> callback) {

        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        BailCase bailCase = callback.getCaseDetails().getCaseData();

        bailCase.remove(BailCaseFieldDefinition.SEARCH_CRITERIA);
        bailCase.remove(BailCaseFieldDefinition.SEARCH_PARTIES);
        bailCase.remove(BailCaseFieldDefinition.CASE_MANAGEMENT_LOCATION);
        bailCase.remove(BailCaseFieldDefinition.CASE_MANAGEMENT_LOCATION_REF_DATA);
        bailCase.remove(BailCaseFieldDefinition.CASE_MANAGEMENT_CATEGORY);
        bailCase.remove(BailCaseFieldDefinition.STAFF_LOCATION);
        bailCase.remove(BailCaseFieldDefinition.STAFF_LOCATION_ID);

        return new PreSubmitCallbackResponse<>(bailCase);
    }

}
