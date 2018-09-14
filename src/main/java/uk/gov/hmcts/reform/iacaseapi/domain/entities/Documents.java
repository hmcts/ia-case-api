package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import java.util.List;
import java.util.Optional;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.IdValue;

public class Documents {

    private Optional<List<IdValue<DocumentWithType>>> documents = Optional.empty();

    public Optional<List<IdValue<DocumentWithType>>> getDocuments() {
        return documents;
    }

    public void setDocuments(List<IdValue<DocumentWithType>> documents) {
        this.documents = Optional.ofNullable(documents);
    }
}
