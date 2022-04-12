package uk.gov.hmcts.reform.bailcaseapi.domain.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.bailcaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.DocumentWithDescription;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.IdValue;


@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class DocumentReceiverTest {

    @Mock
    private DateProvider dateProvider;

    private DocumentReceiver documentReceiver;

    @BeforeEach
    public void setUp() {
        documentReceiver = new DocumentReceiver(dateProvider);
    }

    @Test
    void should_receive_document_parts_by_adding_metadata() {

        Document document = mock(Document.class);
        String description = "Description";
        String dateUploaded = "2018-12-25";

        DocumentTag tag = DocumentTag.BAIL_EVIDENCE;

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
    void should_try_to_receive_document_by_adding_metadata() {

        Document document = mock(Document.class);
        String description = "Description";
        String dateUploaded = "2018-12-25";

        DocumentWithDescription documentWithDescription = mock(DocumentWithDescription.class);
        DocumentTag tag = DocumentTag.BAIL_EVIDENCE;

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
    void should_try_to_receive_all_documents_by_adding_metadata() {

        Document document = mock(Document.class);
        String description = "Description";
        String dateUploaded = "2018-12-25";

        DocumentWithDescription documentWithDescription = mock(DocumentWithDescription.class);
        DocumentTag tag = DocumentTag.BAIL_EVIDENCE;

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
    void should_try_to_receive_all_documents_by_adding_metadata_with_supplied_by() {

        Document document = mock(Document.class);
        String description = "Description";
        String dateUploaded = "2018-12-25";

        DocumentWithDescription documentWithDescription = mock(DocumentWithDescription.class);
        DocumentTag tag = DocumentTag.BAIL_EVIDENCE;

        List<IdValue<DocumentWithDescription>> documentsWithDescription =
            Collections.singletonList(new IdValue<>("1", documentWithDescription));

        when(dateProvider.now()).thenReturn(LocalDate.parse(dateUploaded));
        when(documentWithDescription.getDocument()).thenReturn(Optional.of(document));
        when(documentWithDescription.getDescription()).thenReturn(Optional.of(description));

        List<DocumentWithMetadata> actualReceivedDocuments =
            documentReceiver.tryReceiveAll(documentsWithDescription, tag, "Appellant");

        verify(dateProvider, times(1)).now();

        assertNotNull(actualReceivedDocuments);
        assertEquals(1, actualReceivedDocuments.size());
        assertEquals(document, actualReceivedDocuments.get(0).getDocument());
        assertEquals(description, actualReceivedDocuments.get(0).getDescription());
        assertEquals(dateUploaded, actualReceivedDocuments.get(0).getDateUploaded());
        assertEquals(tag, actualReceivedDocuments.get(0).getTag());
        assertEquals("Appellant", actualReceivedDocuments.get(0).getSuppliedBy());
    }

    @Test
    void should_not_receive_document_if_file_is_not_actually_uploaded() {

        DocumentWithDescription documentWithDescription = mock(DocumentWithDescription.class);
        DocumentTag tag = DocumentTag.BAIL_EVIDENCE;

        when(documentWithDescription.getDocument()).thenReturn(Optional.empty());

        Optional<DocumentWithMetadata> actualReceivedDocument =
            documentReceiver.tryReceive(documentWithDescription, tag);

        verify(dateProvider, never()).now();

        assertNotNull(actualReceivedDocument);
        assertFalse(actualReceivedDocument.isPresent());
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> documentReceiver.receive(null, "description", DocumentTag.BAIL_EVIDENCE))
            .hasMessage("document must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> documentReceiver.receive(mock(Document.class), null, DocumentTag.BAIL_EVIDENCE))
            .hasMessage("description must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> documentReceiver.receive(mock(Document.class), "description", null))
            .hasMessage("tag must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> documentReceiver.tryReceive(null, DocumentTag.BAIL_EVIDENCE))
            .hasMessage("documentWithDescription must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> documentReceiver.tryReceive(mock(DocumentWithDescription.class), null))
            .hasMessage("tag must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> documentReceiver.tryReceiveAll(null, DocumentTag.BAIL_EVIDENCE))
            .hasMessage("documentWithDescription must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
