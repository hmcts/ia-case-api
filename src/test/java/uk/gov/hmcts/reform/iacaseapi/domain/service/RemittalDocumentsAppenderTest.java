package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.RemittalDocument;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class RemittalDocumentsAppenderTest {

    @Mock
    private IdValue<RemittalDocument> existingRemittalDocumentById1;
    @Mock
    private IdValue<RemittalDocument> existingRemittalDocumentById2;
    @Mock
    private RemittalDocument remittalDecisionDocuments;
    @Mock
    private RemittalDocument remittalDocuments;

    private RemittalDocumentsAppender remittalDocumentsAppender = new RemittalDocumentsAppender();

    @Test
    void should_append_new_document_in_first_position() {

        List<IdValue<RemittalDocument>> existingRemittalDocuments =
            Arrays.asList(
                existingRemittalDocumentById1,
                existingRemittalDocumentById2
            );

        when(existingRemittalDocumentById1.getValue()).thenReturn(remittalDocuments);
        when(existingRemittalDocumentById2.getValue()).thenReturn(remittalDocuments);


        final List<IdValue<RemittalDocument>> allDocuments =
            remittalDocumentsAppender.prepend(existingRemittalDocuments, remittalDecisionDocuments);

        verify(existingRemittalDocumentById1, never()).getId();
        verify(existingRemittalDocumentById2, never()).getId();

        assertNotNull(allDocuments);
        assertEquals(3, allDocuments.size());

        assertEquals("3", allDocuments.get(0).getId());
        assertEquals(remittalDecisionDocuments, allDocuments.get(0).getValue());

        assertEquals("2", allDocuments.get(1).getId());
        assertEquals(remittalDocuments, allDocuments.get(1).getValue());

        assertEquals("1", allDocuments.get(2).getId());
        assertEquals(remittalDocuments, allDocuments.get(2).getValue());
    }

    @Test
    void should_return_new_documents_if_no_existing_documents_present() {

        List<IdValue<RemittalDocument>> existingDocuments = Collections.emptyList();

        final List<IdValue<RemittalDocument>> allRemittalDocuments =
            remittalDocumentsAppender.prepend(existingDocuments, remittalDecisionDocuments);

        assertNotNull(allRemittalDocuments);
        assertEquals(1, allRemittalDocuments.size());

        assertEquals("1", allRemittalDocuments.get(0).getId());
        assertEquals(remittalDecisionDocuments, allRemittalDocuments.get(0).getValue());
    }

    @Test
    void should_not_allow_null_arguments() {

        List<IdValue<RemittalDocument>> existingDocuments = Arrays.asList(existingRemittalDocumentById1);

        assertThatThrownBy(() -> remittalDocumentsAppender.prepend(null, remittalDecisionDocuments))
            .hasMessage("existingDocuments must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> remittalDocumentsAppender.prepend(existingDocuments, null))
            .hasMessage("newRemittalDocument must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
