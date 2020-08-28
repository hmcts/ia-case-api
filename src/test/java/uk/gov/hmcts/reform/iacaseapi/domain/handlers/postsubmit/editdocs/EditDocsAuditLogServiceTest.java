package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit.editdocs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.UserDetailsProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserDetails;

@ExtendWith(MockitoExtension.class)
class EditDocsAuditLogServiceTest {

    @Mock private
    EditDocsAuditService editDocsAuditService;
    @Mock private
    UserDetails userDetails;
    @Mock private
    AsylumCase asylumCase;
    @Mock private
    UserDetailsProvider userDetailsProvider;
    @InjectMocks
    EditDocsAuditLogService editDocsAuditLogService;

    @Test
    void shouldBuildAuditDetails() {
        mockUserDetailsProvider();
        mockAuditServiceDependencies();
        given(asylumCase.read(eq(AsylumCaseFieldDefinition.EDIT_DOCUMENTS_REASON), eq(String.class)))
            .willReturn(Optional.of("some reasons"));

        AuditDetails actualAuditDetails = editDocsAuditLogService.buildAuditDetails(1L, asylumCase, asylumCase);

        assertEquals("user-id-124", actualAuditDetails.getIdamUserId());
        assertEquals("some forename some surname", actualAuditDetails.getUser());
        assertEquals(Arrays.asList("id1", "id2", "id3", "id4", "id5", "id6", "id7", "id8", "id9"),
            actualAuditDetails.getDocumentIds());
        assertEquals(1L, actualAuditDetails.getCaseId());
        assertEquals("some reasons", actualAuditDetails.getReason());
        assertNotNull(actualAuditDetails.getDateTime());
    }

    void mockUserDetailsProvider() {
        when(userDetailsProvider.getUserDetails()).thenReturn(userDetails);
        when(userDetails.getId()).thenReturn("user-id-124");
        when(userDetails.getForename()).thenReturn("some forename");
        when(userDetails.getSurname()).thenReturn("some surname");
    }

    void mockAuditServiceDependencies() {
        when(editDocsAuditService.getUpdatedAndDeletedDocIdsForGivenField(any(AsylumCase.class),
            any(AsylumCase.class), any(AsylumCaseFieldDefinition.class)))
            .thenReturn(Collections.singletonList("id1"))
            .thenReturn(Collections.singletonList("id2"))
            .thenReturn(Collections.singletonList("id3"))
            .thenReturn(Collections.singletonList("id4"))
            .thenReturn(Collections.singletonList("id5"))
            .thenReturn(Collections.singletonList("id6"))
            .thenReturn(Collections.singletonList("id7"))
            .thenReturn(Collections.singletonList("id8"))
            .thenReturn(Collections.singletonList("id9"))
            .thenThrow(new RuntimeException("no more calls expected"));
    }

}