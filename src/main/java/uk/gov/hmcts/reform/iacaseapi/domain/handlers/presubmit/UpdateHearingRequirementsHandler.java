package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.InterpreterLanguageCategory.SIGN_LANGUAGE_INTERPRETER;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.InterpreterLanguageCategory.SPOKEN_LANGUAGE_INTERPRETER;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.populateAppellantInterpreterLanguageFieldsIfRequired;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.PreviousRequirementsAndRequestsAppender;

@Component
public class UpdateHearingRequirementsHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final PreviousRequirementsAndRequestsAppender previousRequirementsAndRequestsAppender;
    private final FeatureToggler featureToggler;
    private static final String LIST_YES = "Yes";

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

        populateAppellantInterpreterLanguageFieldsIfRequired(asylumCase);

        ensureOnlySelectedLanguageCategoryIsSet(asylumCase);

        List<WitnessDetails> witnessDetails = asylumCase.<List<IdValue<WitnessDetails>>>read(WITNESS_DETAILS)
            .orElse(Collections.emptyList())
            .stream()
            .map(IdValue::getValue)
            .collect(Collectors.toList());

        asylumCase.write(WITNESS_COUNT, witnessDetails.size());

        asylumCase.write(DISABLE_OVERVIEW_PAGE, YesOrNo.YES);
        asylumCase.write(CURRENT_CASE_STATE_VISIBLE_TO_CASE_OFFICER, State.UNKNOWN);
        asylumCase.write(UPDATE_HEARING_REQUIREMENTS_EXISTS, YesOrNo.YES);

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

        if (asylumCase.read(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS, YesOrNo.class).map(flag -> flag.equals(YesOrNo.YES)).orElse(false)
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
            .map(yesOrNo -> Objects.equals(yesOrNo, YesOrNo.YES))
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

            sanitizeAppellantLanguageComplexType(asylumCase);
        }
    }

    private void sanitizeAppellantLanguageComplexType(AsylumCase asylumCase) {
        sanitizeInterpreterLanguageRefDataComplexType(asylumCase, APPELLANT_INTERPRETER_SPOKEN_LANGUAGE);
        sanitizeInterpreterLanguageRefDataComplexType(asylumCase, APPELLANT_INTERPRETER_SIGN_LANGUAGE);
    }

    private void sanitizeInterpreterLanguageRefDataComplexType(AsylumCase asylumCase, AsylumCaseFieldDefinition asylumCaseFieldDefinition) {
        InterpreterLanguageRefData sanitizedComplexType;
        Optional<InterpreterLanguageRefData> spoken =
            asylumCase.read(asylumCaseFieldDefinition, InterpreterLanguageRefData.class);

        if (spoken.isPresent()) {
            InterpreterLanguageRefData spokenComplexType = spoken.get();

            sanitizedComplexType = clearComplexTypeField(spokenComplexType);
            asylumCase.write(asylumCaseFieldDefinition, sanitizedComplexType);
        }
    }

    private InterpreterLanguageRefData clearComplexTypeField(InterpreterLanguageRefData languageComplexType) {
        if (languageComplexType.getLanguageManualEntry().isEmpty()
            && languageComplexType.getLanguageManualEntryDescription() != null) {

            languageComplexType.setLanguageManualEntryDescription(null);

        } else if (languageComplexType.getLanguageManualEntry().contains(LIST_YES)
            && languageComplexType.getLanguageRefData() != null) {

            languageComplexType.setLanguageRefData(null);
        }
        return languageComplexType;
    }
}

