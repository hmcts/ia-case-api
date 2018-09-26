package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import java.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.*;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.CcdEventPreSubmitHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.DispatchPriority;
import uk.gov.hmcts.reform.iacaseapi.domain.service.AppellantNationalitiesAsStringExtractor;

@Component
public class CaseDetailsUpdater implements CcdEventPreSubmitHandler<AsylumCase> {

    private final AppellantNationalitiesAsStringExtractor appellantNationalitiesAsStringExtractor;

    public CaseDetailsUpdater(
        @Autowired AppellantNationalitiesAsStringExtractor appellantNationalitiesAsStringExtractor
    ) {
        this.appellantNationalitiesAsStringExtractor = appellantNationalitiesAsStringExtractor;
    }

    public boolean canHandle(
        Stage stage,
        CcdEvent<AsylumCase> ccdEvent
    ) {
        return stage == Stage.ABOUT_TO_SUBMIT
               && (ccdEvent.getEventId() == EventId.START_APPEAL
                   || ccdEvent.getEventId() == EventId.CHANGE_APPEAL
                   || ccdEvent.getEventId() == EventId.SUBMIT_APPEAL);
    }

    public DispatchPriority getDispatchPriority() {
        return DispatchPriority.LATE;
    }

    public CcdEventPreSubmitResponse<AsylumCase> handle(
        Stage stage,
        CcdEvent<AsylumCase> ccdEvent
    ) {
        if (!canHandle(stage, ccdEvent)) {
            throw new IllegalStateException("Cannot handle ccd event");
        }

        AsylumCase asylumCase =
            ccdEvent
                .getCaseDetails()
                .getCaseData();

        CcdEventPreSubmitResponse<AsylumCase> preSubmitResponse =
            new CcdEventPreSubmitResponse<>(asylumCase);

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

        if (ccdEvent.getEventId() == EventId.SUBMIT_APPEAL) {
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
