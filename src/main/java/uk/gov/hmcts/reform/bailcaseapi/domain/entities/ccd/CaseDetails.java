package uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.time.LocalDateTime;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import uk.gov.hmcts.reform.iacaseapi.domain.RequiredFieldMissingException;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@ToString
@EqualsAndHashCode
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
