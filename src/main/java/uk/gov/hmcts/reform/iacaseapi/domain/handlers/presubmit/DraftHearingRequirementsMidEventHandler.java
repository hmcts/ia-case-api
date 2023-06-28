package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.INTERPRETER_LANGUAGE_REF_DATA;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.InterpreterLanguageRefData;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Value;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.RefDataUserService;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.dto.hearingdetails.CategoryValues;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.dto.hearingdetails.CommonDataResponse;

@Component
public class DraftHearingRequirementsMidEventHandler implements PreSubmitCallbackHandler<AsylumCase> {

    RefDataUserService refDataUserService;

    public DraftHearingRequirementsMidEventHandler(RefDataUserService refDataUserService) {
        this.refDataUserService = refDataUserService;
    }

    public static final String INTERPRETER_LANGUAGES = "InterpreterLanguage";
    public static final String IS_CHILD_REQUIRED = "Y";
    public static final String DRAFT_HEARING_REQUIREMENTS_PAGE_ID = "draftHearingRequirements";
    public static final String YES = "Yes";

    public boolean canHandle(
            PreSubmitCallbackStage callbackStage,
            Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.MID_EVENT
               && (callback.getEvent() == Event.DRAFT_HEARING_REQUIREMENTS
                   || callback.getEvent() == Event.UPDATE_HEARING_REQUIREMENTS);
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

        AsylumCase asylumCaseBefore =
            callback
                .getCaseDetailsBefore()
                .map(CaseDetails::getCaseData)
                .orElse(null);

        String pageId = callback.getPageId();

        Optional<List<IdValue<InterpreterLanguageRefData>>> optionalInterpreterLanguage =
            asylumCase.read(INTERPRETER_LANGUAGE_REF_DATA);

        Optional<List<IdValue<InterpreterLanguageRefData>>> optionalInterpreterLanguageBefore =
            asylumCaseBefore != null
                ? asylumCaseBefore.read(INTERPRETER_LANGUAGE_REF_DATA)
                : Optional.empty();

        if (Objects.equals(pageId, DRAFT_HEARING_REQUIREMENTS_PAGE_ID)
            && !isDynamicListPopulated(optionalInterpreterLanguage)
            && !isDynamicListPopulated(optionalInterpreterLanguageBefore)) {

            populateDynamicList(asylumCase);
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private AsylumCase populateDynamicList(AsylumCase asylumCase) {
        List<CategoryValues> languages;
        DynamicList dynamicListOfLanguages;

        try {
            CommonDataResponse commonDataResponse = refDataUserService.retrieveCategoryValues(
                INTERPRETER_LANGUAGES,
                IS_CHILD_REQUIRED
            );

            languages = refDataUserService.filterCategoryValuesByCategoryId(commonDataResponse, INTERPRETER_LANGUAGES);

            dynamicListOfLanguages = new DynamicList(new Value("", ""),
                refDataUserService.mapCategoryValuesToDynamicListValues(languages));

        } catch (Exception e) {
            throw new RuntimeException("Couldn't read response by RefData service for InterpreterLanguage(s)", e);
        }

        InterpreterLanguageRefData interpreterLanguageObject = new InterpreterLanguageRefData(
            dynamicListOfLanguages,
            Collections.emptyList(),
            "");

        List<IdValue<InterpreterLanguageRefData>> interpreterLanguageCollection = List.of(
            new IdValue<>("1", interpreterLanguageObject)
        );

        asylumCase.write(INTERPRETER_LANGUAGE_REF_DATA, interpreterLanguageCollection);

        return asylumCase;
    }

    private boolean isDynamicListPopulated(Optional<List<IdValue<InterpreterLanguageRefData>>> optionalInterpreterLanguage) {
        if (optionalInterpreterLanguage.isPresent() && !optionalInterpreterLanguage.get().isEmpty()) {
            return !optionalInterpreterLanguage.get()
                .get(0)
                .getValue()
                .getInterpreterLanguageRd()
                .getListItems()
                .isEmpty();
        } else {
            return false;
        }
    }
}

