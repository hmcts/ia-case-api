package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

@Slf4j
@Component
public class GenerateServiceRequestPreparer implements PreSubmitCallbackHandler<AsylumCase> {

    private final boolean isFeePaymentEnabled;

    public GenerateServiceRequestPreparer(
        @Value("${featureFlag.isfeePaymentEnabled}") boolean isFeePaymentEnabled) {
        this.isFeePaymentEnabled = isFeePaymentEnabled;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");
        log.info("Test 1");
        return (callbackStage == PreSubmitCallbackStage.ABOUT_TO_START)
               &&  callback.getEvent() == Event.GENERATE_SERVICE_REQUEST
               && isFeePaymentEnabled;
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        log.info("Test 2");
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }
        log.info("Test 8");
        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        PreSubmitCallbackResponse<AsylumCase> response = new PreSubmitCallbackResponse<>(asylumCase);
        log.info("Test 9");
        try {
            log.info(asylumCase.read(SERVICE_REQUEST_REFERENCE, String.class).orElse(""));
        } catch (Error | Exception e) {
            log.error(e.getMessage());
            log.error("ERROR ON 1");
        }
        try {
            log.info(String.valueOf(asylumCase.read(HAS_SERVICE_REQUEST_ALREADY, YesOrNo.class).orElse(YesOrNo.NO)));
        } catch (Error | Exception e) {
            log.error(e.getMessage());
            log.error("ERROR ON 2");
        }
        log.info("Test 10");
        if (!asylumCase.read(SERVICE_REQUEST_REFERENCE, String.class).orElse("").isEmpty()
            || asylumCase.read(HAS_SERVICE_REQUEST_ALREADY, YesOrNo.class).orElse(YesOrNo.NO).equals(YesOrNo.YES)) {
            response.addError(
                    "A service request has already been created for this case. Pay via the 'Service Request' tab."
            );
            return response;
        }
        log.info("Test 11");

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}