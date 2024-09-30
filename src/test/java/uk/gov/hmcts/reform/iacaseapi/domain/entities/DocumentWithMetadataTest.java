package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class DocumentWithMetadataTest {

    private Document document;
    private String description;
    private String dateUploaded;
    private DocumentTag tag;
    private String suppliedBy;
    private String uploadedBy;
    private String timeUploaded;

    @BeforeEach
    void setUp() {
        document = mock(Document.class);
        description = "Test Description";
        dateUploaded = "2024-08-20";
        tag = DocumentTag.CASE_SUMMARY;
        suppliedBy = "Test Supplier";
        uploadedBy = "Test Uploader";
        timeUploaded = "10:00 AM";
    }

    @Test
    void should_create_document_with_metadata_full_constructor() {
        DocumentWithMetadata documentWithMetadata = new DocumentWithMetadata(
            document, description, dateUploaded, tag, suppliedBy, uploadedBy, timeUploaded
        );

        assertEquals(document, documentWithMetadata.getDocument());
        assertEquals(description, documentWithMetadata.getDescription());
        assertEquals(dateUploaded, documentWithMetadata.getDateUploaded());
        assertEquals(tag, documentWithMetadata.getTag());
        assertEquals(suppliedBy, documentWithMetadata.getSuppliedBy());
        assertEquals(uploadedBy, documentWithMetadata.getUploadedBy());
        assertEquals(timeUploaded, documentWithMetadata.getDateTimeUploaded());
    }

    @Test
    void should_create_document_with_metadata_without_suppliedBy_and_uploadedBy() {
        DocumentWithMetadata documentWithMetadata = new DocumentWithMetadata(
            document, description, dateUploaded, tag
        );

        assertEquals(document, documentWithMetadata.getDocument());
        assertEquals(description, documentWithMetadata.getDescription());
        assertEquals(dateUploaded, documentWithMetadata.getDateUploaded());
        assertEquals(tag, documentWithMetadata.getTag());
        assertNull(documentWithMetadata.getSuppliedBy());
        assertNull(documentWithMetadata.getUploadedBy());
        assertNull(documentWithMetadata.getDateTimeUploaded());
    }

    @Test
    void should_throw_exception_when_document_is_null() {
        DocumentWithMetadata nullDocument = new DocumentWithMetadata(null, description, dateUploaded, tag);
        assertThrows(NullPointerException.class, nullDocument::getDocument);
    }

    @Test
    void should_throw_exception_when_dateUploaded_is_null() {
        DocumentWithMetadata nullDate = new DocumentWithMetadata(document, description, null, tag);
        assertThrows(NullPointerException.class, nullDate::getDateUploaded);
    }

    @Test
    void should_throw_exception_when_tag_is_null() {
        DocumentWithMetadata nullTag = new DocumentWithMetadata(document, description, dateUploaded, null);
        assertThrows(NullPointerException.class, nullTag::getTag);
    }

    @Test
    void should_return_correct_getters() {
        DocumentWithMetadata documentWithMetadata = new DocumentWithMetadata(
            document, description, dateUploaded, tag, suppliedBy, uploadedBy, timeUploaded
        );

        assertNotNull(documentWithMetadata.getDocument());
        assertEquals(description, documentWithMetadata.getDescription());
        assertEquals(dateUploaded, documentWithMetadata.getDateUploaded());
        assertEquals(tag, documentWithMetadata.getTag());
        assertEquals(suppliedBy, documentWithMetadata.getSuppliedBy());
        assertEquals(uploadedBy, documentWithMetadata.getUploadedBy());
        assertEquals(timeUploaded, documentWithMetadata.getDateTimeUploaded());
    }

    @Test
    void should_handle_null_values_correctly() {
        DocumentWithMetadata documentWithMetadata = new DocumentWithMetadata(
            document, description, dateUploaded, tag, null, null, null
        );

        assertNull(documentWithMetadata.getSuppliedBy());
        assertNull(documentWithMetadata.getUploadedBy());
        assertNull(documentWithMetadata.getDateTimeUploaded());
    }

    @Test
    void should_provide_string_representation() {
        DocumentWithMetadata documentWithMetadata = new DocumentWithMetadata(
            document, description, dateUploaded, tag, suppliedBy, uploadedBy, timeUploaded
        );

        String stringRepresentation = documentWithMetadata.toString();
        assertTrue(stringRepresentation.contains("document="));
        assertTrue(stringRepresentation.contains("description="));
        assertTrue(stringRepresentation.contains("dateUploaded="));
        assertTrue(stringRepresentation.contains("tag="));
        assertTrue(stringRepresentation.contains("suppliedBy="));
        assertTrue(stringRepresentation.contains("uploadedBy="));
    }

    @Test
    void should_return_correct_equals_and_hashcode() {
        DocumentWithMetadata documentWithMetadata1 = new DocumentWithMetadata(
            document, description, dateUploaded, tag, suppliedBy, uploadedBy, timeUploaded
        );

        DocumentWithMetadata documentWithMetadata2 = new DocumentWithMetadata(
            document, description, dateUploaded, tag, suppliedBy, uploadedBy, timeUploaded
        );

        assertEquals(documentWithMetadata1, documentWithMetadata2);
        assertEquals(documentWithMetadata1.hashCode(), documentWithMetadata2.hashCode());
    }
}
