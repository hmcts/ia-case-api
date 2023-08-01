package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_INTERPRETER_LANGUAGE_CATEGORY;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_INTERPRETER_SERVICES_NEEDED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_WITNESSES_ATTENDING;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_DETAILS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.UPDATE_HEARING_REQUIREMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.InterpreterLanguagesUtils.WITNESS_LIST_ELEMENT_N_FIELD;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.InterpreterLanguagesUtils.WITNESS_N_FIELD;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.InterpreterLanguagesUtils.WITNESS_N_INTERPRETER_CATEGORY_FIELD;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.InterpreterLanguagesUtils.WITNESS_N_INTERPRETER_SIGN_LANGUAGE;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.InterpreterLanguagesUtils.WITNESS_N_INTERPRETER_SPOKEN_LANGUAGE;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.InterpreterLanguagesUtils.buildWitnessFullName;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.InterpreterLanguagesUtils.clearWitnessIndividualFields;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.InterpreterLanguagesUtils.clearWitnessInterpreterLanguageFields;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.InterpreterLanguagesDynamicListUpdater.INTERPRETER_LANGUAGES;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.InterpreterLanguagesDynamicListUpdater.NO_WITNESSES_SELECTED_ERROR;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.InterpreterLanguagesDynamicListUpdater.SIGN;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.InterpreterLanguagesDynamicListUpdater.SIGN_LANGUAGES;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.InterpreterLanguagesDynamicListUpdater.SPOKEN;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.InterpreterLanguagesDynamicListUpdater.generateDynamicList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DynamicMultiSelectList;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.InterpreterLanguageRefData;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Value;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.WitnessDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

