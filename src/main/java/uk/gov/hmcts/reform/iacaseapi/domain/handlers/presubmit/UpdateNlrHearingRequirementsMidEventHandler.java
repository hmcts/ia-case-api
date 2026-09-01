package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_NLR_INTERPRETER_REQUIRED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.NLR_INTERPRETER_LANGUAGE_CATEGORY;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.NLR_INTERPRETER_SIGN_LANGUAGE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.NLR_INTERPRETER_SPOKEN_LANGUAGE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.InterpreterLanguageCategory.SIGN_LANGUAGE_INTERPRETER;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.InterpreterLanguageCategory.SPOKEN_LANGUAGE_INTERPRETER;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.UPDATE_HEARING_REQUIREMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.MID_EVENT;

import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.InterpreterLanguageRefData;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Value;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

@Slf4j
@Component
public class UpdateNlrHearingRequirementsMidEventHandler extends WitnessHandler
    implements PreSubmitCallbackHandler<AsylumCase> {

    private static final String MANUAL_LANGUAGE_YES = "Yes";
    private static final String PAGE_ID = "nlrHearingRequirementsPage";
    public static final String SPOKEN_ERROR = "A Spoken language must be selected or manual entry must be added for the spoken language interpreter requirement";
    public static final String SPOKEN_MANUAL_ERROR = "Spoken language interpreter needed Enter the language manually field cannot be empty";
    public static final String SIGN_ERROR = "A Sign language must be selected or manual entry must be added for the sign language requirement";
    public static final String SIGN_MANUAL_ERROR = "Sign language needed Enter the language manually field cannot be empty";


    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        String pageId = callback.getPageId();

        return (callbackStage == MID_EVENT
            && callback.getEvent().equals(UPDATE_HEARING_REQUIREMENTS)
            && pageId.equals(PAGE_ID));
    }

    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        PreSubmitCallbackResponse<AsylumCase> response = new PreSubmitCallbackResponse<>(asylumCase);

        boolean needsInterpreter = asylumCase.read(IS_NLR_INTERPRETER_REQUIRED, YesOrNo.class)
            .map(YesOrNo::isYes).orElse(false);
        if (needsInterpreter) {
            Optional<List<String>> optionalCategory = asylumCase.read(NLR_INTERPRETER_LANGUAGE_CATEGORY);
            List<String> category = optionalCategory.orElse(List.of());
            if (category.contains(SPOKEN_LANGUAGE_INTERPRETER.getValue())) {
                List<String> manualEntry = asylumCase.read(NLR_INTERPRETER_SPOKEN_LANGUAGE, InterpreterLanguageRefData.class)
                    .map(InterpreterLanguageRefData::getLanguageManualEntry)
                    .orElse(List.of());
                String manualDesc = asylumCase.read(NLR_INTERPRETER_SPOKEN_LANGUAGE, InterpreterLanguageRefData.class)
                    .map(InterpreterLanguageRefData::getLanguageManualEntryDescription)
                    .orElse("");
                Value refDataValue = asylumCase.read(NLR_INTERPRETER_SPOKEN_LANGUAGE, InterpreterLanguageRefData.class)
                    .map(InterpreterLanguageRefData::getLanguageRefData)
                    .map(DynamicList::getValue)
                    .orElse(null);
                if (manualEntry.isEmpty() && refDataValue == null) {
                    response.addError(SPOKEN_ERROR);
                } else if (manualEntry.contains(MANUAL_LANGUAGE_YES) && manualDesc.isEmpty()) {
                    response.addError(SPOKEN_MANUAL_ERROR);
                }
            }

            if (category.contains(SIGN_LANGUAGE_INTERPRETER.getValue())) {
                List<String> manualEntry = asylumCase.read(NLR_INTERPRETER_SIGN_LANGUAGE, InterpreterLanguageRefData.class)
                    .map(InterpreterLanguageRefData::getLanguageManualEntry)
                    .orElse(List.of());
                String manualDesc = asylumCase.read(NLR_INTERPRETER_SIGN_LANGUAGE, InterpreterLanguageRefData.class)
                    .map(InterpreterLanguageRefData::getLanguageManualEntryDescription)
                    .orElse("");
                Value refDataValue = asylumCase.read(NLR_INTERPRETER_SIGN_LANGUAGE, InterpreterLanguageRefData.class)
                    .map(InterpreterLanguageRefData::getLanguageRefData)
                    .map(DynamicList::getValue)
                    .orElse(null);
                if (manualEntry.isEmpty() && refDataValue == null) {
                    response.addError(SIGN_ERROR);
                } else if (manualEntry.contains(MANUAL_LANGUAGE_YES) && manualDesc.isEmpty()) {
                    response.addError(SIGN_MANUAL_ERROR);
                }
            }
        }
        return response;
    }

}
