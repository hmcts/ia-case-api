package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentWithDescription;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class DocumentReceiverTest {

    @Mock private DateProvider dateProvider;

    private DocumentReceiver documentReceiver;

    @Before
    public void setUp() {
        documentReceiver = new DocumentReceiver(dateProvider);
    }

    @Test
    public void should_receive_document_parts_by_adding_metadata() {

        Document document = mock(Document.class);
        String description = "Description";
        String dateUploaded = "2018-12-25";

        DocumentTag tag = DocumentTag.RESPONDENT_EVIDENCE;

        when(dateProvider.now()).thenReturn(LocalDate.parse(dateUploaded));

        DocumentWithMetadata actualReceivedDocument =
            documentReceiver.receive(document, description, tag);

        verify(dateProvider, times(1)).now();

        assertNotNull(actualReceivedDocument);
        assertEquals(document, actualReceivedDocument.getDocument());
        assertEquals(description, actualReceivedDocument.getDescription());
        assertEquals(dateUploaded, actualReceivedDocument.getDateUploaded());
        assertEquals(tag, actualReceivedDocument.getTag());
    }

    @Test
    public void should_try_to_receive_document_by_adding_metadata() {

        Document document = mock(Document.class);
        String description = "Description";
        String dateUploaded = "2018-12-25";

        DocumentWithDescription documentWithDescription = mock(DocumentWithDescription.class);
        DocumentTag tag = DocumentTag.RESPONDENT_EVIDENCE;

        when(dateProvider.now()).thenReturn(LocalDate.parse(dateUploaded));
        when(documentWithDescription.getDocument()).thenReturn(Optional.of(document));
        when(documentWithDescription.getDescription()).thenReturn(Optional.of(description));

        Optional<DocumentWithMetadata> actualReceivedDocument =
            documentReceiver.tryReceive(documentWithDescription, tag);

        verify(dateProvider, times(1)).now();

        assertNotNull(actualReceivedDocument);
        assertTrue(actualReceivedDocument.isPresent());
        assertEquals(document, actualReceivedDocument.get().getDocument());
        assertEquals(description, actualReceivedDocument.get().getDescription());
        assertEquals(dateUploaded, actualReceivedDocument.get().getDateUploaded());
        assertEquals(tag, actualReceivedDocument.get().getTag());
    }

    @Test
    public void should_try_to_receive_all_documents_by_adding_metadata() {

        Document document = mock(Document.class);
        String description = "Description";
        String dateUploaded = "2018-12-25";

        DocumentWithDescription documentWithDescription = mock(DocumentWithDescription.class);
        DocumentTag tag = DocumentTag.RESPONDENT_EVIDENCE;

        List<IdValue<DocumentWithDescription>> documentsWithDescription =
            Collections.singletonList(new IdValue<>("1", documentWithDescription));

        when(dateProvider.now()).thenReturn(LocalDate.parse(dateUploaded));
        when(documentWithDescription.getDocument()).thenReturn(Optional.of(document));
        when(documentWithDescription.getDescription()).thenReturn(Optional.of(description));

        List<DocumentWithMetadata> actualReceivedDocuments =
            documentReceiver.tryReceiveAll(documentsWithDescription, tag);

        verify(dateProvider, times(1)).now();

        assertNotNull(actualReceivedDocuments);
        assertEquals(1, actualReceivedDocuments.size());
        assertEquals(document, actualReceivedDocuments.get(0).getDocument());
        assertEquals(description, actualReceivedDocuments.get(0).getDescription());
        assertEquals(dateUploaded, actualReceivedDocuments.get(0).getDateUploaded());
        assertEquals(tag, actualReceivedDocuments.get(0).getTag());
    }

    @Test
    public void should_not_receive_document_if_file_is_not_actually_uploaded() {

        DocumentWithDescription documentWithDescription = mock(DocumentWithDescription.class);
        DocumentTag tag = DocumentTag.RESPONDENT_EVIDENCE;

        when(documentWithDescription.getDocument()).thenReturn(Optional.empty());

        Optional<DocumentWithMetadata> actualReceivedDocument =
            documentReceiver.tryReceive(documentWithDescription, tag);

        verify(dateProvider, never()).now();

        assertNotNull(actualReceivedDocument);
        assertFalse(actualReceivedDocument.isPresent());
    }

    @Test
    public void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> documentReceiver.receive(null, "description", DocumentTag.CASE_ARGUMENT))
            .hasMessage("document must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> documentReceiver.receive(mock(Document.class), null, DocumentTag.CASE_ARGUMENT))
            .hasMessage("description must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> documentReceiver.receive(mock(Document.class), "description", null))
            .hasMessage("tag must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> documentReceiver.tryReceive(null, DocumentTag.CASE_ARGUMENT))
            .hasMessage("documentWithDescription must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> documentReceiver.tryReceive(mock(DocumentWithDescription.class), null))
            .hasMessage("tag must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> documentReceiver.tryReceiveAll(null, DocumentTag.CASE_ARGUMENT))
            .hasMessage("documentWithDescription must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
