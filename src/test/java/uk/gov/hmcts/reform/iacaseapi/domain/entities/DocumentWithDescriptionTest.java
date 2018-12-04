package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Mockito.mock;

import org.junit.Test;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;

public class DocumentWithDescriptionTest {

    private final Document document = mock(Document.class);
    private final String description = "Some evidence";

    private DocumentWithDescription documentWithDescription =
        new DocumentWithDescription(
            document,
            description
        );

    @Test
    public void should_hold_onto_values() {

        assertEquals(document, documentWithDescription.getDocument());
        assertEquals(description, documentWithDescription.getDescription());
    }
}