@Component
public class WitnessesUpdateMidEventHandler extends WitnessesDraftMidEventHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private static final String IS_WITNESSES_ATTENDING_PAGE_ID = "isWitnessesAttending";
    private static final String IS_INTERPRETER_SERVICES_NEEDED_PAGE_ID = "isInterpreterServicesNeeded";
    private static final String APPELLANT_INTERPRETER_SPOKEN_LANGUAGE_PAGE_ID = "appellantInterpreterSpokenLanguage";
    private static final String APPELLANT_INTERPRETER_SIGN_LANGUAGE_PAGE_ID = "appellantInterpreterSignLanguage";
    private static final String WHICH_WITNESS_REQUIRES_INTERPRETER_PAGE_ID = "whichWitnessRequiresInterpreter";
    private static final String IS_ANY_WITNESS_INTERPRETER_REQUIRED_PAGE_ID = "isAnyWitnessInterpreterRequired";
    private static final String ADDITIONAL_INSTRUCTIONS_PAGE_ID = "additionalInstructions";
    private static final String WITNESSES_NUMBER_EXCEEDED_ERROR = "Maximum number of witnesses is 10";
    private List<AsylumCaseFieldDefinition> fieldsToBeCleared = new ArrayList<>();

    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        String pageId = callback.getPageId();

        return (callbackStage == MID_EVENT
               && callback.getEvent().equals(UPDATE_HEARING_REQUIREMENTS)
               && Set.of(IS_WITNESSES_ATTENDING_PAGE_ID,
            IS_INTERPRETER_SERVICES_NEEDED_PAGE_ID,
            APPELLANT_INTERPRETER_SPOKEN_LANGUAGE_PAGE_ID,
            APPELLANT_INTERPRETER_SIGN_LANGUAGE_PAGE_ID,
            WHICH_WITNESS_REQUIRES_INTERPRETER_PAGE_ID,
            IS_ANY_WITNESS_INTERPRETER_REQUIRED_PAGE_ID).contains(pageId))
               || (callbackStage == ABOUT_TO_SUBMIT
                   && callback.getEvent().equals(UPDATE_HEARING_REQUIREMENTS));
    }

    @Override
    public DispatchPriority getDispatchPriority() {
        return DispatchPriority.LATEST;
    }

    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }
        
        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        PreSubmitCallbackResponse<AsylumCase> response = new PreSubmitCallbackResponse<>(asylumCase);

        if (callbackStage.equals(ABOUT_TO_SUBMIT)) {
            fieldsToBeCleared.forEach(asylumCase::clear);
            return response;
        }

        AsylumCase oldAsylumCase = callback.getCaseDetailsBefore().orElse(callback.getCaseDetails()).getCaseData();
        String pageId = callback.getPageId();
        YesOrNo isWitnessAttending = asylumCase.read(IS_WITNESSES_ATTENDING, YesOrNo.class).orElse(NO);

        Optional<List<String>> optionalAppellantInterpreterLanguageCategory = asylumCase
            .read(APPELLANT_INTERPRETER_LANGUAGE_CATEGORY);
        List<String> appellantInterpreterLanguageCategory = optionalAppellantInterpreterLanguageCategory
            .orElse(Collections.emptyList());

        Optional<List<IdValue<WitnessDetails>>> optionalWitnesses = asylumCase.read(WITNESS_DETAILS);

        Map<Integer,Integer> newToOldWitnessesIndexes = newWitnessesToOldWitnessesIndexes(callback);

        switch (pageId) {
            case IS_WITNESSES_ATTENDING_PAGE_ID:

                // cannot add more than 10 witnesses to the collection
                optionalWitnesses.ifPresent(witnesses -> {
                    if (witnesses.size() > WITNESS_N_FIELD.size()) {        // 10
                        response.addError(WITNESSES_NUMBER_EXCEEDED_ERROR);
                    }
                });

                if (optionalWitnesses.isEmpty() || isWitnessAttending.equals(NO)) {
                    // if no witnesses present nullify with dummies all witness-related fields (clearing does not work)
                    clearWitnessIndividualFields(asylumCase);
                    clearWitnessInterpreterLanguageFields(asylumCase);
                }
                break;

            case IS_INTERPRETER_SERVICES_NEEDED_PAGE_ID:
                // skip if this isn't the last page before "whichWitnessRequiresInterpreter"
                YesOrNo isInterpreterServicesNeeded = asylumCase.read(IS_INTERPRETER_SERVICES_NEEDED, YesOrNo.class)
                    .orElse(NO);
                if (isInterpreterServicesNeeded.equals(YES)) {
                    break;
                }

            case APPELLANT_INTERPRETER_SPOKEN_LANGUAGE_PAGE_ID:
                // skip if this isn't the last page before "whichWitnessRequiresInterpreter"
                if (appellantInterpreterLanguageCategory.contains(SPOKEN)) {
                    break;
                }

            case APPELLANT_INTERPRETER_SIGN_LANGUAGE_PAGE_ID:
                // skip if this isn't the last page before "whichWitnessRequiresInterpreter"
                if (appellantInterpreterLanguageCategory.contains(SIGN)) {
                    break;
                }

            case IS_ANY_WITNESS_INTERPRETER_REQUIRED_PAGE_ID:

                if (optionalWitnesses.isEmpty() || isWitnessAttending.equals(NO)) {
                    // if no witnesses present nullify with dummies all witness-related fields (clearing does not work)
                    clearWitnessIndividualFields(asylumCase);
                    clearWitnessInterpreterLanguageFields(asylumCase);

                } else {
                    optionalWitnesses.ifPresent(witnesses -> transferOldWitnessesToNewWitnesses(
                        asylumCase, oldAsylumCase, witnesses, newToOldWitnessesIndexes));
                }
                break;

            case WHICH_WITNESS_REQUIRES_INTERPRETER_PAGE_ID:

                InterpreterLanguageRefData spokenLanguages = generateDynamicList(INTERPRETER_LANGUAGES);
                InterpreterLanguageRefData signLanguages = generateDynamicList(SIGN_LANGUAGES);

                Map<Integer,List<String>> witnessIndexToInterpreterNeeded = new HashMap<>();

                int numberOfWitnesses = optionalWitnesses.map(List::size).orElse(0);

                Map<Integer, InterpreterLanguageRefData> existingSpokenSelections = new HashMap<>();
                Map<Integer, InterpreterLanguageRefData> existingSignSelections = new HashMap<>();

                newToOldWitnessesIndexes.forEach((n,o) -> {
                    oldAsylumCase
                        .read(WITNESS_N_INTERPRETER_SPOKEN_LANGUAGE.get(o), InterpreterLanguageRefData.class)
                        .ifPresent(spokenLanguageSelection -> existingSpokenSelections.put(n, spokenLanguageSelection));

                    oldAsylumCase
                        .read(WITNESS_N_INTERPRETER_SIGN_LANGUAGE.get(o), InterpreterLanguageRefData.class)
                        .ifPresent(signLanguageSelection -> existingSignSelections.put(n, signLanguageSelection));
                });

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
                            InterpreterLanguageRefData selection = existingSpokenSelections.get(index) != null
                                ? existingSpokenSelections.get(index)
                                : spokenLanguages;
                            asylumCase.write(WITNESS_N_INTERPRETER_SPOKEN_LANGUAGE.get(index), selection);
                        }
                        if (Objects.equals(interpreterCategory, SIGN)) {
                            InterpreterLanguageRefData selection = existingSignSelections.get(index) != null
                                ? existingSignSelections.get(index)
                                : signLanguages;
                            asylumCase.write(WITNESS_N_INTERPRETER_SIGN_LANGUAGE.get(index), selection);
                        }
                    });
                });

                int j = 0;
                while (j < 10) {
                    if (witnessIndexToInterpreterNeeded.get(j) == null) {
                        asylumCase.clear(WITNESS_N_INTERPRETER_SPOKEN_LANGUAGE.get(j));
                        asylumCase.clear(WITNESS_N_INTERPRETER_SIGN_LANGUAGE.get(j));
                        asylumCase.write(WITNESS_N_INTERPRETER_CATEGORY_FIELD.get(j), Collections.emptyList());

                        fieldsToBeCleared.addAll(
                            Set.of(WITNESS_N_INTERPRETER_SPOKEN_LANGUAGE.get(j),
                            WITNESS_N_INTERPRETER_SIGN_LANGUAGE.get(j))
                        );
                    } else {
                        if (!witnessIndexToInterpreterNeeded.get(j).contains(SPOKEN)) {
                            asylumCase.clear(WITNESS_N_INTERPRETER_SPOKEN_LANGUAGE.get(j));
                            fieldsToBeCleared.add(WITNESS_N_INTERPRETER_SPOKEN_LANGUAGE.get(j));
                        }
                        if (!witnessIndexToInterpreterNeeded.get(j).contains(SIGN)) {
                            asylumCase.clear(WITNESS_N_INTERPRETER_SIGN_LANGUAGE.get(j));
                            fieldsToBeCleared.add(WITNESS_N_INTERPRETER_SPOKEN_LANGUAGE.get(j));
                        }
                    }
                    j++;
                }
            //
            //case ADDITIONAL_INSTRUCTIONS_PAGE_ID:
            //    fieldsToBeCleared.forEach(asylumCase::clear);
            default:
                break;
        }

        return response;
    }

    private Map<Integer,Integer> newWitnessesToOldWitnessesIndexes(Callback<AsylumCase> callback) {
        Map<Integer,Integer> newToOld = new HashMap<>();

        CaseDetails<AsylumCase> oldCaseDetails = callback.getCaseDetailsBefore().orElse(null);
        CaseDetails<AsylumCase> newCaseDetails = callback.getCaseDetails();

        if (oldCaseDetails != null) {
            Optional<List<IdValue<WitnessDetails>>> optionalOldWitnesses = oldCaseDetails
                .getCaseData().read(WITNESS_DETAILS);
            Optional<List<IdValue<WitnessDetails>>> optionalNewWitnesses = newCaseDetails
                .getCaseData().read(WITNESS_DETAILS);

            List<IdValue<WitnessDetails>> oldWitnesses = optionalOldWitnesses.orElse(Collections.emptyList());
            List<IdValue<WitnessDetails>> newWitnesses = optionalNewWitnesses.orElse(Collections.emptyList());

            int o = 0;
            int n = 0;
            while (o < oldWitnesses.size()) {
                while (n < newWitnesses.size()) {

                    if (StringUtils.equals(
                        oldWitnesses.get(o).getValue().buildWitnessFullName(),
                        newWitnesses.get(n).getValue().buildWitnessFullName())) {

                        newToOld.put(n, o);
                    }
                    n++;
                }
                n = 0;
                o++;
            }
        }

        return newToOld;
    }


    protected AsylumCase transferOldWitnessesToNewWitnesses(AsylumCase asylumCase,
                                                            AsylumCase oldAsylumCase,
                                                            List<IdValue<WitnessDetails>> witnesses,
                                                            Map<Integer,Integer> newToOldIndexes) {

        Map<Integer,DynamicMultiSelectList> previousWitnessSelections = new HashMap<>();
        newToOldIndexes.forEach((n,o) -> previousWitnessSelections.put(
            o,
            oldAsylumCase.read(WITNESS_LIST_ELEMENT_N_FIELD.get(o), DynamicMultiSelectList.class).orElse(null)
        ));

        Map<Integer,List<String>> previousCategoriesPerWitnessSelections = new HashMap<>();
        newToOldIndexes.forEach((n,o) -> {
            boolean hasSpokenLanguage = oldAsylumCase.read(WITNESS_N_INTERPRETER_SPOKEN_LANGUAGE.get(o), InterpreterLanguageRefData.class)
                .map(language -> language.getLanguageRefData() != null || language.getLanguageManualEntryDescription() != null)
                .orElse(false);
            boolean hasSignLanguage = oldAsylumCase.read(WITNESS_N_INTERPRETER_SIGN_LANGUAGE.get(o), InterpreterLanguageRefData.class)
                .map(language -> language.getLanguageRefData() != null || language.getLanguageManualEntryDescription() != null)
                .orElse(false);

            List<String> selection = new ArrayList<>();
            if (hasSpokenLanguage) {
                selection.add(SPOKEN);
            }
            if (hasSignLanguage) {
                selection.add(SIGN);
            }
            previousCategoriesPerWitnessSelections.put(o, selection);
        });

        int i = 0;
        while (i < witnesses.size()) {

            String fullName = buildWitnessFullName(witnesses.get(i).getValue());

            asylumCase.write(WITNESS_N_FIELD.get(i), witnesses.get(i).getValue());

            DynamicMultiSelectList existingWitnessSelection = previousWitnessSelections.get(newToOldIndexes.get(i)) != null
                ? previousWitnessSelections.get(newToOldIndexes.get(i))
                : new DynamicMultiSelectList(Collections.emptyList(), List.of(new Value(fullName, fullName))
            );

            asylumCase.write(WITNESS_LIST_ELEMENT_N_FIELD.get(i), existingWitnessSelection);

            List<String> existingCategoryPerWitnessSelection = Collections.emptyList();

            if (newToOldIndexes.get(i) != null) {
                existingCategoryPerWitnessSelection = previousCategoriesPerWitnessSelections.get(newToOldIndexes.get(i));
            }

            asylumCase.write(WITNESS_N_INTERPRETER_CATEGORY_FIELD.get(i), existingCategoryPerWitnessSelection);
            i++;
        }
        while (i < 10) {
            // clearing does not work, dummy fields have to be set and then hidden with ccd
            asylumCase.write(WITNESS_N_FIELD.get(i), new WitnessDetails("", ""));
            asylumCase.write(WITNESS_LIST_ELEMENT_N_FIELD.get(i), new DynamicMultiSelectList());
            i++;
        }
        return asylumCase;
    }

}
