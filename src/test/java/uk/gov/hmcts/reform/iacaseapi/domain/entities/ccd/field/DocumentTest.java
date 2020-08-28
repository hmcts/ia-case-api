package uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class DocumentTest {

    private final String documentUrl = "http://doc-store/A";
    private final String documentBinaryUrl = "http://doc-store/A/binary";
    private final String documentFilename = "evidence.pdf";

    private Document document = new Document(
        documentUrl,
        documentBinaryUrl,
        documentFilename
    );

    @Test
    void should_hold_onto_values() {

        assertEquals(documentUrl, document.getDocumentUrl());
        assertEquals(documentBinaryUrl, document.getDocumentBinaryUrl());
        assertEquals(documentFilename, document.getDocumentFilename());
    }

}
