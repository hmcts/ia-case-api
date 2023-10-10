package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_DETAILS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.DRAFT_HEARING_REQUIREMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.InterpreterLanguagesUtils.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.InterpreterLanguagesUtils.WITNESS_LIST_ELEMENT_N_FIELD;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DynamicMultiSelectList;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Value;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.WitnessDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.WitnessesService;

@Component
public class WitnessesDraftMidEventHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private static final String IS_WITNESSES_ATTENDING_PAGE_ID = "isWitnessesAttending";
    private static final String IS_ANY_WITNESS_INTERPRETER_REQUIRED_PAGE_ID = "isAnyWitnessInterpreterRequired";
    private static final String WITNESSES_NUMBER_EXCEEDED_ERROR = "Maximum number of witnesses is 10";

    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        String pageId = callback.getPageId();

        return callbackStage == PreSubmitCallbackStage.MID_EVENT
               && callback.getEvent().equals(DRAFT_HEARING_REQUIREMENTS)
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
            case IS_WITNESSES_ATTENDING_PAGE_ID -> {

                // Append witness party IDs
                WitnessesService.appendWitnessPartyId(asylumCase);
                optionalWitnesses = asylumCase.read(WITNESS_DETAILS);

                // cannot add more than 10 witnesses to the collection
                optionalWitnesses.ifPresentOrElse(witnesses -> {
                    if (witnesses.size() > WITNESS_N_FIELD.size()) {        // 10
                        response.addError(WITNESSES_NUMBER_EXCEEDED_ERROR);
                    }
                }, () -> {
                    // if no witnesses present nullify with dummies all witness-related fields (clearing does not work)
                    clearWitnessIndividualFields(asylumCase);
                    clearWitnessInterpreterLanguageFields(asylumCase);
                });
            }
            case IS_ANY_WITNESS_INTERPRETER_REQUIRED_PAGE_ID -> {
                clearWitnessIndividualFields(asylumCase);
                optionalWitnesses.ifPresent(witnesses ->
                    decentralizeWitnessCollection(asylumCase, witnesses));
            }
            default -> {
            }
        }

        return response;
    }

    /**
     * Breaks witnessDetails collection down into individual witness fields (witness1, witness2 etc.) and generates the
     * individual dynamicMultiSelectList fields for each witness (witnessListElement1, witnessListElement2 etc.)
     *
     * @param asylumCase The asylum case
     * @param witnesses  The value of the witnessDetails field (collection)
     */
    protected void decentralizeWitnessCollection(
        AsylumCase asylumCase, List<IdValue<WitnessDetails>> witnesses) {

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
    }

}
