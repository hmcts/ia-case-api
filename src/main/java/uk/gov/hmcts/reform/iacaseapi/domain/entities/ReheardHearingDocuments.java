package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import lombok.Setter;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;

import java.util.List;

import static java.util.Objects.requireNonNull;

@Setter
public class ReheardHearingDocuments {

    private List<IdValue<DocumentWithMetadata>> reheardHearingDocs;

    private ReheardHearingDocuments() {
        // noop -- for deserializer
    }

    public ReheardHearingDocuments(List<IdValue<DocumentWithMetadata>> reheardHearingDocs) {
        requireNonNull(reheardHearingDocs);
        this.reheardHearingDocs = reheardHearingDocs;
    }

    public List<IdValue<DocumentWithMetadata>> getReheardHearingDocs() {
        requireNonNull(reheardHearingDocs);
        return reheardHearingDocs;
    }
}
