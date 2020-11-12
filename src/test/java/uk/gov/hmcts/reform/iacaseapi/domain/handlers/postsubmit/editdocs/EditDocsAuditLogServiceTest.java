package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit.editdocs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.UserDetailsProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserDetails;

public class EditDocsAuditLogServiceTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule().strictness(Strictness.STRICT_STUBS);

    @Mock
    private EditDocsAuditService editDocsAuditService;
    @Mock
    private UserDetails userDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private UserDetailsProvider userDetailsProvider;
    @InjectMocks
    private EditDocsAuditLogService editDocsAuditLogService;

    @Test
    public void shouldBuildAuditDetails() {
        mockUserDetailsProvider();
        mockEditDocsAuditService();
        given(asylumCase.read(eq(AsylumCaseFieldDefinition.EDIT_DOCUMENTS_REASON), eq(String.class)))
            .willReturn(Optional.of("some reasons"));

        AuditDetails actualAuditDetails = editDocsAuditLogService.buildAuditDetails(1L, asylumCase, asylumCase);

        assertEquals("user-id-124", actualAuditDetails.getIdamUserId());
        assertEquals("some forename some surname", actualAuditDetails.getUser());
        assertEquals(Arrays.asList("id1", "id2", "id3", "id4", "id5", "id6", "id7", "id8", "id9", "id10", "id11"),
            actualAuditDetails.getDocumentIds());
        assertEquals(Arrays.asList(
            "docName1", "docName2", "docName3", "docName4", "docName5", "docName6", "docName7", "docName8",
            "docName9", "docName10", "docName11"),
            actualAuditDetails.getDocumentNames()
        );
        assertEquals(1L, actualAuditDetails.getCaseId());
        assertEquals("some reasons", actualAuditDetails.getReason());
        assertNotNull(actualAuditDetails.getDateTime());
    }

    private void mockUserDetailsProvider() {
        when(userDetailsProvider.getUserDetails()).thenReturn(userDetails);
        when(userDetails.getId()).thenReturn("user-id-124");
        when(userDetails.getForename()).thenReturn("some forename");
        when(userDetails.getSurname()).thenReturn("some surname");
    }

    private void mockEditDocsAuditService() {
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
            .thenReturn(Collections.singletonList("id10"))
            .thenReturn(Collections.singletonList("id11"))
            .thenThrow(new RuntimeException("no more calls expected"));

        when(editDocsAuditService.getUpdatedAndDeletedDocNamesForGivenField(any(AsylumCase.class),
            any(AsylumCase.class), any(AsylumCaseFieldDefinition.class)))
            .thenReturn(Collections.singletonList("docName1"))
            .thenReturn(Collections.singletonList("docName2"))
            .thenReturn(Collections.singletonList("docName3"))
            .thenReturn(Collections.singletonList("docName4"))
            .thenReturn(Collections.singletonList("docName5"))
            .thenReturn(Collections.singletonList("docName6"))
            .thenReturn(Collections.singletonList("docName7"))
            .thenReturn(Collections.singletonList("docName8"))
            .thenReturn(Collections.singletonList("docName9"))
            .thenReturn(Collections.singletonList("docName10"))
            .thenReturn(Collections.singletonList("docName11"))
            .thenThrow(new RuntimeException("no more calls expected"));
    }

}
