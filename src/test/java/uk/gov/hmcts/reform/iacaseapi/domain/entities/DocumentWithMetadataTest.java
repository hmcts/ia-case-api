package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;

class DocumentWithMetadataTest {

    final Document document = mock(Document.class);
    final String description = "Some evidence";
    final String dateUploaded = "2018-12-25";
    final DocumentTag tag = DocumentTag.CASE_ARGUMENT;

    DocumentWithMetadata documentWithMetadata =
        new DocumentWithMetadata(
            document,
            description,
            dateUploaded,
            tag
        );

    @Test
    void should_hold_onto_values() {

        assertEquals(document, documentWithMetadata.getDocument());
        assertEquals(description, documentWithMetadata.getDescription());
        assertEquals(dateUploaded, documentWithMetadata.getDateUploaded());
        assertEquals(tag, documentWithMetadata.getTag());
    }

}
