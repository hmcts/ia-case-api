package uk.gov.hmcts.reform.iacaseapi.events.domain.handlers.presubmit;

import java.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.entities.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.events.domain.entities.Callback;
import uk.gov.hmcts.reform.iacaseapi.events.domain.entities.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.entities.ccd.EventId;
import uk.gov.hmcts.reform.iacaseapi.events.domain.entities.CallbackStage;
import uk.gov.hmcts.reform.iacaseapi.events.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.events.domain.entities.DispatchPriority;
import uk.gov.hmcts.reform.iacaseapi.events.domain.service.AppellantNationalitiesAsStringExtractor;

@Component
public class CaseDetailsUpdater implements PreSubmitCallbackHandler<AsylumCase> {

    private final AppellantNationalitiesAsStringExtractor appellantNationalitiesAsStringExtractor;

    public CaseDetailsUpdater(
        @Autowired AppellantNationalitiesAsStringExtractor appellantNationalitiesAsStringExtractor
    ) {
        this.appellantNationalitiesAsStringExtractor = appellantNationalitiesAsStringExtractor;
    }

    public boolean canHandle(
        CallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        return callbackStage == CallbackStage.ABOUT_TO_SUBMIT
               && (callback.getEventId() == EventId.START_APPEAL
                   || callback.getEventId() == EventId.CHANGE_APPEAL
                   || callback.getEventId() == EventId.SUBMIT_APPEAL);
    }

    public DispatchPriority getDispatchPriority() {
        return DispatchPriority.LATE;
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        CallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle ccd event");
        }

        AsylumCase asylumCase =
            callback
                .getCaseDetails()
                .getCaseData();

        PreSubmitCallbackResponse<AsylumCase> preSubmitResponse =
            new PreSubmitCallbackResponse<>(asylumCase);

        String appellantNameForDisplay =
            asylumCase
                .getAppellantNameForDisplay()
                .orElseThrow(() -> new IllegalStateException("appellantNameForDisplay is not present"));

        String appellantDob =
            asylumCase
                .getAppellantDob()
                .orElseThrow(() -> new IllegalStateException("appellantDob is not present"));

        String appellantNationality =
            appellantNationalitiesAsStringExtractor
                .extract(asylumCase)
                .orElseThrow(() -> new IllegalStateException("appellantNationality is not present"));

        CaseDetails caseDetails = new CaseDetails();

        if (callback.getEventId() == EventId.SUBMIT_APPEAL) {
            caseDetails.setCaseStartDate(LocalDate.now().toString());
        }

        caseDetails.setAppellantName(appellantNameForDisplay);
        caseDetails.setAppellantNationality(appellantNationality);
        caseDetails.setAppellantDob(appellantDob);
        caseDetails.setTypeOfAppeal("Asylum");
        caseDetails.setLegalRepName("Legal Rep");

        asylumCase.setCaseDetails(caseDetails);

        return preSubmitResponse;
    }
}
