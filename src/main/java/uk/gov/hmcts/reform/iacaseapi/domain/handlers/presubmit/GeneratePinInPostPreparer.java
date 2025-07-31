package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Objects.requireNonNull;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

@Component
public class GeneratePinInPostPreparer implements PreSubmitCallbackHandler<AsylumCase> {

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_START
            && callback.getEvent() == Event.GENERATE_PIN_IN_POST;
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        PreSubmitCallbackResponse<AsylumCase> response = new PreSubmitCallbackResponse<>(asylumCase);

        if (isRepresented(asylumCase)) {
            response.addError("Case still has a legal representative, cannot generate PIN in post. Please run Remove Legal Representative event to generate.");
        }
        return response;
    }

    private boolean isRepresented(AsylumCase asylumCase) {
        return !(asylumCase.read(AsylumCaseFieldDefinition.LEGAL_REP_NAME, String.class).orElse("").isEmpty()
            && asylumCase.read(AsylumCaseFieldDefinition.LEGAL_REPRESENTATIVE_NAME, String.class).orElse("").isEmpty()
            && asylumCase.read(AsylumCaseFieldDefinition.LEGAL_REPRESENTATIVE_EMAIL_ADDRESS, String.class).orElse("").isEmpty()
            && asylumCase.read(AsylumCaseFieldDefinition.LEGAL_REP_COMPANY, String.class).orElse("").isEmpty()
            && asylumCase.read(AsylumCaseFieldDefinition.LEGAL_REP_COMPANY_NAME, String.class).orElse("").isEmpty()
            && asylumCase.read(AsylumCaseFieldDefinition.LEGAL_REP_REFERENCE_NUMBER, String.class).orElse("").isEmpty()
            && asylumCase.read(AsylumCaseFieldDefinition.LEGAL_REP_INDIVIDUAL_PARTY_ID, String.class).orElse("").isEmpty()
            && asylumCase.read(AsylumCaseFieldDefinition.LEGAL_REP_ORGANISATION_PARTY_ID, String.class).orElse("").isEmpty());
    }
}


