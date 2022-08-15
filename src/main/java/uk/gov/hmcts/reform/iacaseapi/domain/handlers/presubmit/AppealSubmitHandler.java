package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.PA_APPEAL_TYPE_PAYMENT_OPTION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.SUBMIT_APPEAL;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

@Slf4j
@Component
public class AppealSubmitHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final String PAY_OFFLINE = "payOffline";
    private final String PAY_LATER = "payLater";

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return
            callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
            && (callback.getEvent() == SUBMIT_APPEAL);
    }


    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase =
            callback
                .getCaseDetails()
                .getCaseData();

        String paAppealTypePaymentOption = asylumCase.read(PA_APPEAL_TYPE_PAYMENT_OPTION, String.class).orElse("");

        if (paAppealTypePaymentOption.equals(PAY_OFFLINE)) {
            asylumCase.write(PA_APPEAL_TYPE_PAYMENT_OPTION, PAY_LATER);
        }

        final long caseId = callback.getCaseDetails().getId();
        YesOrNo appealOutOfCountry = asylumCase.read(AsylumCaseFieldDefinition.APPEAL_OUT_OF_COUNTRY, YesOrNo.class).orElse(YesOrNo.NO);

        log.info("Appeal submitted for case ID {},  Out Of Country: {}", caseId, appealOutOfCountry);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
