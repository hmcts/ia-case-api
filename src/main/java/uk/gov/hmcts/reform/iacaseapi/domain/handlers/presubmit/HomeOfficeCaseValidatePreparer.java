package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_HOME_OFFICE_INTEGRATION_ENABLED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State.APPEAL_SUBMITTED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State.APPEAL_SUBMITTED_OUT_OF_TIME;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

@Component
public class HomeOfficeCaseValidatePreparer implements PreSubmitCallbackHandler<AsylumCase> {

    private final boolean isHomeOfficeIntegrationEnabled;

    public HomeOfficeCaseValidatePreparer(
        @Value("${featureFlag.isHomeOfficeIntegrationEnabled}") boolean isHomeOfficeIntegrationEnabled) {
        this.isHomeOfficeIntegrationEnabled = isHomeOfficeIntegrationEnabled;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_START
            && callback.getEvent() == Event.SUBMIT_APPEAL
            && (callback.getCaseDetails().getState() == APPEAL_SUBMITTED
            || callback.getCaseDetails().getState() == APPEAL_SUBMITTED_OUT_OF_TIME);
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        //Check if home office is enabled and set field for CCD definition
        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        if (isHomeOfficeIntegrationEnabled) {
            asylumCase.write(IS_HOME_OFFICE_INTEGRATION_ENABLED, "Yes");
        } else {
            asylumCase.write(IS_HOME_OFFICE_INTEGRATION_ENABLED, "No");
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
