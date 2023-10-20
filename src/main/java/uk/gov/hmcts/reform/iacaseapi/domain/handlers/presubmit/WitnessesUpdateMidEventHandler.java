package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_INTERPRETER_LANGUAGE_CATEGORY;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_ANY_WITNESS_INTERPRETER_REQUIRED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_INTERPRETER_SERVICES_NEEDED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_WITNESSES_ATTENDING;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_COUNT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_DETAILS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_DETAILS_READONLY;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.InterpreterLanguageCategory.SIGN_LANGUAGE_INTERPRETER;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.InterpreterLanguageCategory.SPOKEN_LANGUAGE_INTERPRETER;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.UPDATE_HEARING_REQUIREMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.MID_EVENT;
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
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.WitnessInterpreterLanguagesDynamicListUpdater.INTERPRETER_LANGUAGES;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.WitnessInterpreterLanguagesDynamicListUpdater.NO_WITNESSES_SELECTED_ERROR;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.WitnessInterpreterLanguagesDynamicListUpdater.SIGN_LANGUAGES;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
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
import uk.gov.hmcts.reform.iacaseapi.domain.service.WitnessesService;

@Slf4j
@Component
public class WitnessesUpdateMidEventHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private static final String IS_WITNESSES_ATTENDING_PAGE_ID = "isWitnessesAttending";
    private static final String IS_INTERPRETER_SERVICES_NEEDED_PAGE_ID = "isInterpreterServicesNeeded";
    private static final String APPELLANT_INTERPRETER_SPOKEN_LANGUAGE_PAGE_ID = "appellantInterpreterSpokenLanguage";
    private static final String APPELLANT_INTERPRETER_SIGN_LANGUAGE_PAGE_ID = "appellantInterpreterSignLanguage";
    private static final String WHICH_WITNESS_REQUIRES_INTERPRETER_PAGE_ID = "whichWitnessRequiresInterpreter";
    private static final String IS_ANY_WITNESS_INTERPRETER_REQUIRED_PAGE_ID = "isAnyWitnessInterpreterRequired";
    private static final String WITNESSES_NUMBER_EXCEEDED_ERROR = "Maximum number of witnesses is 10";
    private List<AsylumCaseFieldDefinition> fieldsToBeCleared = new ArrayList<>();
    private final WitnessInterpreterLanguagesDynamicListUpdater witnessInterpreterLanguagesDynamicListUpdater;
    static final String SPOKEN = SPOKEN_LANGUAGE_INTERPRETER.getValue();
    static final String SIGN = SIGN_LANGUAGE_INTERPRETER.getValue();

    public WitnessesUpdateMidEventHandler(
        WitnessInterpreterLanguagesDynamicListUpdater witnessInterpreterLanguagesDynamicListUpdater) {

        this.witnessInterpreterLanguagesDynamicListUpdater = witnessInterpreterLanguagesDynamicListUpdater;
    }

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

        /*
        this handler is called with midEvents from six different pages in updateHearingRequirements
        to prop up those pages, plus it collects the fields that need to be cleared before submitting
        the event and clears them (collected in the fieldsToBeCleared list) at the aboutToSubmit stage
        because clearing in midEvents doesn't always work (midEvents don't commit data in database)
        */
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

        log.info("WitnessInterpreterMidEventHandler running for case {}", callback.getCaseDetails().getId());

        if (callbackStage.equals(ABOUT_TO_SUBMIT)) {
            log.info("fieldsToBeCleared at aboutToSubmit stage: {}", fieldsToBeCleared);
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

        // append witness party ID if missing
        WitnessesService.appendWitnessPartyId(asylumCase);

        Optional<List<IdValue<WitnessDetails>>> optionalWitnesses = asylumCase.read(WITNESS_DETAILS);
        List<IdValue<WitnessDetails>> witnesses = optionalWitnesses.orElseGet(Collections::emptyList);

        // mapping indexes of preexisting witnesses in new witness list to indexes in old witness list
        Map<Integer,Integer> newToOldWitnessesIndexes = newWitnessesToOldWitnessesIndexes(callback);
        log.info("newWitnessesToOldWitnessesIndexes: {}", newToOldWitnessesIndexes);

        // CODE REPETITION IN THIS STATEMENT IS DUE TO CHECKSTYLE NOT PERMITTING FALLTHROUGH
        switch (pageId) {
            case IS_WITNESSES_ATTENDING_PAGE_ID -> {

                // add error to the page where witnesses are added to prevent more than 10 witnesses
                // being added

                if (witnesses.isEmpty()) {
                    clearWitnessIndividualFields(asylumCase);
                    clearWitnessInterpreterLanguageFields(asylumCase);
                } else if (witnesses.size() > WITNESS_N_FIELD.size()) { // 10
                    // cannot add more than 10 witnesses to the collection
                    response.addError(WITNESSES_NUMBER_EXCEEDED_ERROR);
                }
            }
            case IS_INTERPRETER_SERVICES_NEEDED_PAGE_ID -> {
                log.info("WitnessInterpreterMidEventHandler on page: {}", IS_INTERPRETER_SERVICES_NEEDED_PAGE_ID);

                // skip if this isn't the last page before "whichWitnessRequiresInterpreter" otherwise do the mapping
                // as explained in transferOldWitnessesToNewWitnesses()

                boolean isInterpreterServicesNeeded = asylumCase
                    .read(IS_INTERPRETER_SERVICES_NEEDED, YesOrNo.class)
                    .map(YES::equals)
                    .orElse(false);



                if (!isInterpreterServicesNeeded && isWitnessAttending.equals(NO)) {

                    // if no witnesses present nullify with dummies all witness-related fields
                    // (clearing does not work) for ccd show-conditions to work
                    clearWitnessIndividualFields(asylumCase);
                    clearWitnessInterpreterLanguageFields(asylumCase);

                    addAllWitnessFieldsToFieldsToBeCleared();
                    asylumCase.write(WITNESS_COUNT, 0);

                    log.info("fieldsToBeCleared: {}", fieldsToBeCleared);
                }
            }
            case APPELLANT_INTERPRETER_SPOKEN_LANGUAGE_PAGE_ID -> {
                log.info("WitnessInterpreterMidEventHandler on page: {}", APPELLANT_INTERPRETER_SPOKEN_LANGUAGE_PAGE_ID);

                // skip if this isn't the last page before "whichWitnessRequiresInterpreter" otherwise do the mapping
                // as explained in transferOldWitnessesToNewWitnesses()

                if (!appellantInterpreterLanguageCategory.contains(SIGN) && isWitnessAttending.equals(NO)) {

                    // if no witnesses present nullify with dummies all witness-related fields
                    // (clearing does not work) for ccd show-conditions to work
                    clearWitnessIndividualFields(asylumCase);
                    clearWitnessInterpreterLanguageFields(asylumCase);

                    addAllWitnessFieldsToFieldsToBeCleared();
                    asylumCase.write(WITNESS_COUNT, 0);

                    log.info("fieldsToBeCleared: {}", fieldsToBeCleared);
                }
            }
            case APPELLANT_INTERPRETER_SIGN_LANGUAGE_PAGE_ID -> {
                log.info("WitnessInterpreterMidEventHandler on page: {}", APPELLANT_INTERPRETER_SIGN_LANGUAGE_PAGE_ID);

                // skip if this isn't the last page before "whichWitnessRequiresInterpreter" otherwise do the mapping
                //as explained in transferOldWitnessesToNewWitnesses()

                if (isWitnessAttending.equals(NO)) {
                    // if no witnesses present nullify with dummies all witness-related fields
                    // (clearing does not work) for ccd show-conditions to work
                    clearWitnessIndividualFields(asylumCase);
                    clearWitnessInterpreterLanguageFields(asylumCase);

                    addAllWitnessFieldsToFieldsToBeCleared();
                    asylumCase.write(WITNESS_COUNT, 0);

                    log.info("fieldsToBeCleared: {}", fieldsToBeCleared);
                }

            }
            case IS_ANY_WITNESS_INTERPRETER_REQUIRED_PAGE_ID -> {
                log.info("WitnessInterpreterMidEventHandler on page: {}", IS_ANY_WITNESS_INTERPRETER_REQUIRED_PAGE_ID);

                // if this page is shown it's because isWitnessAttending=Yes and it'll certainly be the last
                // before "whichWitnessRequiresInterpreter"
                // so do the mapping as explained in transferOldWitnessesToNewWitnesses()

                boolean isAnyWitnessInterpreterRequired = asylumCase
                    .read(IS_ANY_WITNESS_INTERPRETER_REQUIRED, YesOrNo.class)
                    .map(yesOrNo -> yesOrNo.equals(YES))
                    .orElse(false);

                if (!isAnyWitnessInterpreterRequired) {
                    // if witness list empty or none need interpreter nullify with dummies all witness-related fields
                    // (clearing does not work) for ccd show-conditions to work
                    clearWitnessIndividualFields(asylumCase);
                    clearWitnessInterpreterLanguageFields(asylumCase);

                    addAllWitnessFieldsToFieldsToBeCleared();
                    asylumCase.write(WITNESS_COUNT, 0);

                    log.info("fieldsToBeCleared: {}", fieldsToBeCleared);
                } else {
                    transferOldWitnessesToNewWitnesses(
                        asylumCase, oldAsylumCase, witnesses, newToOldWitnessesIndexes);
                }
            }
            case WHICH_WITNESS_REQUIRES_INTERPRETER_PAGE_ID -> {
                log.info("WitnessInterpreterMidEventHandler on page: {}", WHICH_WITNESS_REQUIRES_INTERPRETER_PAGE_ID);

                /*
                this page reads the selections made on the UI and props up the dynamic lists (for interpreters)
                in forthcoming pages, populating empty/new dynamic lists for new witnesses or old dynamic lists
                with their original selection for preexisting witnesses
                 */

                Map<Integer, InterpreterLanguageRefData> existingSpokenSelections = new HashMap<>();
                Map<Integer, InterpreterLanguageRefData> existingSignSelections = new HashMap<>();
                Map<Integer, List<String>> witnessIndexToInterpreterNeeded = new HashMap<>();
                int numberOfWitnesses = optionalWitnesses.map(List::size).orElse(0);
                newToOldWitnessesIndexes.forEach((n, o) -> {
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
                        List<String> selectedInterpreterType = optionalSelectedInterpreterType
                            .orElse(Collections.emptyList());

                        witnessIndexToInterpreterNeeded.put(i, selectedInterpreterType);
                    }
                    i++;
                }
                if (witnessIndexToInterpreterNeeded.isEmpty()) {
                    response.addError(NO_WITNESSES_SELECTED_ERROR);
                }
                InterpreterLanguageRefData spokenLanguages = witnessInterpreterLanguagesDynamicListUpdater
                    .generateDynamicList(INTERPRETER_LANGUAGES);
                InterpreterLanguageRefData signLanguages = witnessInterpreterLanguagesDynamicListUpdater
                    .generateDynamicList(SIGN_LANGUAGES);
                witnessIndexToInterpreterNeeded.forEach((index, interpretersNeeded) -> {
                    interpretersNeeded.forEach(interpreterCategory -> {
                        if (Objects.equals(interpreterCategory, SPOKEN)) {
                            InterpreterLanguageRefData selection = existingSpokenSelections.get(index) != null
                                ? existingSpokenSelections.get(index)
                                : spokenLanguages;

                            // necessary to fill the dynamic dropdown list when the selection was manually entered
                            if (selection.getLanguageRefData() == null || selection.getLanguageRefData().getListItems() == null) {
                                selection.setLanguageRefData(spokenLanguages.getLanguageRefData());
                            }
                            asylumCase.write(WITNESS_N_INTERPRETER_SPOKEN_LANGUAGE.get(index), selection);
                        }
                        if (Objects.equals(interpreterCategory, SIGN)) {
                            InterpreterLanguageRefData selection = existingSignSelections.get(index) != null
                                ? existingSignSelections.get(index)
                                : signLanguages;

                            // necessary to fill the dynamic dropdown list when the selection was manually entered
                            if (selection.getLanguageRefData() == null || selection.getLanguageRefData().getListItems() == null) {
                                selection.setLanguageRefData(signLanguages.getLanguageRefData());
                            }
                            asylumCase.write(WITNESS_N_INTERPRETER_SIGN_LANGUAGE.get(index), selection);
                        }
                    });
                });

                /*
                reset this collection
                these are fields no longer relevant to any witness (either new or preexisting)
                clearing on the fly doesn't work with midEvents, so they're collected here and the wiped
                at the aboutToSubmit stage
                 */

                fieldsToBeCleared.clear();
                log.info("Clearing fieldsToBeCleared: {}", fieldsToBeCleared);

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
                            fieldsToBeCleared.add(WITNESS_N_INTERPRETER_SIGN_LANGUAGE.get(j));
                        }
                    }
                    j++;
                }
                log.info("fieldsToBeCleared after page {}: {}",
                    WHICH_WITNESS_REQUIRES_INTERPRETER_PAGE_ID,
                    fieldsToBeCleared);
            }
            default -> {
            }
        }

        return response;
    }

    /**
     * After deleting or adding witnesses, preexisting witnesses may take a different index. For example, if there are
     * four witnesses, if witness3 gets deleted, then, in the new witness list, witness4 will become witness3.
     * This map establishes the relation between the indexes of preexisting witnesses, so if witness3 gets deleted the
     * mapping will show 0-0, 1-1, 3-2 (three preexisting witnesses and their mapping between old and new index).
     * @param callback The callback.
     * @return A mapping between old and new index for each preexisting witness.
     */
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


    /**
     * Updating the witness list means adding or deleting witnesses and changing the index of each witness in
     * the new witness list, so it's necessary to identify if, for example, witness3 had been deleted. In that case,
     * witness4 becomes witness3, and witnessListElement4 should become witnessListElement3,
     * witness4InterpreterSpokenLanguage should become witness3InterpreterSpokenLanguage,
     * witness4InterpreterSignLanguage should become witness3InterpreterSignLanguage,
     * witness4InterpreterLanguageCategory should become witness3InterpreterLanguageCategory ETC (...).
     * @param asylumCase The asylumCase being updated, where the updated fields will be written to.
     * @param oldAsylumCase The asylumCase before starting the event, containing the old witness list before updating.
     * @param witnesses The list of witnesses updated in this event.
     * @param newToOldIndexes The mapping for each preexisting witness between its new updated index and its old index.
     */
    protected void transferOldWitnessesToNewWitnesses(AsylumCase asylumCase,
                                                      AsylumCase oldAsylumCase,
                                                      List<IdValue<WitnessDetails>> witnesses,
                                                      Map<Integer,Integer> newToOldIndexes) {
        log.info("transferOldWitnessesToNewWitnesses");

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
            // clearing does not work, dummy fields WITH EMPTY STRINGS have to be set and then hidden with ccd
            asylumCase.write(WITNESS_N_FIELD.get(i), new WitnessDetails("", ""));
            asylumCase.write(WITNESS_LIST_ELEMENT_N_FIELD.get(i), new DynamicMultiSelectList());
            i++;
        }
        asylumCase.write(WITNESS_COUNT, witnesses.size());
        log.info("Witness count: {}", witnesses.size());
    }

    private void addAllWitnessFieldsToFieldsToBeCleared() {
        fieldsToBeCleared.clear();
        fieldsToBeCleared.addAll(WITNESS_N_FIELD);
        fieldsToBeCleared.addAll(WITNESS_LIST_ELEMENT_N_FIELD);
        fieldsToBeCleared.addAll(WITNESS_N_INTERPRETER_CATEGORY_FIELD);
        fieldsToBeCleared.addAll(WITNESS_N_INTERPRETER_SPOKEN_LANGUAGE);
        fieldsToBeCleared.addAll(WITNESS_N_INTERPRETER_SIGN_LANGUAGE);
        fieldsToBeCleared.add(WITNESS_DETAILS);
        fieldsToBeCleared.add(WITNESS_DETAILS_READONLY);
    }

    public List<AsylumCaseFieldDefinition> getFieldsToBeCleared() {
        return fieldsToBeCleared;
    }

    public void setFieldsToBeCleared(List<AsylumCaseFieldDefinition> fieldsToBeCleared) {
        this.fieldsToBeCleared = fieldsToBeCleared;
    }
}
