package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.NonLegalRepDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.PinInPostDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.CcdDataService;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HAS_NON_LEGAL_REP_JOINED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.JOIN_APPEAL_PIN;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.NLR_DETAILS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.JOIN_APPEAL_CONFIRMATION;

@Component
public class NonLegalRepJoinAppealHandler implements PreSubmitCallbackHandler<AsylumCase> {
    private final CcdDataService ccdDataService;

    public NonLegalRepJoinAppealHandler(
        CcdDataService ccdDataService
    ) {
        this.ccdDataService = ccdDataService;
    }

    @Override
    public DispatchPriority getDispatchPriority() {
        return DispatchPriority.EARLY;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
            && callback.getEvent() == JOIN_APPEAL_CONFIRMATION;
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        CaseDetails<AsylumCase> caseDetailsBefore = callback.getCaseDetailsBefore()
            .orElseThrow(() -> new IllegalStateException("No case details before given"));

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        PinInPostDetails pinInPostDetails = asylumCase.read(JOIN_APPEAL_PIN, PinInPostDetails.class)
            .orElseThrow(() -> new IllegalStateException("Join appeal pin in post details are not present"));

        pinInPostDetails.setPinUsed(YesOrNo.YES);
        asylumCase.write(JOIN_APPEAL_PIN, pinInPostDetails);

        long caseId = callback.getCaseDetails().getId();

        caseDetailsBefore.getCaseData().read(NLR_DETAILS, NonLegalRepDetails.class)
            .map(NonLegalRepDetails::getIdamId)
            .ifPresent(existingNlrIdamId -> {
                ccdDataService.revokeUserAccessToCase(callback.getCaseDetails().getId(), existingNlrIdamId);
            });

        NonLegalRepDetails newNlrDetails = asylumCase.read(NLR_DETAILS, NonLegalRepDetails.class)
            .orElseThrow(() -> new IllegalStateException("Non legal rep details are not present"));
        String newNlrIdamId = newNlrDetails.getIdamId();
        try {
            ccdDataService.giveUserAccessToCase(caseId, newNlrIdamId);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to assign case role to the new non legal rep: " + e.getMessage(), e);
        }
        asylumCase.write(HAS_NON_LEGAL_REP_JOINED, YesOrNo.YES);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
