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
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.service.EditDocsAuditLogService;
import uk.gov.hmcts.reform.iacaseapi.domain.service.EditDocsAuditService;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.security.idam.IdamUserDetailsHelper;

@MockitoSettings(strictness = Strictness.STRICT_STUBS)
@ExtendWith(MockitoExtension.class)
class EditDocsAuditLogServiceTest {

    @Mock
    private EditDocsAuditService editDocsAuditService;
    @Mock
    private UserDetails userDetails;
    @Mock
    private BailCase bailCase;
    @Mock
    private IdamUserDetailsHelper idamUserDetailsHelper;

    @InjectMocks
    private EditDocsAuditLogService editDocsAuditLogService;

    @Test
    void shouldBuildAuditDetails() {
        mockUserDetailsProvider();
        mockEditDocsAuditService();
        given(bailCase.read(eq(BailCaseFieldDefinition.EDIT_DOCUMENTS_REASON), eq(String.class)))
            .willReturn(Optional.of("some reasons"));

        AuditDetails actualAuditDetails = editDocsAuditLogService.buildAuditDetails(1L, bailCase, bailCase);

        assertEquals("user-id-124", actualAuditDetails.getIdamUserId());
        assertEquals("some forename some surname", idamUserDetailsHelper.getIdamUserName(userDetails));
        assertEquals(
            Arrays.asList("id1", "id4", "id2", "id5", "id3", "id6"),
            actualAuditDetails.getDocumentIds());
        assertEquals(Arrays.asList(
                         "docName1", "docName4", "docName2", "docName5", "docName3", "docName6"),
                     actualAuditDetails.getDocumentNames()
        );
        assertEquals(1L, actualAuditDetails.getCaseId());
        assertEquals("some reasons", actualAuditDetails.getReason());
        assertNotNull(actualAuditDetails.getDateTime());
    }

    private void mockUserDetailsProvider() {
        when(userDetails.getId()).thenReturn("user-id-124");
        when(idamUserDetailsHelper.getIdamUserName(userDetails)).thenReturn("some forename some surname");
    }

    private void mockEditDocsAuditService() {
        when(editDocsAuditService.getUpdatedAndDeletedDocIdsForGivenField(any(BailCase.class),
                                                                          any(BailCase.class),
                                                                          any(BailCaseFieldDefinition.class)))
            .thenReturn(Collections.singletonList("id1"))
            .thenReturn(Collections.singletonList("id2"))
            .thenReturn(Collections.singletonList("id3"))
            .thenThrow(new RuntimeException("no more calls expected"));

        when(editDocsAuditService.getUpdatedAndDeletedDocNamesForGivenField(any(BailCase.class),
                                                                            any(BailCase.class),
                                                                            any(BailCaseFieldDefinition.class)))
            .thenReturn(Collections.singletonList("docName1"))
            .thenReturn(Collections.singletonList("docName2"))
            .thenReturn(Collections.singletonList("docName3"))
            .thenThrow(new RuntimeException("no more calls expected"));

        when(editDocsAuditService.getAddedDocIdsForGivenField(any(BailCase.class),
                                                              any(BailCase.class),
                                                              any(BailCaseFieldDefinition.class)))
            .thenReturn(Collections.singletonList("id4"))
            .thenReturn(Collections.singletonList("id5"))
            .thenReturn(Collections.singletonList("id6"))
            .thenThrow(new RuntimeException("no more calls expected"));

        when(editDocsAuditService.getAddedDocNamesForGivenField(any(BailCase.class),
                                                                any(BailCase.class),
                                                                any(BailCaseFieldDefinition.class)))
            .thenReturn(Collections.singletonList("docName4"))
            .thenReturn(Collections.singletonList("docName5"))
            .thenReturn(Collections.singletonList("docName6"))
            .thenThrow(new RuntimeException("no more calls expected"));
    }

}
