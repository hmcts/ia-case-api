package uk.gov.hmcts.reform.bailcaseapi.domain.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.Document;

class DocumentWithMetadataTest {

    private final Document document = mock(Document.class);
    private final String description = "Some evidence";
    private final String dateUploaded = "2021-12-25";
    private final DocumentTag tag = DocumentTag.BAIL_EVIDENCE;

    private DocumentWithMetadata documentWithMetadata =
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
