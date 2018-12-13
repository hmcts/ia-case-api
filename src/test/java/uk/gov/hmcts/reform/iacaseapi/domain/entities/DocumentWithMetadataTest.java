package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Mockito.mock;

import org.junit.Test;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;

public class DocumentWithMetadataTest {

    private final Document document = mock(Document.class);
    private final String description = "Some evidence";
    private final String dateUploaded = "2018-12-25";
    private final DocumentTag tag = DocumentTag.CASE_ARGUMENT;

    private DocumentWithMetadata documentWithMetadata =
        new DocumentWithMetadata(
            document,
            description,
            dateUploaded,
            tag
        );

    @Test
    public void should_hold_onto_values() {

        assertEquals(document, documentWithMetadata.getDocument());
        assertEquals(description, documentWithMetadata.getDescription());
        assertEquals(dateUploaded, documentWithMetadata.getDateUploaded());
        assertEquals(tag, documentWithMetadata.getTag());
    }
}
