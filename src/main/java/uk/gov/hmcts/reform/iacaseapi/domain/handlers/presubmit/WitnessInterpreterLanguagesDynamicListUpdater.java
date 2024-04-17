package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_DETAILS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.InterpreterLanguageCategory.SIGN_LANGUAGE_INTERPRETER;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.InterpreterLanguageCategory.SPOKEN_LANGUAGE_INTERPRETER;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.DRAFT_HEARING_REQUIREMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.InterpreterLanguagesUtils.WITNESS_N_INTERPRETER_CATEGORY_FIELD;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.InterpreterLanguagesUtils.WITNESS_N_INTERPRETER_SIGN_LANGUAGE;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.InterpreterLanguagesUtils.WITNESS_N_INTERPRETER_SPOKEN_LANGUAGE;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.InterpreterLanguageRefData;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.InterpreterLanguagesUtils;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.RefDataUserService;

@Component
public class WitnessInterpreterLanguagesDynamicListUpdater implements PreSubmitCallbackHandler<AsylumCase> {

    private final RefDataUserService refDataUserService;

    public WitnessInterpreterLanguagesDynamicListUpdater(RefDataUserService refDataUserService) {
        this.refDataUserService = refDataUserService;
    }

    private static final String WHICH_WITNESS_REQUIRES_INTERPRETER_PAGE_ID = "whichWitnessRequiresInterpreter";
    public static final String INTERPRETER_LANGUAGES = "InterpreterLanguage";
    public static final String SIGN_LANGUAGES = "SignLanguage";
    public static final String YES = "Yes";
    protected static final String NO_WITNESSES_SELECTED_ERROR = "Select at least one witness interpreter requirement";

    public boolean canHandle(
            PreSubmitCallbackStage callbackStage,
            Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.MID_EVENT
               && callback.getEvent().equals(DRAFT_HEARING_REQUIREMENTS)
               && Objects.equals(callback.getPageId(), WHICH_WITNESS_REQUIRES_INTERPRETER_PAGE_ID);
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
            PreSubmitCallbackStage callbackStage,
            Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        PreSubmitCallbackResponse<AsylumCase> response = new PreSubmitCallbackResponse<>(asylumCase);

        int numberOfWitnesses = asylumCase.read(WITNESS_DETAILS, List.class).orElse(Collections.emptyList()).size();

        // ONLY RUN THIS FOR DRAFT_HEARING_REQUIREMENTS
        // UPDATE_HEARING_REQUIREMENTS TAKES CARE OF WITNESS INTERPRETERS IN WitnessUpdateMidEventHandler

        InterpreterLanguageRefData spokenLanguages = generateDynamicList(INTERPRETER_LANGUAGES);
        InterpreterLanguageRefData signLanguages = generateDynamicList(SIGN_LANGUAGES);

        Map<Integer,List<String>> witnessIndexToInterpreterNeeded = new HashMap<>();

        int i = 0;
        while (i < numberOfWitnesses) {

            boolean witnessNeedInterpreter = !asylumCase
                .<List<String>>read(WITNESS_N_INTERPRETER_CATEGORY_FIELD.get(i))
                .orElse(Collections.emptyList()).isEmpty();

            if (witnessNeedInterpreter) {

                Optional<List<String>> optionalSelectedInterpreterType = asylumCase
                    .read(WITNESS_N_INTERPRETER_CATEGORY_FIELD.get(i));
                List<String> selectedInterpreterType = optionalSelectedInterpreterType.orElse(Collections.emptyList());

                witnessIndexToInterpreterNeeded.put(i, selectedInterpreterType);
            }
            i++;
        }

        if (witnessIndexToInterpreterNeeded.isEmpty()) {
            response.addError(NO_WITNESSES_SELECTED_ERROR);
        }

        witnessIndexToInterpreterNeeded.forEach((index, interpretersNeeded) -> {
            interpretersNeeded.forEach(interpreterCategory -> {
                if (Objects.equals(interpreterCategory, SPOKEN_LANGUAGE_INTERPRETER.getValue())) {
                    asylumCase.write(WITNESS_N_INTERPRETER_SPOKEN_LANGUAGE.get(index), spokenLanguages);
                }
                if (Objects.equals(interpreterCategory, SIGN_LANGUAGE_INTERPRETER.getValue())) {
                    asylumCase.write(WITNESS_N_INTERPRETER_SIGN_LANGUAGE.get(index), signLanguages);
                }
            });
        });

        return response;
    }

    public InterpreterLanguageRefData generateDynamicList(String languageCategory) {
        return InterpreterLanguagesUtils.generateDynamicList(refDataUserService, languageCategory);
    }
}

