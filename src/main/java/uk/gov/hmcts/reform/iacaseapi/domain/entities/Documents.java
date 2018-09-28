package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import java.util.List;
import java.util.Optional;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.IdValue;

public class Documents {

    private Optional<List<IdValue<DocumentWithMetadata>>> documents = Optional.empty();

    public Optional<List<IdValue<DocumentWithMetadata>>> getDocuments() {
        return documents;
    }

    public void setDocuments(List<IdValue<DocumentWithMetadata>> documents) {
        this.documents = Optional.ofNullable(documents);
    }
}
