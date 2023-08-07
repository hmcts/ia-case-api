package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_INTERPRETER_SIGN_LANGUAGE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_INTERPRETER_SPOKEN_LANGUAGE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_DETAILS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.DRAFT_HEARING_REQUIREMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.UPDATE_HEARING_REQUIREMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.InterpreterLanguagesUtils.WITNESS_LIST_ELEMENT_N_FIELD;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.InterpreterLanguagesUtils.WITNESS_N_INTERPRETER_CATEGORY_FIELD;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.InterpreterLanguagesUtils.WITNESS_N_INTERPRETER_SIGN_LANGUAGE;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.InterpreterLanguagesUtils.WITNESS_N_INTERPRETER_SPOKEN_LANGUAGE;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DynamicMultiSelectList;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.InterpreterLanguageRefData;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Value;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
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

    private final RefDataUserService refDataUserService;

    public InterpreterLanguagesDynamicListUpdater(RefDataUserService refDataUserService) {
        this.refDataUserService = refDataUserService;
    }

    private static final String APPELLANT_INTERPRETER_LANGUAGE_CATEGORY = "appellantInterpreterLanguageCategory";
    private static final String WHICH_WITNESS_REQUIRES_INTERPRETER_PAGE_ID = "whichWitnessRequiresInterpreter";
    public static final String INTERPRETER_LANGUAGES = "InterpreterLanguage";
    public static final String SIGN_LANGUAGES = "SignLanguage";
    public static final String IS_CHILD_REQUIRED = "Y";
    public static final String YES = "Yes";
    protected static final String NO_WITNESSES_SELECTED_ERROR = "Select at least one witness";
    protected static final String SPOKEN = "spokenLanguageInterpreter";
    protected static final String SIGN = "signLanguageInterpreter";

    public boolean canHandle(
            PreSubmitCallbackStage callbackStage,
            Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.MID_EVENT
               && Set.of(DRAFT_HEARING_REQUIREMENTS, UPDATE_HEARING_REQUIREMENTS).contains(callback.getEvent())
               && Set.of(APPELLANT_INTERPRETER_LANGUAGE_CATEGORY, WHICH_WITNESS_REQUIRES_INTERPRETER_PAGE_ID)
                   .contains(callback.getPageId());
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

        AsylumCase asylumCaseBefore = callback.getCaseDetailsBefore()
            .map(CaseDetails::getCaseData)
            .orElse(asylumCase);

        String pageId = callback.getPageId();
        Event event = callback.getEvent();

        Optional<CaseDetails<AsylumCase>> optionalCaseDetailsBefore = callback.getCaseDetailsBefore();

        Optional<InterpreterLanguageRefData> appellantSpokenLanguage = optionalCaseDetailsBefore
            .flatMap(caseDetailsBefore ->
                caseDetailsBefore.getCaseData()
                    .read(APPELLANT_INTERPRETER_SPOKEN_LANGUAGE, InterpreterLanguageRefData.class));

        Optional<InterpreterLanguageRefData> appellantSignLanguage = optionalCaseDetailsBefore
            .flatMap(caseDetailsBefore ->
                caseDetailsBefore.getCaseData()
                    .read(APPELLANT_INTERPRETER_SIGN_LANGUAGE, InterpreterLanguageRefData.class));

        boolean shouldPopulateSpokenLanguage = appellantSpokenLanguage
            .map(appellantSpoken -> appellantSpoken.getLanguageRefData() == null)
            .orElse(true);

        boolean shouldPopulateSignLanguage = appellantSignLanguage
            .map(appellantSign -> appellantSign.getLanguageRefData() == null)
            .orElse(true);

        // POPULATE APPELLANT INTERPRETER LANGUAGE DYNAMIC LIST IF NECESSARY
        // TO BE RUN FOR: 1) DRAFT_HEARING_REQUIREMENTS 2) UPDATE_HEARING_REQUIREMENTS

        if (Objects.equals(pageId, APPELLANT_INTERPRETER_LANGUAGE_CATEGORY)) {
            if (shouldPopulateSpokenLanguage) {
                populateDynamicList(asylumCase, asylumCaseBefore, INTERPRETER_LANGUAGES, APPELLANT_INTERPRETER_SPOKEN_LANGUAGE);
            }
            if (shouldPopulateSignLanguage) {
                populateDynamicList(asylumCase, asylumCaseBefore, SIGN_LANGUAGES, APPELLANT_INTERPRETER_SIGN_LANGUAGE);
            }
        }

        PreSubmitCallbackResponse<AsylumCase> response = new PreSubmitCallbackResponse<>(asylumCase);

        int numberOfWitnesses = asylumCase.read(WITNESS_DETAILS, List.class).orElse(Collections.emptyList()).size();

        // ONLY RUN THIS FOR DRAFT_HEARING_REQUIREMENTS
        // UPDATE_HEARING_REQUIREMENTS TAKES CARE OF WITNESS INTERPRETERS IN WitnessUpdateMidEventHandler

        if (event.equals(DRAFT_HEARING_REQUIREMENTS)
            && Objects.equals(pageId, WHICH_WITNESS_REQUIRES_INTERPRETER_PAGE_ID)) {

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

    private void populateDynamicList(AsylumCase asylumCase, AsylumCase asylumCaseBefore, String languageCategory, AsylumCaseFieldDefinition languageCategoryDefinition) {
        InterpreterLanguageRefData interpreterLanguage = generateDynamicList(languageCategory);

        Optional<InterpreterLanguageRefData> optionalExistingInterpreterLanguageRefData = asylumCaseBefore.read(languageCategoryDefinition);

        optionalExistingInterpreterLanguageRefData.ifPresent(existing -> {
            interpreterLanguage.setLanguageManualEntry(existing.getLanguageManualEntry());
            interpreterLanguage.setLanguageManualEntryDescription(existing.getLanguageManualEntryDescription());
        });

        asylumCase.write(languageCategoryDefinition, interpreterLanguage);
    }

    public InterpreterLanguageRefData generateDynamicList(String languageCategory) {
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

