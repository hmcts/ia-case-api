package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

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
public class LegalRepresentativeUpdateDetailsHandler implements PreSubmitCallbackHandler<AsylumCase> {

    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
            && callback.getEvent() == Event.UPDATE_LEGAL_REPRESENTATIVES_DETAILS;
    }

    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        String name = asylumCase.read(
            AsylumCaseFieldDefinition.UPDATE_LEGAL_REP_NAME, String.class)
            .orElse("");
        String email = asylumCase.read(
            AsylumCaseFieldDefinition.UPDATE_LEGAL_REP_EMAIL_ADDRESS, String.class)
            .orElse("");
        String reference = asylumCase.read(
            AsylumCaseFieldDefinition.UPDATE_LEGAL_REP_REFERENCE_NUMBER,String.class)
            .orElse("");

        asylumCase.clear(AsylumCaseFieldDefinition.UPDATE_LEGAL_REP_COMPANY);
        asylumCase.clear(AsylumCaseFieldDefinition.UPDATE_LEGAL_REP_NAME);
        asylumCase.clear(AsylumCaseFieldDefinition.UPDATE_LEGAL_REP_EMAIL_ADDRESS);
        asylumCase.clear(AsylumCaseFieldDefinition.UPDATE_LEGAL_REP_REFERENCE_NUMBER);

        asylumCase.write(AsylumCaseFieldDefinition.LEGAL_REP_NAME, name);
        asylumCase.write(AsylumCaseFieldDefinition.LEGAL_REPRESENTATIVE_EMAIL_ADDRESS, email);
        asylumCase.write(AsylumCaseFieldDefinition.LEGAL_REP_REFERENCE_NUMBER, reference);

        // remove the field which is used to suppress notifications after appeal is transferred to another Legal Rep firm
        asylumCase.clear(AsylumCaseFieldDefinition.CHANGE_ORGANISATION_REQUEST_FIELD);


        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
