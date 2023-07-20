package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.DRAFT_HEARING_REQUIREMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.UPDATE_HEARING_REQUIREMENTS;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DynamicMultiSelectList;
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

    protected static final List<AsylumCaseFieldDefinition> WITNESS_N_FIELD = List.of(
        WITNESS_1,
        WITNESS_2,
        WITNESS_3,
        WITNESS_4,
        WITNESS_5,
        WITNESS_6,
        WITNESS_7,
        WITNESS_8,
        WITNESS_9,
        WITNESS_10);
    protected static final List<AsylumCaseFieldDefinition> WITNESS_LIST_ELEMENT_N_FIELD = List.of(
        WITNESS_LIST_ELEMENT_1,
        WITNESS_LIST_ELEMENT_2,
        WITNESS_LIST_ELEMENT_3,
        WITNESS_LIST_ELEMENT_4,
        WITNESS_LIST_ELEMENT_5,
        WITNESS_LIST_ELEMENT_6,
        WITNESS_LIST_ELEMENT_7,
        WITNESS_LIST_ELEMENT_8,
        WITNESS_LIST_ELEMENT_9,
        WITNESS_LIST_ELEMENT_10
    );
    protected static final List<AsylumCaseFieldDefinition> WITNESS_N_INTERPRETER_CATEGORY_FIELD = List.of(
        WITNESS_1_INTERPRETER_LANGUAGE_CATEGORY,
        WITNESS_2_INTERPRETER_LANGUAGE_CATEGORY,
        WITNESS_3_INTERPRETER_LANGUAGE_CATEGORY,
        WITNESS_4_INTERPRETER_LANGUAGE_CATEGORY,
        WITNESS_5_INTERPRETER_LANGUAGE_CATEGORY,
        WITNESS_6_INTERPRETER_LANGUAGE_CATEGORY,
        WITNESS_7_INTERPRETER_LANGUAGE_CATEGORY,
        WITNESS_8_INTERPRETER_LANGUAGE_CATEGORY,
        WITNESS_9_INTERPRETER_LANGUAGE_CATEGORY,
        WITNESS_10_INTERPRETER_LANGUAGE_CATEGORY
    );


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

                optionalWitnesses.ifPresent(witnesses -> {
                    if (witnesses.size() > WITNESS_N_FIELD.size()) {        // 10
                        response.addError(WITNESSES_NUMBER_EXCEEDED_ERROR);
                    }
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

}
