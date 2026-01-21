package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class DocumentsAppenderTest {

    @Mock
    private IdValue<DocumentWithMetadata> existingDocumentById1;
    @Mock
    private IdValue<DocumentWithMetadata> existingDocumentById2;
    @Mock
    private IdValue<DocumentWithMetadata> existingDocumentById3;
    @Mock
    private DocumentWithMetadata existingDocument1 = mock(DocumentWithMetadata.class);
    @Mock
    private DocumentWithMetadata existingDocument2 = mock(DocumentWithMetadata.class);
    @Mock
    private DocumentWithMetadata existingDocument3 = mock(DocumentWithMetadata.class);
    @Mock
    private DocumentWithMetadata newDocument1 = mock(DocumentWithMetadata.class);
    @Mock
    private DocumentWithMetadata newDocument2 = mock(DocumentWithMetadata.class);

    private DocumentsAppender documentsAppender = new DocumentsAppender();

    @Test
    void should_append_new_document_in_first_position() {

        List<IdValue<DocumentWithMetadata>> existingDocuments =
            Arrays.asList(
                existingDocumentById1,
                existingDocumentById2
            );

        List<DocumentWithMetadata> newDocuments =
            Arrays.asList(
                newDocument1,
                newDocument2
            );

        when(existingDocumentById1.getValue()).thenReturn(existingDocument1);
        when(existingDocumentById2.getValue()).thenReturn(existingDocument2);

        final List<IdValue<DocumentWithMetadata>> allDocuments =
            documentsAppender.append(existingDocuments, newDocuments);

        verify(existingDocumentById1, never()).getId();
        verify(existingDocumentById2, never()).getId();

        assertNotNull(allDocuments);
        assertEquals(4, allDocuments.size());

        assertEquals("4", allDocuments.get(0).getId());
        assertEquals(newDocument1, allDocuments.get(0).getValue());

        assertEquals("3", allDocuments.get(1).getId());
        assertEquals(newDocument2, allDocuments.get(1).getValue());

        assertEquals("2", allDocuments.get(2).getId());
        assertEquals(existingDocument1, allDocuments.get(2).getValue());

        assertEquals("1", allDocuments.get(3).getId());
        assertEquals(existingDocument2, allDocuments.get(3).getValue());
    }

    @Test
    void should_prepend_new_document_in_last_position() {

        List<IdValue<DocumentWithMetadata>> existingDocuments =
            Arrays.asList(
                existingDocumentById1,
                existingDocumentById2
            );

        List<DocumentWithMetadata> newDocuments =
            Arrays.asList(
                newDocument1,
                newDocument2
            );

        when(existingDocumentById1.getValue()).thenReturn(existingDocument1);
        when(existingDocumentById2.getValue()).thenReturn(existingDocument2);

        final List<IdValue<DocumentWithMetadata>> allDocuments =
            documentsAppender.prepend(existingDocuments, newDocuments);

        verify(existingDocumentById1, never()).getId();
        verify(existingDocumentById2, never()).getId();

        assertNotNull(allDocuments);
        assertEquals(4, allDocuments.size());

        assertEquals("4", allDocuments.get(0).getId());
        assertEquals(existingDocument1, allDocuments.get(0).getValue());

        assertEquals("3", allDocuments.get(1).getId());
        assertEquals(existingDocument2, allDocuments.get(1).getValue());

        assertEquals("2", allDocuments.get(2).getId());
        assertEquals(newDocument1, allDocuments.get(2).getValue());

        assertEquals("1", allDocuments.get(3).getId());
        assertEquals(newDocument2, allDocuments.get(3).getValue());
    }

    @Test
    void should_append_new_document_in_first_position_replacing_any_existing_with_tag() {

        List<IdValue<DocumentWithMetadata>> existingDocuments =
            Arrays.asList(
                existingDocumentById1,
                existingDocumentById2,
                existingDocumentById3
            );

        List<DocumentWithMetadata> newDocuments =
            Arrays.asList(
                newDocument1,
                newDocument2
            );

        when(existingDocumentById1.getValue()).thenReturn(existingDocument1);
        when(existingDocumentById2.getValue()).thenReturn(existingDocument2);
        when(existingDocumentById3.getValue()).thenReturn(existingDocument3);

        when(existingDocument1.getTag()).thenReturn(DocumentTag.APPLICATION_SUBMISSION);
        when(existingDocument2.getTag()).thenReturn(DocumentTag.BAIL_EVIDENCE);
        when(existingDocument3.getTag()).thenReturn(DocumentTag.APPLICATION_SUBMISSION);

        final List<IdValue<DocumentWithMetadata>> allDocuments =
            documentsAppender.append(existingDocuments, newDocuments, DocumentTag.APPLICATION_SUBMISSION);

        verify(existingDocumentById1, never()).getId();
        verify(existingDocumentById2, never()).getId();
        verify(existingDocumentById3, never()).getId();

        assertNotNull(allDocuments);
        assertEquals(3, allDocuments.size());

        assertEquals("3", allDocuments.get(0).getId());
        assertEquals(newDocument1, allDocuments.get(0).getValue());

        assertEquals("2", allDocuments.get(1).getId());
        assertEquals(newDocument2, allDocuments.get(1).getValue());

        assertEquals("1", allDocuments.get(2).getId());
        assertEquals(existingDocument2, allDocuments.get(2).getValue());
    }

    @Test
    void should_return_existing_documents_if_no_new_documents_present() {

        List<IdValue<DocumentWithMetadata>> existingDocuments =
            Arrays.asList(
                existingDocumentById1,
                existingDocumentById2
            );

        List<DocumentWithMetadata> newDocuments = Collections.emptyList();

        when(existingDocumentById1.getValue()).thenReturn(existingDocument1);
        when(existingDocumentById2.getValue()).thenReturn(existingDocument2);

        final List<IdValue<DocumentWithMetadata>> allDocuments =
            documentsAppender.append(existingDocuments, newDocuments);

        assertNotNull(allDocuments);
        assertEquals(2, allDocuments.size());

        assertEquals("2", allDocuments.get(0).getId());
        assertEquals(existingDocument1, allDocuments.get(0).getValue());

        assertEquals("1", allDocuments.get(1).getId());
        assertEquals(existingDocument2, allDocuments.get(1).getValue());
    }

    @Test
    void should_return_new_documents_if_no_existing_documents_present() {

        List<IdValue<DocumentWithMetadata>> existingDocuments = Collections.emptyList();

        List<DocumentWithMetadata> newDocuments =
            Arrays.asList(
                newDocument1,
                newDocument2
            );

        final List<IdValue<DocumentWithMetadata>> allDocuments =
            documentsAppender.append(existingDocuments, newDocuments);

        assertNotNull(allDocuments);
        assertEquals(2, allDocuments.size());

        assertEquals("2", allDocuments.get(0).getId());
        assertEquals(newDocument1, allDocuments.get(0).getValue());

        assertEquals("1", allDocuments.get(1).getId());
        assertEquals(newDocument2, allDocuments.get(1).getValue());
    }

    @Test
    void should_not_allow_null_arguments() {

        List<IdValue<DocumentWithMetadata>> existingDocuments = Arrays.asList(existingDocumentById1);
        List<DocumentWithMetadata> newDocuments = Arrays.asList(newDocument1);

        assertThatThrownBy(() -> documentsAppender.append(null, newDocuments))
            .hasMessage("existingDocuments must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> documentsAppender.append(existingDocuments, null))
            .hasMessage("newDocuments must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
