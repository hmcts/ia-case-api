package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentWithDescription;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class DocumentsAppenderTest {

    @Mock private DateProvider dateProvider;
    @Mock private IdValue<DocumentWithMetadata> existingDocumentById1;
    @Mock private IdValue<DocumentWithMetadata> existingDocumentById2;
    @Mock private IdValue<DocumentWithDescription> newDocumentById1;
    @Mock private IdValue<DocumentWithDescription> newDocumentById2;
    private String expectedDateUploaded = LocalDate.MAX.toString();

    private DocumentsAppender documentsAppender;

    @Before
    public void setUp() {
        documentsAppender = new DocumentsAppender(dateProvider);
    }

    @Test
    public void should_append_new_direction_in_first_position() {

        when(dateProvider.now()).thenReturn(LocalDate.MAX);

        DocumentWithMetadata existingDocument1 = mock(DocumentWithMetadata.class);
        when(existingDocumentById1.getValue()).thenReturn(existingDocument1);

        DocumentWithMetadata existingDocument2 = mock(DocumentWithMetadata.class);
        when(existingDocumentById2.getValue()).thenReturn(existingDocument2);

        Document newDocumentReference1 = mock(Document.class);
        String newDocumentDescription1 = "Document 1";
        DocumentWithDescription newDocument1 = mock(DocumentWithDescription.class);
        when(newDocument1.getDocument()).thenReturn(newDocumentReference1);
        when(newDocument1.getDescription()).thenReturn(newDocumentDescription1);
        when(newDocumentById1.getValue()).thenReturn(newDocument1);

        Document newDocumentReference2 = mock(Document.class);
        String newDocumentDescription2 = "Document 2";
        DocumentWithDescription newDocument2 = mock(DocumentWithDescription.class);
        when(newDocument2.getDocument()).thenReturn(newDocumentReference2);
        when(newDocument2.getDescription()).thenReturn(newDocumentDescription2);
        when(newDocumentById2.getValue()).thenReturn(newDocument2);

        List<IdValue<DocumentWithMetadata>> existingDocuments =
            Arrays.asList(existingDocumentById1, existingDocumentById2);

        List<IdValue<DocumentWithDescription>> newDocuments =
            Arrays.asList(newDocumentById1, newDocumentById2);

        List<IdValue<DocumentWithMetadata>> allDocuments =
            documentsAppender.append(existingDocuments, newDocuments);

        verify(existingDocumentById1, never()).getId();
        verify(existingDocumentById2, never()).getId();

        assertNotNull(allDocuments);
        assertEquals(4, allDocuments.size());

        assertEquals("4", allDocuments.get(0).getId());
        assertEquals(newDocumentReference1, allDocuments.get(0).getValue().getDocument());
        assertEquals(newDocumentDescription1, allDocuments.get(0).getValue().getDescription());
        assertEquals(expectedDateUploaded, allDocuments.get(0).getValue().getDateUploaded());

        assertEquals("3", allDocuments.get(1).getId());
        assertEquals(newDocumentReference2, allDocuments.get(1).getValue().getDocument());
        assertEquals(newDocumentDescription2, allDocuments.get(1).getValue().getDescription());
        assertEquals(expectedDateUploaded, allDocuments.get(1).getValue().getDateUploaded());

        assertEquals("2", allDocuments.get(2).getId());
        assertEquals(existingDocument1, allDocuments.get(2).getValue());

        assertEquals("1", allDocuments.get(3).getId());
        assertEquals(existingDocument2, allDocuments.get(3).getValue());
    }

    @Test
    public void should_return_existing_documents_if_no_new_documents_present() {

        DocumentWithMetadata existingDocument1 = mock(DocumentWithMetadata.class);
        when(existingDocumentById1.getValue()).thenReturn(existingDocument1);

        List<IdValue<DocumentWithMetadata>> existingDocuments =
            Arrays.asList(existingDocumentById1);

        List<IdValue<DocumentWithDescription>> newDocuments =
            Collections.emptyList();

        List<IdValue<DocumentWithMetadata>> allDocuments =
            documentsAppender.append(existingDocuments, newDocuments);

        assertNotNull(allDocuments);
        assertEquals(1, allDocuments.size());

        assertEquals("1", allDocuments.get(0).getId());
        assertEquals(existingDocument1, allDocuments.get(0).getValue());
    }

    @Test
    public void should_return_new_documents_if_no_existing_documents_present() {

        when(dateProvider.now()).thenReturn(LocalDate.MAX);

        Document newDocumentReference1 = mock(Document.class);
        String newDocumentDescription1 = "Document 1";
        DocumentWithDescription newDocument1 = mock(DocumentWithDescription.class);
        when(newDocument1.getDocument()).thenReturn(newDocumentReference1);
        when(newDocument1.getDescription()).thenReturn(newDocumentDescription1);
        when(newDocumentById1.getValue()).thenReturn(newDocument1);

        List<IdValue<DocumentWithMetadata>> existingDocuments =
            Collections.emptyList();

        List<IdValue<DocumentWithDescription>> newDocuments =
            Arrays.asList(newDocumentById1);

        List<IdValue<DocumentWithMetadata>> allDocuments =
            documentsAppender.append(existingDocuments, newDocuments);

        assertNotNull(allDocuments);
        assertEquals(1, allDocuments.size());

        assertEquals("1", allDocuments.get(0).getId());
        assertEquals(newDocumentReference1, allDocuments.get(0).getValue().getDocument());
        assertEquals(newDocumentDescription1, allDocuments.get(0).getValue().getDescription());
        assertEquals(expectedDateUploaded, allDocuments.get(0).getValue().getDateUploaded());
    }

    @Test
    public void should_not_allow_null_arguments() {

        List<IdValue<DocumentWithMetadata>> existingDocuments =
            Arrays.asList(existingDocumentById1);

        List<IdValue<DocumentWithDescription>> newDocuments =
            Arrays.asList(newDocumentById1);

        assertThatThrownBy(() -> documentsAppender.append(null, newDocuments))
            .hasMessage("existingDocuments must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> documentsAppender.append(existingDocuments, null))
            .hasMessage("newDocuments must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
