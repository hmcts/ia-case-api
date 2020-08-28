package uk.gov.hmcts.reform.iacaseapi.domain.entities.em;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;

public class BundleDocumentTest {

    private final String name = "name";
    private final String description = "description";
    private final int sortIndex = 1;
    private final Document sourceDocument = mock(Document.class);

    private BundleDocument bundleDocument = new BundleDocument(name, description, sortIndex, sourceDocument);


    @Test
    public void should_hold_onto_values() {

        assertEquals(sourceDocument, bundleDocument.getSourceDocument());
        assertEquals(description, bundleDocument.getDescription());
        assertEquals(sortIndex, bundleDocument.getSortIndex());
        assertEquals(name, bundleDocument.getName());
    }

}
