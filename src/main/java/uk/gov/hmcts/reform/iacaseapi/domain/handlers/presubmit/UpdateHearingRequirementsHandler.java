package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.InterpreterLanguageCategory.SIGN_LANGUAGE_INTERPRETER;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.InterpreterLanguageCategory.SPOKEN_LANGUAGE_INTERPRETER;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.InterpreterLanguagesUtils.WITNESS_N_FIELD;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.InterpreterLanguagesUtils.WITNESS_N_INTERPRETER_CATEGORY_FIELD;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.InterpreterLanguagesUtils.WITNESS_N_INTERPRETER_SIGN_LANGUAGE;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.InterpreterLanguagesUtils.WITNESS_N_INTERPRETER_SPOKEN_LANGUAGE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Application;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ApplicationType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.InterpreterLanguageRefData;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.WitnessDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.InterpreterLanguagesUtils;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.PreviousRequirementsAndRequestsAppender;

@Component
public class UpdateHearingRequirementsHandler extends WitnessHandler
    implements PreSubmitCallbackHandler<AsylumCase> {

    private final PreviousRequirementsAndRequestsAppender previousRequirementsAndRequestsAppender;
    private final FeatureToggler featureToggler;

    public UpdateHearingRequirementsHandler(
        PreviousRequirementsAndRequestsAppender previousRequirementsAndRequestsAppender,
        FeatureToggler featureToggler
    ) {
        this.previousRequirementsAndRequestsAppender = previousRequirementsAndRequestsAppender;
        this.featureToggler = featureToggler;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && callback.getEvent() == Event.UPDATE_HEARING_REQUIREMENTS;
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
            .orElseThrow(() -> new IllegalStateException("Could not retrieve previous case data")).getCaseData();

        ensureOnlySelectedLanguageCategoryIsSet(asylumCase);

        List<IdValue<WitnessDetails>> inclusiveWitnessDetails = buildInclusiveWitnessDetailsList(asylumCase, asylumCaseBefore);

        List<IdValue<WitnessDetails>> nonDeletedWitnesses = inclusiveWitnessDetails.stream()
            .filter(idValue -> !Objects.equals(idValue.getValue().getIsWitnessDeleted(), YES)).toList();

        YesOrNo isWitnessAttending = asylumCase.read(IS_WITNESSES_ATTENDING, YesOrNo.class).orElse(NO);
        YesOrNo isAnyWitnessInterpreterRequired = asylumCase.read(IS_ANY_WITNESS_INTERPRETER_REQUIRED, YesOrNo.class)
            .orElse(NO);

        if (nonDeletedWitnesses.isEmpty() || isWitnessAttending.equals(NO)) {
            clearWitnessRelatedFields(asylumCase);
            clearWitnessFields(asylumCase);
            asylumCase.clear(WITNESS_DETAILS_READONLY);
            asylumCase.write(WITNESS_COUNT, 0);
            asylumCase.write(IS_ANY_WITNESS_INTERPRETER_REQUIRED, NO);
            asylumCase.clear(WITNESS_DETAILS);

        } else if (isAnyWitnessInterpreterRequired.equals(NO)) {

            clearWitnessRelatedFields(asylumCase);
            filterOutDeletedWitnessesAndCompress(nonDeletedWitnesses, asylumCase);

        } else {

            filterOutDeletedFieldsAndCompress(inclusiveWitnessDetails, asylumCase);
            filterOutDeletedWitnessesAndCompress(nonDeletedWitnesses, asylumCase);
            clearLanguagesAccordingToCategories(asylumCase);
            InterpreterLanguagesUtils.sanitizeWitnessLanguageComplexType(asylumCase);
        }

        final YesOrNo datesToAvoidYesNo = asylumCase.read(DATES_TO_AVOID_YES_NO, YesOrNo.class)
                .orElse(YesOrNo.NO);
        if (datesToAvoidYesNo == YesOrNo.NO) {
            asylumCase.clear(DATES_TO_AVOID);
        }

        asylumCase.write(DISABLE_OVERVIEW_PAGE, YES);
        asylumCase.write(CURRENT_CASE_STATE_VISIBLE_TO_CASE_OFFICER, State.UNKNOWN);
        asylumCase.write(UPDATE_HEARING_REQUIREMENTS_EXISTS, YES);

        changeUpdateHearingsApplicationsToCompleted(asylumCase);
        asylumCase.clear(APPLICATION_UPDATE_HEARING_REQUIREMENTS_EXISTS);

        // we need to clear this to reset the agreed adjustments
        asylumCase.clear(REVIEWED_HEARING_REQUIREMENTS);

        // Clear review fields once the update happens
        asylumCase.clear(VULNERABILITIES_TRIBUNAL_RESPONSE);
        asylumCase.clear(REMOTE_VIDEO_CALL_TRIBUNAL_RESPONSE);
        asylumCase.clear(MULTIMEDIA_TRIBUNAL_RESPONSE);
        asylumCase.clear(SINGLE_SEX_COURT_TRIBUNAL_RESPONSE);
        asylumCase.clear(IN_CAMERA_COURT_TRIBUNAL_RESPONSE);
        asylumCase.clear(ADDITIONAL_TRIBUNAL_RESPONSE);

        // Clear review decision display fields once the update happens
        asylumCase.clear(VULNERABILITIES_DECISION_FOR_DISPLAY);
        asylumCase.clear(REMOTE_HEARING_DECISION_FOR_DISPLAY);
        asylumCase.clear(MULTIMEDIA_DECISION_FOR_DISPLAY);
        asylumCase.clear(SINGLE_SEX_COURT_DECISION_FOR_DISPLAY);
        asylumCase.clear(IN_CAMERA_COURT_DECISION_FOR_DISPLAY);
        asylumCase.clear(OTHER_DECISION_FOR_DISPLAY);

        if (asylumCase.read(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS, YesOrNo.class).map(flag -> flag.equals(YES)).orElse(false)
            && featureToggler.getValue("reheard-feature", false)) {
            previousRequirementsAndRequestsAppender.appendAndTrim(asylumCase);
        }

        boolean isAcceleratedDetainedAppeal = HandlerUtils.isAcceleratedDetainedAppeal(asylumCase);

        if (isAcceleratedDetainedAppeal) {
            //Set flag to Yes to enable updateHearingAdjustmentsEvent for ada cases
            asylumCase.write(ADA_HEARING_ADJUSTMENTS_UPDATABLE, YesOrNo.YES);
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private void clearLanguagesAccordingToCategories(AsylumCase asylumCase) {
        int i = 0;
        while (i < 10) {
            Optional<List<String>> categoriesOpt = asylumCase.read(WITNESS_N_INTERPRETER_CATEGORY_FIELD.get(i));
            List<String> categories = categoriesOpt.orElse(Collections.emptyList());

            if (!categories.contains(SPOKEN_LANGUAGE_INTERPRETER.getValue())) {
                asylumCase.clear(WITNESS_N_INTERPRETER_SPOKEN_LANGUAGE.get(i));
            }
            if (!categories.contains(SIGN_LANGUAGE_INTERPRETER.getValue())) {
                asylumCase.clear(WITNESS_N_INTERPRETER_SIGN_LANGUAGE.get(i));
            }
            i++;
        }
    }

    private void changeUpdateHearingsApplicationsToCompleted(AsylumCase asylumCase) {
        asylumCase.write(APPLICATIONS, asylumCase.<List<IdValue<Application>>>read(APPLICATIONS)
            .orElse(emptyList())
            .stream()
            .map(application -> {
                String applicationType = application.getValue().getApplicationType();
                if (ApplicationType.UPDATE_HEARING_REQUIREMENTS.toString().equals(applicationType)) {

                    return new IdValue<>(application.getId(), new Application(
                        application.getValue().getApplicationDocuments(),
                        application.getValue().getApplicationSupplier(),
                        applicationType,
                        application.getValue().getApplicationReason(),
                        application.getValue().getApplicationDate(),
                        application.getValue().getApplicationDecision(),
                        application.getValue().getApplicationDecisionReason(),
                        application.getValue().getApplicationDateOfDecision(),
                        "Completed"
                    ));
                }

                return application;
            })
            .collect(Collectors.toList())
        );
    }

    private void ensureOnlySelectedLanguageCategoryIsSet(AsylumCase asylumCase) {
        boolean isInterpreterServicesNeeded = asylumCase
            .read(IS_INTERPRETER_SERVICES_NEEDED, YesOrNo.class)
            .map(yesOrNo -> Objects.equals(yesOrNo, YES))
            .orElse(false);

        if (!isInterpreterServicesNeeded) {
            asylumCase.clear(APPELLANT_INTERPRETER_LANGUAGE_CATEGORY);
            asylumCase.clear(APPELLANT_INTERPRETER_SPOKEN_LANGUAGE);
            asylumCase.clear(APPELLANT_INTERPRETER_SIGN_LANGUAGE);
        } else {
            Optional<List<String>> languageCategoriesOptional = asylumCase
                .read(APPELLANT_INTERPRETER_LANGUAGE_CATEGORY);
            if (languageCategoriesOptional.isPresent()) {
                List<String> languageCategories = languageCategoriesOptional.get();
                Optional<InterpreterLanguageRefData> appellantInterpreterSpokenLanguage = asylumCase.read(APPELLANT_INTERPRETER_SPOKEN_LANGUAGE);
                Optional<InterpreterLanguageRefData> appellantInterpreterSignLanguage = asylumCase.read(APPELLANT_INTERPRETER_SIGN_LANGUAGE);

                if (appellantInterpreterSpokenLanguage.isPresent()
                    && !languageCategories.contains(SPOKEN_LANGUAGE_INTERPRETER.getValue())) {
                    asylumCase.clear(APPELLANT_INTERPRETER_SPOKEN_LANGUAGE);
                }

                if (appellantInterpreterSignLanguage.isPresent()
                    && !languageCategories.contains(SIGN_LANGUAGE_INTERPRETER.getValue())) {
                    asylumCase.clear(APPELLANT_INTERPRETER_SIGN_LANGUAGE);
                }
            }

            InterpreterLanguagesUtils.sanitizeAppellantLanguageComplexType(asylumCase);
        }
    }

    private void clearWitnessRelatedFields(AsylumCase asylumCase) {
        // to be called if the witness collection is updated to empty
        clearWitnessRelatedFieldsAfterIndex(0, asylumCase);
    }

    private void clearWitnessRelatedFieldsAfterIndex(int index, AsylumCase asylumCase) {
        while (index < 10) {
            asylumCase.write(WITNESS_N_INTERPRETER_CATEGORY_FIELD.get(index), Collections.emptyList());
            asylumCase.clear(WITNESS_N_INTERPRETER_SPOKEN_LANGUAGE.get(index));
            asylumCase.clear(WITNESS_N_INTERPRETER_SIGN_LANGUAGE.get(index));
            index++;
        }
    }

    private void clearWitnessFields(AsylumCase asylumCase) {
        // to be called if the witness collection is updated to empty
        clearWitnessFieldsAfterIndex(0, asylumCase);
    }

    private void clearWitnessFieldsAfterIndex(int index, AsylumCase asylumCase) {
        while (index < 10) {
            asylumCase.clear(WITNESS_N_FIELD.get(index));
            index++;
        }
    }

    /**
     * Filter out fields related to deleted witnesses (witnessInterpreterLanguageCategory,
     * witnessSpokenLanguageInterpreter, witnessSignLanguageInterpreter) and transpose them from top to bottom,
     * so that, out of 4 witnesses, if the third is deleted, the fields related to witness4 are transcribed onto
     * the fields for witness3.
     * @param inclusiveWitnessDetails List of witnesses inclusive of deleted, non-deleted old and new witnesses.
     * @param asylumCase The case data.
     */
    private void filterOutDeletedFieldsAndCompress(List<IdValue<WitnessDetails>> inclusiveWitnessDetails,
                                                  AsylumCase asylumCase) {

        int i = 0;
        int j = 0;
        while (i < inclusiveWitnessDetails.size()) {
            if (!isWitnessDeleted(inclusiveWitnessDetails.get(i))) {
                asylumCase.write(WITNESS_N_INTERPRETER_CATEGORY_FIELD.get(j),
                    asylumCase.read(WITNESS_N_INTERPRETER_CATEGORY_FIELD.get(i)).orElse(Collections.emptyList()));
                asylumCase.write(WITNESS_N_INTERPRETER_SPOKEN_LANGUAGE.get(j),
                    asylumCase.read(WITNESS_N_INTERPRETER_SPOKEN_LANGUAGE.get(i)).orElse(null));
                asylumCase.write(WITNESS_N_INTERPRETER_SIGN_LANGUAGE.get(j),
                    asylumCase.read(WITNESS_N_INTERPRETER_SIGN_LANGUAGE.get(i)).orElse(null));
                j++;
            }
            i++;
        }
        while (j < 10) {
            asylumCase.write(WITNESS_N_INTERPRETER_CATEGORY_FIELD.get(j), Collections.emptyList());
            asylumCase.write(WITNESS_N_INTERPRETER_SPOKEN_LANGUAGE.get(j), null);
            asylumCase.write(WITNESS_N_INTERPRETER_SIGN_LANGUAGE.get(j), null);
            j++;
        }
    }

    /*
    witness1 deleted=NO  -> witness1 (used to be witness1)
    witness2 deleted=NO  -> witness2 (used to be witness2)
    witness3 deleted=YES -> witness3 (used to be witness5)
    witness4 deleted=YES -> witness4 (used to be witness6)
    witness5 deleted=NO  -> witness5 (used to be witness7)
    witness6 deleted=NO
    witness7 deleted=NO
     */
    /**
     * Filter out witness individual fields (witness1, witness2 etc.) and transpose them from top to bottom,
     * so that, out of (e.g.) 7 witnesses, if witness3 and witness4 are deleted, witness5 becomes witness3,
     * witness6 becomes witness5, witness 7 becomes witness6.
     * @param nonDeletedWitnesses List of non-deleted old and new witnesses.
     * @param asylumCase The case data.
     */
    private void filterOutDeletedWitnessesAndCompress(List<IdValue<WitnessDetails>> nonDeletedWitnesses,
                                                      AsylumCase asylumCase) {

        List<IdValue<WitnessDetails>> reindexedNonDeletedWitnesses = new ArrayList<>();
        int k = 0;
        while (k < nonDeletedWitnesses.size()) {
            IdValue<WitnessDetails> reindexedWitness = new IdValue<>(
                String.valueOf(k + 1),
                nonDeletedWitnesses.get(k).getValue()
            );
            reindexedNonDeletedWitnesses.add(reindexedWitness);
            asylumCase.write(WITNESS_N_FIELD.get(k), reindexedWitness.getValue());
            k++;
        }
        asylumCase.write(WITNESS_DETAILS, reindexedNonDeletedWitnesses);
        asylumCase.write(WITNESS_COUNT, reindexedNonDeletedWitnesses.size());
        while (k < 10) {
            asylumCase.clear(WITNESS_N_FIELD.get(k));
            k++;
        }
    }

}

