package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import lombok.extern.slf4j.Slf4j;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

@Slf4j
@Component
public class SetCaseAsUnrepresentedHandler implements PreSubmitCallbackHandler<AsylumCase> {

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
            && (callback.getEvent() == Event.REMOVE_REPRESENTATION
                || callback.getEvent() == Event.REMOVE_LEGAL_REPRESENTATIVE);
    }

    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback) {

        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase =
            callback
                .getCaseDetails()
                .getCaseData();

        boolean isAdmin = HandlerUtils.isInternalCase(asylumCase);
        log.info("is admin is " + isAdmin);
        log.info("is in country detained appeal is " + isInCountryDetainedAppeal(asylumCase));
        log.info("legal rep company is " + LEGAL_REP_COMPANY);
        log.info("legal rep company address is " + LEGAL_REP_COMPANY_ADDRESS);
        log.info("legal rep name is " + LEGAL_REP_NAME);
        log.info("legal representative name is " + LEGAL_REPRESENTATIVE_NAME);
        log.info("legal rep reference number is " + LEGAL_REP_REFERENCE_NUMBER);
        log.info("appellant in uk is " + APPELLANT_IN_UK);

        if (isInCountryDetainedAppeal(asylumCase) && !isAdmin) {
            asylumCase.write(IS_ADMIN, YesOrNo.YES);
            asylumCase.clear(LEGAL_REP_COMPANY);
            asylumCase.clear(LEGAL_REP_COMPANY_ADDRESS);
            asylumCase.clear(LEGAL_REP_NAME);
            asylumCase.clear(LEGAL_REPRESENTATIVE_NAME);
            asylumCase.clear(LEGAL_REP_REFERENCE_NUMBER);
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private boolean isInCountryDetainedAppeal(AsylumCase asylumCase) {
        return (HandlerUtils.isAppellantInDetention(asylumCase)
            && asylumCase.read(APPELLANT_IN_UK, YesOrNo.class).map(value -> value.equals(YesOrNo.YES)).orElse(true));
    }
}
