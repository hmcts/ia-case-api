package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_DETAILS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.DRAFT_HEARING_REQUIREMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.UPDATE_HEARING_REQUIREMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.InterpreterLanguagesUtils.WITNESS_LIST_ELEMENT_N_FIELD;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.InterpreterLanguagesUtils.WITNESS_N_FIELD;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.InterpreterLanguagesUtils.WITNESS_N_INTERPRETER_CATEGORY_FIELD;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.InterpreterLanguagesUtils.WITNESS_N_INTERPRETER_SIGN_LANGUAGE;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.InterpreterLanguagesUtils.WITNESS_N_INTERPRETER_SPOKEN_LANGUAGE;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DynamicMultiSelectList;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.InterpreterLanguageRefData;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Value;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.WitnessDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

@Component
public class WitnessesMidEventHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private static final String IS_WITNESSES_ATTENDING_PAGE_ID = "isWitnessesAttending";
    private static final String IS_ANY_WITNESS_INTERPRETER_REQUIRED_PAGE_ID = "isAnyWitnessInterpreterRequired";
    private static final String WITNESSES_NUMBER_EXCEEDED_ERROR = "Maximum number of witnesses is 10";

    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        String pageId = callback.getPageId();

        return callbackStage == PreSubmitCallbackStage.MID_EVENT
               && Set.of(DRAFT_HEARING_REQUIREMENTS, UPDATE_HEARING_REQUIREMENTS).contains(callback.getEvent())
               && Set.of(IS_WITNESSES_ATTENDING_PAGE_ID, IS_ANY_WITNESS_INTERPRETER_REQUIRED_PAGE_ID).contains(pageId);
    }

    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        String pageId = callback.getPageId();
        PreSubmitCallbackResponse<AsylumCase> response = new PreSubmitCallbackResponse<>(asylumCase);

        Optional<List<IdValue<WitnessDetails>>> optionalWitnesses = asylumCase.read(WITNESS_DETAILS);

        switch (pageId) {
            case IS_WITNESSES_ATTENDING_PAGE_ID:

                optionalWitnesses.ifPresentOrElse(witnesses -> {
                    if (witnesses.size() > WITNESS_N_FIELD.size()) {        // 10
                        response.addError(WITNESSES_NUMBER_EXCEEDED_ERROR);
                    }
                }, () -> {
                    // if no witnesses present nullify with dummies all witness-related fields (clearing does not work)
                    clearWitnessFieldsPreemptively(asylumCase);
                    clearWitnessInterpreterLanguageFields(asylumCase);
                });
                break;

            case IS_ANY_WITNESS_INTERPRETER_REQUIRED_PAGE_ID:

                clearWitnessFieldsPreemptively(asylumCase);

                optionalWitnesses.ifPresent(witnesses -> {
                    int i = 0;
                    while (i < witnesses.size()) {

                        String fullName = buildWitnessFullName(witnesses.get(i).getValue());

                        asylumCase.write(WITNESS_N_FIELD.get(i), witnesses.get(i).getValue());
                        asylumCase.write(WITNESS_LIST_ELEMENT_N_FIELD.get(i),
                            new DynamicMultiSelectList(
                                Collections.emptyList(),
                                List.of(new Value(fullName, fullName))
                            ));
                        i++;
                    }
                });
                break;
            default:
                break;
        }

        return response;
    }

    private String buildWitnessFullName(WitnessDetails witnessDetails) {
        return (witnessDetails.getWitnessName() + " " + witnessDetails.getWitnessFamilyName()).trim();
    }

    private void clearWitnessFieldsPreemptively(AsylumCase asylumCase) {
        WITNESS_N_FIELD.forEach(field -> asylumCase.write(field, new WitnessDetails("", "")));
        WITNESS_LIST_ELEMENT_N_FIELD.forEach(field -> asylumCase.write(field, new DynamicMultiSelectList()));
        WITNESS_N_INTERPRETER_CATEGORY_FIELD.forEach(field -> asylumCase.write(field, Collections.emptyList()));
    }

    private void clearWitnessInterpreterLanguageFields(AsylumCase asylumCase) {
        DynamicList dummyDynamicList = new DynamicList("");
        WITNESS_N_INTERPRETER_SPOKEN_LANGUAGE.forEach(field ->
                asylumCase.write(field, new InterpreterLanguageRefData(dummyDynamicList, Collections.emptyList(), "")));
        WITNESS_N_INTERPRETER_SIGN_LANGUAGE.forEach(field ->
            asylumCase.write(field, new InterpreterLanguageRefData(dummyDynamicList, Collections.emptyList(), "")));
    }

}
