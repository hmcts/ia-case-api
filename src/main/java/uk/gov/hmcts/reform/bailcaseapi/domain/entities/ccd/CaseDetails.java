package uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd;

import java.time.LocalDateTime;
import uk.gov.hmcts.reform.bailcaseapi.domain.RequiredFieldMissingException;

public class CaseDetails<T extends CaseData> {

    private long id;
    private String jurisdiction;
    private State state;
    private T caseData;
    private LocalDateTime createdDate;
    private String securityClassification;

    private CaseDetails() {

    }

    public CaseDetails(
        long id,
        String jurisdiction,
        State state,
        T caseData,
        LocalDateTime createdDate,
        String securityClassification
    ) {
        this.id = id;
        this.jurisdiction = jurisdiction;
        this.state = state;
        this.caseData = caseData;
        this.createdDate = createdDate;
        this.securityClassification = securityClassification;
    }

    public long getId() {
        return id;
    }

    public String getJurisdiction() {
        if (jurisdiction == null) {
            throw new RequiredFieldMissingException("jurisdiction field is required");
        }
        return jurisdiction;
    }

    public State getState() {
        return state;
    }

    public T getCaseData() {
        if (caseData == null) {
            throw new RequiredFieldMissingException("caseData field is required");
        }
        return caseData;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public String getSecurityClassification() {
        return securityClassification;
    }
}
