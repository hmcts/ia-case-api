package uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field;

import static junit.framework.TestCase.assertEquals;

import org.junit.Test;

public class DocumentTest {

    private final String documentUrl = "http://doc-store/A";
    private final String documentBinaryUrl = "http://doc-store/A/binary";
    private final String documentFilename = "evidence.pdf";

    private Document document = new Document(
        documentUrl,
        documentBinaryUrl,
        documentFilename
    );

    @Test
    public void should_hold_onto_values() {

        assertEquals(documentUrl, document.getDocumentUrl());
        assertEquals(documentBinaryUrl, document.getDocumentBinaryUrl());
        assertEquals(documentFilename, document.getDocumentFilename());
    }

}
