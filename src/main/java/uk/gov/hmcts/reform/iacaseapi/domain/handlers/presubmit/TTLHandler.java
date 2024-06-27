package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.TTL;

import java.util.List;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.*;

@Component
public class TTLHandler implements PreSubmitCallbackHandler<AsylumCase> {

    @Override
    public DispatchPriority getDispatchPriority() {
        return DispatchPriority.EARLY;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");
        List<Event> validEvents = List.of(SEND_DECISION_AND_REASONS, END_APPEAL, REMOVE_APPEAL_FROM_ONLINE,
            APPLY_FOR_FTPA_APPELLANT, REINSTATE_APPEAL);
        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && validEvents.contains(callback.getEvent());
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

        final TTL ttl = asylumCase.read(AsylumCaseFieldDefinition.TTL, TTL.class).orElse(null);
        if (ttl != null) {
            if (callback.getEvent() == REINSTATE_APPEAL) {
                asylumCase.clear(AsylumCaseFieldDefinition.TTL);
            } else {
                ttl.setOverrideTTL(null);
            }
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
