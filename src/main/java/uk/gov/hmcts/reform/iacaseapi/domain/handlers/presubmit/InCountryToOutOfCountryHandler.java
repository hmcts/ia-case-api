package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.sourceOfAppealEjp;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

/**
 * This handler ensures stale data is periodically removed (every mid-event)
 * when appeal is moved from in-country to OOC (or vice versa).
 * This is needed for correct ordering of screens on UI.
 */
@Slf4j
@Component
public class InCountryToOutOfCountryHandler implements PreSubmitCallbackHandler<AsylumCase> {

    public boolean canHandle(
            PreSubmitCallbackStage callbackStage,
            Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.MID_EVENT
                && (callback.getEvent() == Event.START_APPEAL || callback.getEvent() == Event.EDIT_APPEAL)
                && !sourceOfAppealEjp(callback.getCaseDetails().getCaseData());
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

        YesOrNo appellantInUk = asylumCase.read(APPELLANT_IN_UK, YesOrNo.class)
            .orElseThrow(() -> new IllegalStateException("Unable to determine if appeal is in UK or out of country"));

        if (appellantInUk.equals(YesOrNo.YES)) {
            asylumCase.clear(OUT_OF_COUNTRY_DECISION_TYPE);

        } else if (appellantInUk.equals(YesOrNo.NO)) {
            asylumCase.write(APPELLANT_IN_DETENTION, YesOrNo.NO);
            asylumCase.write(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.NO);
            asylumCase.clear(DETENTION_FACILITY);
            asylumCase.clear(DETENTION_STATUS);
            asylumCase.clear(CUSTODIAL_SENTENCE);
            asylumCase.clear(IRC_NAME);
            asylumCase.clear(PRISON_NAME);
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
