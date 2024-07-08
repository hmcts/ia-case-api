package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.AsylumFieldLegalRepNameFixer;
import uk.gov.hmcts.reform.iacaseapi.domain.service.WitnessNamesUpdateService;
import uk.gov.hmcts.reform.iacaseapi.domain.service.PartyIdService;

@Slf4j
@Component
@AllArgsConstructor
public class ListAssistIntegrationHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private AsylumFieldLegalRepNameFixer asylumFieldLegalRepNameFixer;

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && callback.getEvent() == Event.LIST_ASSIST_INTEGRATION;
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        asylumFieldLegalRepNameFixer.fix(asylumCase);

        WitnessNamesUpdateService.update(asylumCase);

        PartyIdService.setAppellantPartyId(asylumCase);
        PartyIdService.setLegalRepPartyId(asylumCase);
        PartyIdService.setSponsorPartyId(asylumCase);
        PartyIdService.appendWitnessPartyId(asylumCase);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
