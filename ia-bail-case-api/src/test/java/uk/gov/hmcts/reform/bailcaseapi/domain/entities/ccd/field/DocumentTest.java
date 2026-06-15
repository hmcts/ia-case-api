package uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class DocumentTest {

    private final String documentUrl = "http://doc-store/A";
    private final String documentBinaryUrl = "http://doc-store/A/binary";
    private final String documentFilename = "evidence.pdf";
    private final String documentHash = "hash";

    private Document document = new Document(
        documentUrl,
        documentBinaryUrl,
        documentFilename,
        documentHash
    );

    @Test
    void should_hold_onto_values() {

        assertEquals(documentUrl, document.getDocumentUrl());
        assertEquals(documentBinaryUrl, document.getDocumentBinaryUrl());
        assertEquals(documentFilename, document.getDocumentFilename());
        assertEquals(documentHash, document.getDocumentHash());

    }

}
