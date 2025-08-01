package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import java.time.LocalDate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.PinInPostDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.AccessCodeGenerator;


@Component
public class PinInPostGenerator implements PreSubmitCallbackHandler<AsylumCase> {

    private final long accessCodeExpiryDays;

    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                && (callback.getEvent() == Event.REMOVE_REPRESENTATION
                    || callback.getEvent() == Event.REMOVE_LEGAL_REPRESENTATIVE
                    || callback.getEvent() == Event.GENERATE_PIN_IN_POST);
    }

    public PinInPostGenerator(
            @Value("${pip_access_code_expiry_days}") long accessCodeExpiryDays
    ) {
        this.accessCodeExpiryDays = accessCodeExpiryDays;
    }

    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(PreSubmitCallbackStage callbackStage,
                                                        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        asylumCase.write(AsylumCaseFieldDefinition.APPELLANT_PIN_IN_POST, PinInPostDetails.builder()
                .accessCode(AccessCodeGenerator.generateAccessCode())
                .expiryDate(LocalDate.now().plusDays(accessCodeExpiryDays).toString())
                .pinUsed(YesOrNo.NO)
                .build());

        if (callback.getEvent().equals(Event.GENERATE_PIN_IN_POST)) {
            asylumCase.write(AsylumCaseFieldDefinition.IS_AIP_TRANSFER, YesOrNo.YES);
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
