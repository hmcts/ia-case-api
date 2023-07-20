package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.WitnessesMidEventHandler.WITNESS_LIST_ELEMENT_N_FIELD;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.WitnessesMidEventHandler.WITNESS_N_INTERPRETER_CATEGORY_FIELD;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DynamicMultiSelectList;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.InterpreterLanguageRefData;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Value;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.RefDataUserService;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.dto.hearingdetails.CategoryValues;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.dto.hearingdetails.CommonDataResponse;

@Component
public class InterpreterLanguagesDynamicListUpdater implements PreSubmitCallbackHandler<AsylumCase> {

    RefDataUserService refDataUserService;

    public InterpreterLanguagesDynamicListUpdater(RefDataUserService refDataUserService) {
        this.refDataUserService = refDataUserService;
    }

    private static final String NO_WITNESSES_SELECTED_ERROR = "Select at least one witness";
    private static final String DRAFT_HEARING_REQUIREMENTS_PAGE_ID = "draftHearingRequirements";
    private static final String WHICH_WITNESS_REQUIRES_INTERPRETER_PAGE_ID = "whichWitnessRequiresInterpreter";
    public static final String INTERPRETER_LANGUAGES = "InterpreterLanguage";
    public static final String SIGN_LANGUAGES = "SignLanguage";
    public static final String IS_CHILD_REQUIRED = "Y";
    public static final String YES = "Yes";
    protected static final List<AsylumCaseFieldDefinition> WITNESS_N_INTERPRETER_SPOKEN_LANGUAGE = List.of(
        WITNESS_1_INTERPRETER_SPOKEN_LANGUAGE,
        WITNESS_2_INTERPRETER_SPOKEN_LANGUAGE,
        WITNESS_3_INTERPRETER_SPOKEN_LANGUAGE,
        WITNESS_4_INTERPRETER_SPOKEN_LANGUAGE,
        WITNESS_5_INTERPRETER_SPOKEN_LANGUAGE,
        WITNESS_6_INTERPRETER_SPOKEN_LANGUAGE,
        WITNESS_7_INTERPRETER_SPOKEN_LANGUAGE,
        WITNESS_8_INTERPRETER_SPOKEN_LANGUAGE,
        WITNESS_9_INTERPRETER_SPOKEN_LANGUAGE,
        WITNESS_10_INTERPRETER_SPOKEN_LANGUAGE
        );
    protected static final List<AsylumCaseFieldDefinition> WITNESS_N_INTERPRETER_SIGN_LANGUAGE = List.of(
        WITNESS_1_INTERPRETER_SIGN_LANGUAGE,
        WITNESS_2_INTERPRETER_SIGN_LANGUAGE,
        WITNESS_3_INTERPRETER_SIGN_LANGUAGE,
        WITNESS_4_INTERPRETER_SIGN_LANGUAGE,
        WITNESS_5_INTERPRETER_SIGN_LANGUAGE,
        WITNESS_6_INTERPRETER_SIGN_LANGUAGE,
        WITNESS_7_INTERPRETER_SIGN_LANGUAGE,
        WITNESS_8_INTERPRETER_SIGN_LANGUAGE,
        WITNESS_9_INTERPRETER_SIGN_LANGUAGE,
        WITNESS_10_INTERPRETER_SIGN_LANGUAGE
    );
    private static final String SPOKEN = "spokenLanguageInterpreter";
    private static final String SIGN = "signLanguageInterpreter";

