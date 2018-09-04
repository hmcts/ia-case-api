package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import java.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Name;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CcdEvent;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CcdEventPreSubmitResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.EventId;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Stage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.CcdEventPreSubmitHandler;
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
               && ccdEvent.getEventId() == EventId.SUBMIT_APPEAL;
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

        Name appellantName =
            asylumCase
                .getAppellantName()
                .orElseThrow(() -> new IllegalStateException("appelantName is not present"));

        String appellantDob =
            asylumCase
                .getAppellantDob()
                .orElseThrow(() -> new IllegalStateException("appelantDob is not present"));

        String appellantNationality =
            appellantNationalitiesAsStringExtractor
                .extract(asylumCase)
                .orElseThrow(() -> new IllegalStateException("appelantNationality is not present"));

        CaseDetails caseDetails = new CaseDetails();

        caseDetails.setCaseStartDate(LocalDate.now().toString());
        caseDetails.setAppellantName(
            (appellantName.getFirstName().orElse("")
             + " "
             + appellantName.getLastName().orElse("")).trim()
        );

        caseDetails.setAppellantNationality(appellantNationality);
        caseDetails.setAppellantDob(appellantDob);
        caseDetails.setLegalRepName("Legal Rep");

        asylumCase.setCaseDetails(caseDetails);

        return preSubmitResponse;
    }
}
