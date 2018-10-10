package uk.gov.hmcts.reform.iacaseapi.shared.domain.entities.ccd;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class CaseDetails<T extends CaseData> {

    private long id;
    private String jurisdiction;
    private State state;
    private T caseData;

    private CaseDetails() {
        // noop -- for deserializer
    }

    public CaseDetails(
        long id,
        String jurisdiction,
        State state,
        T caseData
    ) {
        this.id = id;
        this.state = state;
        this.caseData = caseData;
    }

    public long getId() {
        return id;
    }

    public String getJurisdiction() {
        return jurisdiction;
    }

    public State getState() {
        return state;
    }

    public T getCaseData() {
        return caseData;
    }
}
