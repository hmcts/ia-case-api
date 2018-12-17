package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
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

        Optional<DocumentWithMetadata> actualReceivedDocument =
            documentReceiver.receive(document, description, tag);

        verify(dateProvider, times(1)).now();

        assertNotNull(actualReceivedDocument);
        assertTrue(actualReceivedDocument.isPresent());
        assertEquals(document, actualReceivedDocument.get().getDocument());
        assertEquals(description, actualReceivedDocument.get().getDescription());
        assertEquals(dateUploaded, actualReceivedDocument.get().getDateUploaded());
        assertEquals(tag, actualReceivedDocument.get().getTag());
    }

    @Test
    public void should_receive_document_by_adding_metadata() {

        Document document = mock(Document.class);
        String description = "Description";
        String dateUploaded = "2018-12-25";

        DocumentWithDescription documentWithDescription = mock(DocumentWithDescription.class);
        DocumentTag tag = DocumentTag.RESPONDENT_EVIDENCE;

        when(dateProvider.now()).thenReturn(LocalDate.parse(dateUploaded));
        when(documentWithDescription.getDocument()).thenReturn(Optional.of(document));
        when(documentWithDescription.getDescription()).thenReturn(Optional.of(description));

        Optional<DocumentWithMetadata> actualReceivedDocument =
            documentReceiver.receive(documentWithDescription, tag);

        verify(dateProvider, times(1)).now();

        assertNotNull(actualReceivedDocument);
        assertTrue(actualReceivedDocument.isPresent());
        assertEquals(document, actualReceivedDocument.get().getDocument());
        assertEquals(description, actualReceivedDocument.get().getDescription());
        assertEquals(dateUploaded, actualReceivedDocument.get().getDateUploaded());
        assertEquals(tag, actualReceivedDocument.get().getTag());
    }

    @Test
    public void should_not_receive_document_if_file_is_not_uploaded() {

        DocumentWithDescription documentWithDescription = mock(DocumentWithDescription.class);
        DocumentTag tag = DocumentTag.RESPONDENT_EVIDENCE;

        when(documentWithDescription.getDocument()).thenReturn(Optional.empty());

        Optional<DocumentWithMetadata> actualReceivedDocument =
            documentReceiver.receive(documentWithDescription, tag);

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

        assertThatThrownBy(() -> documentReceiver.receive(null, DocumentTag.CASE_ARGUMENT))
            .hasMessage("documentWithDescription must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> documentReceiver.receive(mock(DocumentWithDescription.class), null))
            .hasMessage("tag must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
