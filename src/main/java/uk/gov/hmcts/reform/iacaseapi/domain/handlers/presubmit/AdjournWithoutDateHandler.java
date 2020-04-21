package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CURRENT_CASE_STATE_VISIBLE_TO_CASE_OFFICER;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.DATE_BEFORE_ADJOURN_WITHOUT_DATE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.LIST_CASE_HEARING_DATE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.LIST_CASE_HEARING_DATE_ADJOURNED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.STATE_BEFORE_ADJOURN_WITHOUT_DATE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.ADJOURN_HEARING_WITHOUT_DATE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

@Component
public class AdjournWithoutDateHandler implements PreSubmitCallbackHandler<AsylumCase> {
    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");
        return callbackStage.equals(ABOUT_TO_SUBMIT) && callback.getEvent().equals(ADJOURN_HEARING_WITHOUT_DATE);
    }

    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(PreSubmitCallbackStage callbackStage,
                                                        Callback<AsylumCase> callback) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        State currentState = asylumCase.read(CURRENT_CASE_STATE_VISIBLE_TO_CASE_OFFICER, State.class)
            .orElse(State.UNKNOWN);
        String currentHearingDate = asylumCase.read(LIST_CASE_HEARING_DATE, String.class)
            .orElseThrow(() -> new IllegalStateException("listCaseHearingDate is missing."));

        asylumCase.write(LIST_CASE_HEARING_DATE_ADJOURNED, "Adjourned");
        asylumCase.write(STATE_BEFORE_ADJOURN_WITHOUT_DATE, currentState.toString());
        asylumCase.write(DATE_BEFORE_ADJOURN_WITHOUT_DATE, currentHearingDate);
        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
