package uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class CaseDetails<T extends CaseData> {

    private long id;
    private State state;
    private T caseData;

    private CaseDetails() {
        // noop -- for deserializer
    }

    public CaseDetails(
        long id,
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

    public State getState() {
        return state;
    }

    public T getCaseData() {
        return caseData;
    }
}
