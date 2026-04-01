package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.SEND_PIP_TO_NON_LEGAL_REP;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.NonLegalRepDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.IdamService;

@Component
public class SendPipToNonLegalRepMidEvent implements PreSubmitCallbackHandler<AsylumCase> {
    private final IdamService idamService;

    public SendPipToNonLegalRepMidEvent(IdamService idamService) {
        this.idamService = idamService;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.MID_EVENT
            && callback.getEvent() == SEND_PIP_TO_NON_LEGAL_REP;
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        String nlrEmail = asylumCase.read(AsylumCaseFieldDefinition.NLR_DETAILS, NonLegalRepDetails.class)
            .map(NonLegalRepDetails::getEmailAddress)
            .orElseThrow(() -> new IllegalStateException("NLR email is not present"));

        if (isNull(idamService.getUserFromEmailV1(nlrEmail))) {
            return new PreSubmitCallbackResponse<>(asylumCase)
                .withError("User with email " + nlrEmail + " has not signed up to HMCTS services. Please invite them to " +
                    "sign up via the \"Send invite to non legal rep\" event before sending the PIP.");
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
