package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.presubmit;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.InterpreterLanguageRefData;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.bailcaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.bailcaseapi.domain.utils.InterpreterLanguagesUtils;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.service.RefDataUserService;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event.*;

@Component
public class InterpreterLanguagesDynamicListUpdater implements PreSubmitCallbackHandler<BailCase> {

    public static final String INTERPRETER_LANGUAGES = "InterpreterLanguage";
    public static final String SIGN_LANGUAGES = "SignLanguage";
    private final RefDataUserService refDataUserService;

    public InterpreterLanguagesDynamicListUpdater(RefDataUserService refDataUserService) {
        this.refDataUserService = refDataUserService;
    }

    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<BailCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_START
            && Set.of(START_APPLICATION, MAKE_NEW_APPLICATION, EDIT_BAIL_APPLICATION_AFTER_SUBMIT, EDIT_BAIL_APPLICATION).contains(callback.getEvent());
    }

    @Override
    public PreSubmitCallbackResponse<BailCase> handle(PreSubmitCallbackStage callbackStage, Callback<BailCase> callback) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        BailCase bailCase = callback.getCaseDetails().getCaseData();

        BailCase bailCaseBefore = callback.getCaseDetailsBefore()
            .map(CaseDetails::getCaseData)
            .orElse(bailCase);

        List.of(APPLICANT_INTERPRETER_SPOKEN_LANGUAGE, FCS1_INTERPRETER_SPOKEN_LANGUAGE, FCS2_INTERPRETER_SPOKEN_LANGUAGE, FCS3_INTERPRETER_SPOKEN_LANGUAGE, FCS4_INTERPRETER_SPOKEN_LANGUAGE)
                .forEach(languageCategory -> populateDynamicList(bailCase, bailCaseBefore, INTERPRETER_LANGUAGES, languageCategory));

        List.of(APPLICANT_INTERPRETER_SIGN_LANGUAGE, FCS1_INTERPRETER_SIGN_LANGUAGE, FCS2_INTERPRETER_SIGN_LANGUAGE, FCS3_INTERPRETER_SIGN_LANGUAGE, FCS4_INTERPRETER_SIGN_LANGUAGE)
                .forEach(languageCategory -> populateDynamicList(bailCase, bailCaseBefore, SIGN_LANGUAGES, languageCategory));

        return new PreSubmitCallbackResponse<>(bailCase);
    }

    private void populateDynamicList(BailCase bailCase, BailCase bailCaseBefore, String languageCategory, BailCaseFieldDefinition languageCategoryDefinition) {
        if (bailCase
                .read(languageCategoryDefinition, InterpreterLanguageRefData.class)
                .map(applicantSpoken -> applicantSpoken.getLanguageRefData() == null)
                .orElse(true)) {
            InterpreterLanguageRefData interpreterLanguage = generateDynamicList(languageCategory);
            Optional<InterpreterLanguageRefData> optionalExistingInterpreterLanguageRefData = bailCaseBefore.read(languageCategoryDefinition);

            optionalExistingInterpreterLanguageRefData.ifPresent(existing -> {
                interpreterLanguage.setLanguageManualEntry(existing.getLanguageManualEntry());
                interpreterLanguage.setLanguageManualEntryDescription(existing.getLanguageManualEntryDescription());

            });
            bailCase.write(languageCategoryDefinition, interpreterLanguage);
        }
    }

    private InterpreterLanguageRefData generateDynamicList(String languageCategory) {
        return InterpreterLanguagesUtils.generateDynamicList(refDataUserService, languageCategory);
    }
}