    public boolean canHandle(
            PreSubmitCallbackStage callbackStage,
            Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.MID_EVENT
            && List.of(Event.DRAFT_HEARING_REQUIREMENTS, Event.UPDATE_HEARING_REQUIREMENTS)
                .contains(callback.getEvent());
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
            PreSubmitCallbackStage callbackStage,
            Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase =
                callback
                        .getCaseDetails()
                        .getCaseData();

        String pageId = callback.getPageId();

        if (Objects.equals(pageId, DRAFT_HEARING_REQUIREMENTS_PAGE_ID)) {
            populateDynamicList(asylumCase, INTERPRETER_LANGUAGES, APPELLANT_INTERPRETER_SPOKEN_LANGUAGE);
            populateDynamicList(asylumCase, SIGN_LANGUAGES, APPELLANT_INTERPRETER_SIGN_LANGUAGE);
        }

        PreSubmitCallbackResponse<AsylumCase> response = new PreSubmitCallbackResponse<>(asylumCase);

        int numberOfWitnesses = asylumCase.read(WITNESS_DETAILS, List.class).orElse(Collections.emptyList()).size();

        if (Objects.equals(pageId, WHICH_WITNESS_REQUIRES_INTERPRETER_PAGE_ID)) {

            InterpreterLanguageRefData spokenLanguages = generateDynamicList(INTERPRETER_LANGUAGES);
            InterpreterLanguageRefData signLanguages = generateDynamicList(SIGN_LANGUAGES);

            Map<Integer,List<String>> witnessIndexToInterpreterNeeded = new HashMap<>();

            int i = 0;
            while (i < numberOfWitnesses) {
                DynamicMultiSelectList witnessElement = asylumCase
                    .read(WITNESS_LIST_ELEMENT_N_FIELD.get(i), DynamicMultiSelectList.class).orElse(null);

                boolean witnessSelected = witnessElement != null
                                        && !witnessElement.getValue().isEmpty()
                                        && !witnessElement.getValue().get(0).getLabel().isEmpty();

                if (witnessSelected) {

                    Optional<List<String>> optionalSelectedInterpreterType = asylumCase.read(WITNESS_N_INTERPRETER_CATEGORY_FIELD.get(i));
                    List<String> selectedInterpreterType = optionalSelectedInterpreterType.isEmpty()
                        ? Collections.emptyList()
                        : optionalSelectedInterpreterType.get();

                    witnessIndexToInterpreterNeeded.put(i, selectedInterpreterType);
                }
                i++;
            }

            if (witnessIndexToInterpreterNeeded.isEmpty()) {
                response.addError(NO_WITNESSES_SELECTED_ERROR);
            }

            witnessIndexToInterpreterNeeded.forEach((index, interpretersNeeded) -> {
                interpretersNeeded.forEach(interpreterCategory -> {
                    if (Objects.equals(interpreterCategory, SPOKEN)) {
                        asylumCase.write(WITNESS_N_INTERPRETER_SPOKEN_LANGUAGE.get(index), spokenLanguages);
                    }
                    if (Objects.equals(interpreterCategory, SIGN)) {
                        asylumCase.write(WITNESS_N_INTERPRETER_SIGN_LANGUAGE.get(index), signLanguages);
                    }
                });
            });

        }

        return response;
    }

    private void populateDynamicList(AsylumCase asylumCase, String languageCategory, AsylumCaseFieldDefinition languageCategoryDefinition) {
        InterpreterLanguageRefData interpreterLanguage = generateDynamicList(languageCategory);

        asylumCase.write(languageCategoryDefinition, interpreterLanguage);
    }

    private InterpreterLanguageRefData generateDynamicList(String languageCategory) {
        List<CategoryValues> languages;
        DynamicList dynamicListOfLanguages;

        try {
            CommonDataResponse commonDataResponse = refDataUserService.retrieveCategoryValues(
                languageCategory,
                IS_CHILD_REQUIRED
            );

            languages = refDataUserService.filterCategoryValuesByCategoryId(commonDataResponse, languageCategory);

            dynamicListOfLanguages = new DynamicList(new Value("", ""),
                refDataUserService.mapCategoryValuesToDynamicListValues(languages));

        } catch (Exception e) {
            throw new RuntimeException(String.format("Could not read response by RefData service for %s(s)", languageCategory), e);
        }

        return new InterpreterLanguageRefData(
            dynamicListOfLanguages,
            Collections.emptyList(),
            "");
    }
}

