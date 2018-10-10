package uk.gov.hmcts.reform.iacaseapi.events.domain.handlers.presubmit;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.entities.Name;
import uk.gov.hmcts.reform.iacaseapi.events.domain.entities.Callback;
import uk.gov.hmcts.reform.iacaseapi.events.domain.entities.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.events.domain.entities.CallbackStage;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.entities.ccd.EventId;
import uk.gov.hmcts.reform.iacaseapi.events.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.events.domain.entities.DispatchPriority;

@Component
public class AppellantNameFormatter implements PreSubmitCallbackHandler<AsylumCase> {

    public boolean canHandle(
        CallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        return callbackStage == CallbackStage.ABOUT_TO_SUBMIT
               && (callback.getEventId() == EventId.START_APPEAL
                   || callback.getEventId() == EventId.CHANGE_APPEAL);
    }

    public DispatchPriority getDispatchPriority() {
        return DispatchPriority.EARLY;
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        CallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle ccd event");
        }

        AsylumCase asylumCase =
            callback
                .getCaseDetails()
                .getCaseData();

        PreSubmitCallbackResponse<AsylumCase> preSubmitResponse =
            new PreSubmitCallbackResponse<>(asylumCase);

        Name appellantName =
            asylumCase
                .getAppellantName()
                .orElseThrow(() -> new IllegalStateException("appellantName is not present"));

        String appellantNameForDisplay =
            appellantName.getTitle().orElse("")
            + " "
            + appellantName.getFirstName().orElse("")
            + " "
            + appellantName.getLastName().orElse("");

        asylumCase.setAppellantNameForDisplay(
            appellantNameForDisplay.replaceAll("\\s+", " ").trim()
        );

        return preSubmitResponse;
    }
}
