package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_ANY_WITNESS_INTERPRETER_REQUIRED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.InterpreterLanguageCategory.SIGN_LANGUAGE_INTERPRETER;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.InterpreterLanguageCategory.SPOKEN_LANGUAGE_INTERPRETER;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.UPDATE_HEARING_REQUIREMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.MID_EVENT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.InterpreterLanguagesUtils.WITNESS_N_FIELD;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.InterpreterLanguagesUtils.WITNESS_N_INTERPRETER_CATEGORY_FIELD;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.InterpreterLanguagesUtils.WITNESS_N_INTERPRETER_SIGN_LANGUAGE;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.InterpreterLanguagesUtils.WITNESS_N_INTERPRETER_SPOKEN_LANGUAGE;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.InterpreterLanguagesUtils.clearWitnessIndividualFields;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.InterpreterLanguagesUtils.clearWitnessInterpreterLanguageFields;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.WitnessInterpreterLanguagesDynamicListUpdater.INTERPRETER_LANGUAGES;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.WitnessInterpreterLanguagesDynamicListUpdater.SIGN_LANGUAGES;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.InterpreterLanguageRefData;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.WitnessDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.PartyIdService;

@Slf4j
@Component
public class WitnessesUpdateMidEventHandler extends WitnessHandler
    implements PreSubmitCallbackHandler<AsylumCase> {

    private static final String MANUAL_LANGUAGE_YES = "Yes";
    private static final String IS_WITNESSES_ATTENDING_PAGE_ID = "isWitnessesAttending";
    private static final String WHICH_WITNESS_REQUIRES_INTERPRETER_PAGE_ID = "whichWitnessRequiresInterpreter";
    private static final String IS_ANY_WITNESS_INTERPRETER_REQUIRED_PAGE_ID = "isAnyWitnessInterpreterRequired";
    private static final String NO_WITNESSES_SELECTED_ERROR = "Select at least one witness interpreter requirement";
    private static final String TOTAL_NUMBER_OF_WITNESSES_EXCEEDED = "Total number of witnesses being "
                                                                     + "handled cannot be higher than 10. ";
    private static final String DELEDED_WITNESSES = "Number of removed witnesses: ";
    private static final String ACTIVE_WITNESSES = "Number of active witnesses: ";
    private static final String DELETE_FIRST = "It's advised to only remove witnesses in this update and then "
                                               + "add new ones in another update.";
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
        Set<String> enabledPages = Set.of(IS_WITNESSES_ATTENDING_PAGE_ID,
            WHICH_WITNESS_REQUIRES_INTERPRETER_PAGE_ID,
            IS_ANY_WITNESS_INTERPRETER_REQUIRED_PAGE_ID);

        return (callbackStage == MID_EVENT
                && callback.getEvent().equals(UPDATE_HEARING_REQUIREMENTS)
                && enabledPages.contains(pageId));

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

        // append witness party ID if missing
        PartyIdService.appendWitnessPartyId(asylumCase);

        AsylumCase oldAsylumCase = callback.getCaseDetailsBefore().orElse(callback.getCaseDetails()).getCaseData();

        List<IdValue<WitnessDetails>> inclusiveWitnessDetails =
            buildInclusiveWitnessDetailsList(asylumCase, oldAsylumCase);

        int deletedWitnesses = deletedWitnesses(inclusiveWitnessDetails).size();

        switch (callback.getPageId()) {
            case IS_WITNESSES_ATTENDING_PAGE_ID -> {

                // add error to the page where witnesses are added to prevent more than 10 witnesses
                // being added

                if (inclusiveWitnessDetails.isEmpty()) {
                    clearWitnessIndividualFields(asylumCase);
                    clearWitnessInterpreterLanguageFields(asylumCase);
                } else if (inclusiveWitnessDetails.size() > 10) { // 10
                    // cannot add more than 10 witnesses to the collection
                    response.addError(TOTAL_NUMBER_OF_WITNESSES_EXCEEDED
                                      + DELEDED_WITNESSES + deletedWitnesses + ". "
                                      + ACTIVE_WITNESSES + (inclusiveWitnessDetails.size() - deletedWitnesses) + ". "
                                      + DELETE_FIRST);
                }
            }
            case IS_ANY_WITNESS_INTERPRETER_REQUIRED_PAGE_ID -> {

                // if this page is shown it's because isWitnessAttending=Yes
                // witness individual fields need to be set up for elements to show correctly
                // on the following page "whichWitnessRequiresInterpreter"

                boolean isAnyWitnessInterpreterRequired = asylumCase
                    .read(IS_ANY_WITNESS_INTERPRETER_REQUIRED, YesOrNo.class)
                    .map(yesOrNo -> Objects.equals(yesOrNo, YES))
                    .orElse(false);

                if (isAnyWitnessInterpreterRequired) {
                    writeIndividualWitnessFields(asylumCase, inclusiveWitnessDetails);
                }
            }

            case WHICH_WITNESS_REQUIRES_INTERPRETER_PAGE_ID -> {

                if (noCategorySelectionMadeForActiveWitnesses(inclusiveWitnessDetails, asylumCase)) {
                    response.addError(NO_WITNESSES_SELECTED_ERROR);

                } else {

                    int lowestIndexOfNewlyAddedWitnesses = inclusiveWitnessDetails.size() - deletedWitnesses;

                    InterpreterLanguageRefData spokenLanguages = witnessInterpreterLanguagesDynamicListUpdater
                        .generateDynamicList(INTERPRETER_LANGUAGES);
                    InterpreterLanguageRefData signLanguages = witnessInterpreterLanguagesDynamicListUpdater
                        .generateDynamicList(SIGN_LANGUAGES);

                    int i = 0;
                    while (i < inclusiveWitnessDetails.size()) {

                        List<String> witnessInterpreterCategories = asylumCase
                            .<List<String>>read(WITNESS_N_INTERPRETER_CATEGORY_FIELD.get(i))
                            .orElse(Collections.emptyList());

                        boolean witnessIsNewlyAdded = i >= lowestIndexOfNewlyAddedWitnesses;

                        if (!isWitnessDeleted(inclusiveWitnessDetails.get(i))) {
                            if (witnessInterpreterCategories.contains(SPOKEN)) {

                                Optional<InterpreterLanguageRefData> optionalLanguage = oldAsylumCase
                                    .read(WITNESS_N_INTERPRETER_SPOKEN_LANGUAGE.get(i));

                                if (witnessIsNewlyAdded
                                    || optionalLanguage.isEmpty()
                                    || interpreterLanguageIsNull(optionalLanguage.get())) {

                                    asylumCase.write(WITNESS_N_INTERPRETER_SPOKEN_LANGUAGE.get(i), spokenLanguages);

                                } else if (existingSelectionWasManual(oldAsylumCase, i, SPOKEN)) {

                                    Optional<InterpreterLanguageRefData> existingSelection =
                                        getExistingSpokenSelection(oldAsylumCase, i);

                                    if (existingSelection.isPresent()
                                        && null == existingSelection.get().getLanguageRefData()) {

                                        existingSelection.get()
                                            .setLanguageRefData(spokenLanguages.getLanguageRefData());
                                        asylumCase.write(WITNESS_N_INTERPRETER_SPOKEN_LANGUAGE.get(i),
                                            existingSelection);
                                    }
                                }
                            }
                            if (witnessInterpreterCategories.contains(SIGN)) {

                                Optional<InterpreterLanguageRefData> optionalLanguage = oldAsylumCase
                                    .read(WITNESS_N_INTERPRETER_SIGN_LANGUAGE.get(i));

                                if (witnessIsNewlyAdded
                                    || optionalLanguage.isEmpty()
                                    || interpreterLanguageIsNull(optionalLanguage.get())) {

                                    asylumCase.write(WITNESS_N_INTERPRETER_SIGN_LANGUAGE.get(i), signLanguages);

                                } else if (existingSelectionWasManual(oldAsylumCase, i, SIGN)) {

                                    Optional<InterpreterLanguageRefData> existingSelection =
                                        getExistingSignSelection(oldAsylumCase, i);
                                    if (existingSelection.isPresent()
                                        && null == existingSelection.get().getLanguageRefData()) {

                                        existingSelection.get()
                                            .setLanguageRefData(signLanguages.getLanguageRefData());
                                        asylumCase.write(WITNESS_N_INTERPRETER_SIGN_LANGUAGE.get(i),
                                            existingSelection);
                                    }
                                }
                            }
                        }
                        i++;
                    }
                }
            }

            default -> {
            }
        }

        return response;
    }

    private boolean interpreterLanguageIsNull(InterpreterLanguageRefData interpreterLanguageRefData) {
        return null == interpreterLanguageRefData.getLanguageRefData()
               && null == interpreterLanguageRefData.getLanguageManualEntryDescription();
    }

    private void writeIndividualWitnessFields(AsylumCase asylumCase,
                                              List<IdValue<WitnessDetails>> inclusiveWitnessCollection) {
        int i = 0;
        while (i < inclusiveWitnessCollection.size()) {

            WitnessDetails witness = inclusiveWitnessCollection.get(i).getValue();
            asylumCase.write(WITNESS_N_FIELD.get(i), witness);

            i++;
        }
        while (i < 10) {
            asylumCase.clear(WITNESS_N_FIELD.get(i));
            i++;
        }
    }

    private boolean noCategorySelectionMadeForActiveWitnesses(List<IdValue<WitnessDetails>> inclusiveWitnessList,
                                                              AsylumCase asylumCase) {
        boolean noSelectionMade = true;

        int i = 0;
        while (i < inclusiveWitnessList.size()) {

            boolean isWitnessDeleted = isWitnessDeleted(inclusiveWitnessList.get(i));
            boolean hasCategory = !asylumCase.<List<String>>read(WITNESS_N_INTERPRETER_CATEGORY_FIELD.get(i))
                .orElse(Collections.emptyList()).isEmpty();

            if (!isWitnessDeleted && hasCategory) {
                noSelectionMade = false;
            }

            i++;
        }

        return noSelectionMade;
    }

    private Optional<InterpreterLanguageRefData> getExistingSpokenSelection(AsylumCase oldAsylumCase, int i) {
        return oldAsylumCase
            .read(WITNESS_N_INTERPRETER_SPOKEN_LANGUAGE.get(i), InterpreterLanguageRefData.class);
    }

    private Optional<InterpreterLanguageRefData> getExistingSignSelection(AsylumCase oldAsylumCase, int i) {
        return oldAsylumCase
            .read(WITNESS_N_INTERPRETER_SIGN_LANGUAGE.get(i), InterpreterLanguageRefData.class);
    }

    private boolean existingSelectionWasManual(AsylumCase oldAsylumCase, int i, String category) {

        Optional<InterpreterLanguageRefData> oldLanguageField = Optional.empty();

        if (Objects.equals(category, SPOKEN)) {
            oldLanguageField = getExistingSpokenSelection(oldAsylumCase, i);
        }
        if (Objects.equals(category, SIGN)) {
            oldLanguageField = getExistingSignSelection(oldAsylumCase, i);
        }

        return oldLanguageField.isPresent()
               && oldLanguageField.get().getLanguageManualEntry() != null
               && oldLanguageField.get().getLanguageManualEntry().contains(MANUAL_LANGUAGE_YES);
    }

}
