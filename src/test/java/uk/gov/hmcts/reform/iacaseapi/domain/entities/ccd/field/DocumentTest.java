package uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field;

import static junit.framework.TestCase.assertEquals;

import org.junit.jupiter.api.Test;

class DocumentTest {

    final String documentUrl = "http://doc-store/A";
    final String documentBinaryUrl = "http://doc-store/A/binary";
    final String documentFilename = "evidence.pdf";

    Document document = new Document(
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
