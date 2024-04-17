package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_INTERPRETER_SIGN_LANGUAGE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_INTERPRETER_SPOKEN_LANGUAGE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.DRAFT_HEARING_REQUIREMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.UPDATE_HEARING_REQUIREMENTS;

import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.InterpreterLanguageRefData;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.InterpreterLanguagesUtils;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.RefDataUserService;

@Component
public class AppellantInterpreterLanguagesDynamicListUpdater implements PreSubmitCallbackHandler<AsylumCase> {

    private final RefDataUserService refDataUserService;

    public AppellantInterpreterLanguagesDynamicListUpdater(RefDataUserService refDataUserService) {
        this.refDataUserService = refDataUserService;
    }

    public static final String INTERPRETER_LANGUAGES = "InterpreterLanguage";
    public static final String SIGN_LANGUAGES = "SignLanguage";
    public static final String IS_CHILD_REQUIRED = "Y";
    public static final String YES = "Yes";

    public boolean canHandle(
            PreSubmitCallbackStage callbackStage,
            Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_START
               && Set.of(DRAFT_HEARING_REQUIREMENTS, UPDATE_HEARING_REQUIREMENTS).contains(callback.getEvent());
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
            PreSubmitCallbackStage callbackStage,
            Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        AsylumCase asylumCaseBefore = callback.getCaseDetailsBefore()
            .map(CaseDetails::getCaseData)
            .orElse(asylumCase);

        boolean shouldPopulateSpokenLanguage = asylumCase
            .read(APPELLANT_INTERPRETER_SPOKEN_LANGUAGE, InterpreterLanguageRefData.class)
            .map(appellantSpoken -> appellantSpoken.getLanguageRefData() == null)
            .orElse(true);

        boolean shouldPopulateSignLanguage = asylumCase
            .read(APPELLANT_INTERPRETER_SIGN_LANGUAGE, InterpreterLanguageRefData.class)
            .map(appellantSign -> appellantSign.getLanguageRefData() == null)
            .orElse(true);

        // POPULATE APPELLANT INTERPRETER LANGUAGE DYNAMIC LIST FOR DRAFT_HEARING_REQUIREMENTS
        // POPULATE APPELLANT INTERPRETER LANGUAGE DYNAMIC LIST FOR UPDATE_HEARING_REQUIREMENTS_IF_NECESSARY

        if (shouldPopulateSpokenLanguage) {
            populateDynamicList(asylumCase, asylumCaseBefore, INTERPRETER_LANGUAGES, APPELLANT_INTERPRETER_SPOKEN_LANGUAGE);
        }
        if (shouldPopulateSignLanguage) {
            populateDynamicList(asylumCase, asylumCaseBefore, SIGN_LANGUAGES, APPELLANT_INTERPRETER_SIGN_LANGUAGE);
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
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
        return InterpreterLanguagesUtils.generateDynamicList(refDataUserService, languageCategory);
    }
}

