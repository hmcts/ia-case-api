package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;

class ReheardHearingDocumentsTest {

    private final Document document = mock(Document.class);
    private final String description = "Some evidence";
    private final String dateUploaded = "2018-12-25";
    private final DocumentTag tag = DocumentTag.CASE_ARGUMENT;
    private final DocumentWithMetadata documentWithMetadata =
            new DocumentWithMetadata(
                    document,
                    description,
                    dateUploaded,
                    tag,
                    "test"
            );
    private List<IdValue<DocumentWithMetadata>> reheardHearingDocs = newArrayList(new IdValue<>("1", documentWithMetadata));
    private ReheardHearingDocuments reheardHearingDocuments;

    @BeforeEach
    public void setUp() {
        reheardHearingDocuments = new ReheardHearingDocuments(reheardHearingDocs);
    }

    @Test
    void should_hold_onto_values() {
        assertThat(reheardHearingDocuments.getReheardHearingDocs()).isEqualTo(reheardHearingDocs);
    }
}
