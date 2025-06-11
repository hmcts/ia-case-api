package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

import java.util.Arrays;
import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.GENERATE_LIST_CMR_TASK_REQUESTED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HOME_OFFICE_SEARCH_STATUS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.GENERATE_LIST_CMR_TASK;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;

@Slf4j
@Component
public class GenerateListCmrTaskHandler implements PreSubmitCallbackHandler<AsylumCase> {

    public GenerateListCmrTaskHandler() {
    }

    public boolean canHandle(
            PreSubmitCallbackStage callbackStage,
            Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        Event event = callback.getEvent();

        return (callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT 
                || callbackStage == PreSubmitCallbackStage.ABOUT_TO_START)
                && GENERATE_LIST_CMR_TASK == event;
    }

        public PreSubmitCallbackResponse<AsylumCase> handle(
            PreSubmitCallbackStage callbackStage,
            Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        if (callbackStage == PreSubmitCallbackStage.ABOUT_TO_START) {
            return handleAboutToStart(asylumCase);
        } else if (callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT) {
            return handleAboutToSubmit(asylumCase);
        } else {
            throw new IllegalStateException("Unexpected callback stage: " + callbackStage);
        }
    }

    private PreSubmitCallbackResponse<AsylumCase> handleAboutToStart(AsylumCase asylumCase) {
        // ABOUT_TO_START: Only perform validation, no data modification
        PreSubmitCallbackResponse<AsylumCase> callbackResponse = new PreSubmitCallbackResponse<>(asylumCase);
        
        validateAppellantDetailsForPaRpAppeals(asylumCase, callbackResponse);
        
        return callbackResponse;
    }

    private PreSubmitCallbackResponse<AsylumCase> handleAboutToSubmit(AsylumCase asylumCase) {
        // ABOUT_TO_SUBMIT: Perform validation and set the flag
        PreSubmitCallbackResponse<AsylumCase> callbackResponse = new PreSubmitCallbackResponse<>(asylumCase);
        
        validateAppellantDetailsForPaRpAppeals(asylumCase, callbackResponse);
        
        // Only proceed to set flag if validation passed
        if (callbackResponse.getErrors().isEmpty()) {
            asylumCase.write(GENERATE_LIST_CMR_TASK_REQUESTED, YES);
        }
        
        return callbackResponse;
    }

    private void validateAppellantDetailsForPaRpAppeals(AsylumCase asylumCase, PreSubmitCallbackResponse<AsylumCase> callbackResponse) {
        final AppealType appealType = asylumCase.read(APPEAL_TYPE, AppealType.class)
                .orElseThrow(() -> new IllegalStateException("AppealType is not present."));

        // Only check for RP and PA appeal types
        if (Arrays.asList(AppealType.RP, AppealType.PA).contains(appealType)
                && appellantDetailsNotMatchedOrFailed(asylumCase)) {

            callbackResponse.addError("You need to match the appellant details before you can generate the list CMR task.");
        }
    }

    private boolean appellantDetailsNotMatchedOrFailed(AsylumCase asylumCase) {
        Optional<String> homeOfficeSearchStatus = asylumCase.read(HOME_OFFICE_SEARCH_STATUS, String.class);

        return homeOfficeSearchStatus.isEmpty()
                || Arrays.asList("FAIL", "MULTIPLE").contains(homeOfficeSearchStatus.get());
    }
}
