package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HAS_BEEN_DECIDED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HAS_BEEN_FTPA_DECIDED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.FORCE_FTPA_DECIDED_STATE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.LEADERSHIP_JUDGE_FTPA_DECISION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.RESIDENT_JUDGE_FTPA_DECISION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

@Component
public class FtpaDecidedFlagHandler implements PreSubmitCallbackHandler<AsylumCase> {

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        Event event = callback.getEvent();
        return callbackStage.equals(ABOUT_TO_SUBMIT) &&
            event.equals(LEADERSHIP_JUDGE_FTPA_DECISION) ||
            event.equals(RESIDENT_JUDGE_FTPA_DECISION) ||
            event.equals(FORCE_FTPA_DECIDED_STATE);
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase =
            callback
                .getCaseDetails()
                .getCaseData();

        asylumCase.write(HAS_BEEN_DECIDED, YesOrNo.YES);
        asylumCase.write(HAS_BEEN_FTPA_DECIDED, YesOrNo.YES);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
