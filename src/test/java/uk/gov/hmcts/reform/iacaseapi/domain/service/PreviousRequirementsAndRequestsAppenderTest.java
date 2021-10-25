package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CASE_FLAG_SET_ASIDE_REHEARD_EXISTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HEARING_REQUIREMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.PREVIOUS_HEARING_REQUIREMENTS;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;


@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class PreviousRequirementsAndRequestsAppenderTest {

    @Mock
    private IdValue<DocumentWithMetadata> existingPreviousRequirementsAndRequestsById1;
    @Mock
    private IdValue<DocumentWithMetadata> existingPreviousRequirementsAndRequestsById2;
    @Mock
    private DocumentWithMetadata existingPreviousRequirementsAndRequests1 = mock(DocumentWithMetadata.class);
    @Mock
    private DocumentWithMetadata existingPreviousRequirementsAndRequests2 = mock(DocumentWithMetadata.class);
    @Mock
    private DocumentWithMetadata newPreviousRequirementsAndRequests1 = mock(DocumentWithMetadata.class);
    @Mock
    private AsylumCase asylumCase;

    private PreviousRequirementsAndRequestsAppender previousRequirementsAndRequestsAppender =
        new PreviousRequirementsAndRequestsAppender();

    @Test
    void should_append_previous_hearing_in_first_position() {

        List<IdValue<DocumentWithMetadata>> existingPreviousRequirementsAndRequests =
            Arrays.asList(
                existingPreviousRequirementsAndRequestsById1,
                existingPreviousRequirementsAndRequestsById2
            );

        DocumentWithMetadata newPreviousRequirementsAndRequests = newPreviousRequirementsAndRequests1;

        when(existingPreviousRequirementsAndRequestsById1.getValue())
            .thenReturn(existingPreviousRequirementsAndRequests1);
        when(existingPreviousRequirementsAndRequestsById2.getValue())
            .thenReturn(existingPreviousRequirementsAndRequests2);

        final List<IdValue<DocumentWithMetadata>> allPreviousRequirementsAndRequests =
            previousRequirementsAndRequestsAppender
                .append(existingPreviousRequirementsAndRequests, newPreviousRequirementsAndRequests);

        verify(existingPreviousRequirementsAndRequestsById1, never()).getId();
        verify(existingPreviousRequirementsAndRequestsById2, never()).getId();

        assertNotNull(allPreviousRequirementsAndRequests);
        assertEquals(3, allPreviousRequirementsAndRequests.size());

        assertEquals("3", allPreviousRequirementsAndRequests.get(0).getId());
        assertEquals(newPreviousRequirementsAndRequests1, allPreviousRequirementsAndRequests.get(0).getValue());

        assertEquals("2", allPreviousRequirementsAndRequests.get(1).getId());
        assertEquals(existingPreviousRequirementsAndRequests1, allPreviousRequirementsAndRequests.get(1).getValue());

        assertEquals("1", allPreviousRequirementsAndRequests.get(2).getId());
        assertEquals(existingPreviousRequirementsAndRequests2, allPreviousRequirementsAndRequests.get(2).getValue());
    }

    @Test
    void should_return_new_previous_requirements_and_requests_if_no_existing_previous_requirements_and_requests_present() {

        List<IdValue<DocumentWithMetadata>> existingDPreviousRequirementsAndRequests = Collections.emptyList();

        final List<IdValue<DocumentWithMetadata>> allPreviousRequirementsAndRequests =
            previousRequirementsAndRequestsAppender
                .append(existingDPreviousRequirementsAndRequests, newPreviousRequirementsAndRequests1);

        assertNotNull(allPreviousRequirementsAndRequests);
        assertEquals(1, allPreviousRequirementsAndRequests.size());

        assertEquals("1", allPreviousRequirementsAndRequests.get(0).getId());
        assertEquals(newPreviousRequirementsAndRequests1, allPreviousRequirementsAndRequests.get(0).getValue());
    }

    @Test
    void should_trim_hearing_requirements_and_requests_when_size_is_greater_than_one() {

        final List<IdValue<DocumentWithMetadata>> existingRequirementsAndRequests = asList(
            new IdValue<DocumentWithMetadata>(
                "1",
                new DocumentWithMetadata(
                    new Document("documentUrl", "binaryUrl", "documentFilename", UUID.randomUUID().toString()),
                    "description",
                    "dateUploaded",
                    DocumentTag.HEARING_REQUIREMENTS
                )
            ),
            new IdValue<DocumentWithMetadata>(
                "2",
                new DocumentWithMetadata(
                    new Document("documentUrl", "binaryUrl", "documentFilename",UUID.randomUUID().toString()),
                    "description",
                    "dateUploaded",
                    DocumentTag.HEARING_REQUIREMENTS
                )
            )
        );

        final List<IdValue<DocumentWithMetadata>> existingPreviousRequirementsAndRequests = asList(
            new IdValue<DocumentWithMetadata>(
                "1",
                new DocumentWithMetadata(
                    new Document("documentUrl", "binaryUrl", "documentFilename",UUID.randomUUID().toString()),
                    "description",
                    "dateUploaded",
                    DocumentTag.HEARING_REQUIREMENTS
                )
            ),
            new IdValue<DocumentWithMetadata>(
                "2",
                new DocumentWithMetadata(
                    new Document("documentUrl", "binaryUrl", "documentFilename",UUID.randomUUID().toString()),
                    "description",
                    "dateUploaded",
                    DocumentTag.HEARING_REQUIREMENTS
                )
            )
        );

        asylumCase.write(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS, YesOrNo.YES);

        previousRequirementsAndRequestsAppender.appendAndTrim(asylumCase);

        when(asylumCase.read(HEARING_REQUIREMENTS)).thenReturn(Optional.of(existingRequirementsAndRequests));

        when(asylumCase.read(PREVIOUS_HEARING_REQUIREMENTS))
            .thenReturn(Optional.of(existingPreviousRequirementsAndRequests));

        verify(asylumCase, times(1)).read(HEARING_REQUIREMENTS);

        verify(asylumCase, times(1)).read(PREVIOUS_HEARING_REQUIREMENTS);

        assertEquals(1L, asylumCase.read(HEARING_REQUIREMENTS).stream().count());

        assertEquals(1L, asylumCase.read(PREVIOUS_HEARING_REQUIREMENTS).stream().count());
    }

    @Test
    void should_not_allow_null_arguments() {

        List<IdValue<DocumentWithMetadata>> existingPreviousRequirementsAndRequests =
            Arrays.asList(existingPreviousRequirementsAndRequestsById1);

        assertThatThrownBy(
            () -> previousRequirementsAndRequestsAppender.append(null, newPreviousRequirementsAndRequests1))
            .hasMessage("existingRequirementsAndRequests must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(
            () -> previousRequirementsAndRequestsAppender.append(existingPreviousRequirementsAndRequests, null))
            .hasMessage("newRequirementsAndRequests must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
