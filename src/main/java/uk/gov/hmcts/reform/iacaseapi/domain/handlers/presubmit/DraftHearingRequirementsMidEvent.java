package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.INTERPRETER_LANGUAGE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.INTERPRETER_LANGUAGE_READONLY;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DynamicListElement;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.InterpreterLanguage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.RefDataUserService;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.dto.hearingdetails.CommonDataResponse;


@Component
public class DraftHearingRequirementsMidEvent implements PreSubmitCallbackHandler<AsylumCase> {

    @Autowired
    RefDataUserService refDataUserService;

    public static final String INTERPRETER_LANGUAGES = "InterpreterLanguage";
    public static final String IS_HEARINGCHILDREQUIRED_N = "N";

    public DraftHearingRequirementsMidEvent() { }

    public boolean canHandle(
            PreSubmitCallbackStage callbackStage,
            Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.MID_EVENT
                && callback.getEvent() == Event.DRAFT_HEARING_REQUIREMENTS;
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
            PreSubmitCallbackStage callbackStage,
            Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final AsylumCase asylumCase =
                callback
                        .getCaseDetails()
                        .getCaseData();

        Map<String, List<DynamicListElement>> values = new HashMap<>();

        try {
            CommonDataResponse commonDataResponse = refDataUserService.retrieveCategoryValues(
                    INTERPRETER_LANGUAGES,
                    IS_HEARINGCHILDREQUIRED_N
            );

            values.put(INTERPRETER_LANGUAGES, refDataUserService.filterCategoryValuesByCategoryId(
                    commonDataResponse, INTERPRETER_LANGUAGES));
        } catch (Exception e) {
            throw new RuntimeException("Couldn't read response", e);
        }

        //INTERPRETER_LANGUAGE should be empty at this point
        Optional<List<IdValue<InterpreterLanguage>>> interpreterLanguage = asylumCase.read(INTERPRETER_LANGUAGE);

        //We overwrite the available languages in INTERPRETER_LANGUAGE.languages
        interpreterLanguage.ifPresent(idValues -> asylumCase.write(INTERPRETER_LANGUAGE, idValues
                .stream()
                .map(i ->
                        "Language\t\t\t" + i.getValue().getLanguage()
                                + "\nLanguages\t\t\t" + values
                                + "\nDialect\t\t\t" + i.getValue().getLanguageDialect()
                                + "\nLanguageManualEnter\t\t\t" + i.getValue().getLanguageManualEnter()
                                + "\nLanguageDescription\t\t\t" + i.getValue().getLanguageDescription() + "\n")
                .collect(Collectors.joining("\n"))));

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}

