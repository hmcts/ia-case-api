package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.flagcase;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

@Component
public class RemoveFlagHandler implements PreSubmitCallbackHandler<AsylumCase> {

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
            && callback.getEvent() == Event.REMOVE_FLAG;
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

        final DynamicList flagType = asylumCase.read(REMOVE_FLAG_TYPE_OF_FLAG, DynamicList.class)
            .orElseThrow(() -> new IllegalStateException("removeFlagTypeOfFlag is missing"));

        final Optional<List<IdValue<CaseFlag>>> maybeExistingCaseFlags = asylumCase.read(CASE_FLAGS);
        final List<IdValue<CaseFlag>> existingCaseFlags = maybeExistingCaseFlags.orElse(Collections.emptyList());

        final List<IdValue<CaseFlag>> newCaseFlags = new ArrayList<>();

        for (IdValue<CaseFlag> idValue : existingCaseFlags) {
            if (!idValue.getId().equals(flagType.getValue().getCode())) {
                newCaseFlags.add(idValue);
            } else {
                clearDisplayFlags(idValue.getValue().getCaseFlagType(), asylumCase);
            }
        }

        asylumCase.write(CASE_FLAGS, newCaseFlags);
        asylumCase.clear(REMOVE_FLAG_TYPE_OF_FLAG);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    public void clearDisplayFlags(CaseFlagType caseFlagType, AsylumCase asylumCase) {
        switch (caseFlagType) {
            case ANONYMITY:
                asylumCase.clear(CASE_FLAG_ANONYMITY_EXISTS);
                asylumCase.clear(CASE_FLAG_ANONYMITY_ADDITIONAL_INFORMATION);
                break;
            case COMPLEX_CASE:
                asylumCase.clear(CASE_FLAG_COMPLEX_CASE_EXISTS);
                asylumCase.clear(CASE_FLAG_COMPLEX_CASE_ADDITIONAL_INFORMATION);
                break;
            case DETAINED_IMMIGRATION_APPEAL:
                asylumCase.clear(CASE_FLAG_DETAINED_IMMIGRATION_APPEAL_EXISTS);
                asylumCase.clear(CASE_FLAG_DETAINED_IMMIGRATION_APPEAL_ADDITIONAL_INFORMATION);
                break;
            case FOREIGN_NATIONAL_OFFENDER:
                asylumCase.clear(CASE_FLAG_FOREIGN_NATIONAL_OFFENDER_EXISTS);
                asylumCase.clear(CASE_FLAG_FOREIGN_NATIONAL_OFFENDER_ADDITIONAL_INFORMATION);
                break;
            case POTENTIALLY_VIOLENT_PERSON:
                asylumCase.clear(CASE_FLAG_POTENTIALLY_VIOLENT_PERSON_EXISTS);
                asylumCase.clear(CASE_FLAG_POTENTIALLY_VIOLENT_PERSON_ADDITIONAL_INFORMATION);
                break;
            case UNACCEPTABLE_CUSTOMER_BEHAVIOUR:
                asylumCase.clear(CASE_FLAG_UNACCEPTABLE_CUSTOMER_BEHAVIOUR_EXISTS);
                asylumCase.clear(CASE_FLAG_UNACCEPTABLE_CUSTOMER_BEHAVIOUR_ADDITIONAL_INFORMATION);
                break;
            case UNACCOMPANIED_MINOR:
                asylumCase.clear(CASE_FLAG_UNACCOMPANIED_MINOR_EXISTS);
                asylumCase.clear(CASE_FLAG_UNACCOMPANIED_MINOR_ADDITIONAL_INFORMATION);
                break;
            default:
                break;
        }
    }
}
