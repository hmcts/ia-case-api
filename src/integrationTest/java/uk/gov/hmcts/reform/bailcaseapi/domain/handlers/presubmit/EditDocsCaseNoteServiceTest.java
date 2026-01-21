package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition.APPLICANT_DOCUMENTS_WITH_METADATA;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition.EDIT_DOCUMENTS_REASON;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentTag.BAIL_SUBMISSION;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.bailcaseapi.component.testutils.SpringBootIntegrationTest;
import uk.gov.hmcts.reform.iacaseapi.domain.UserDetailsProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.CaseNote;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.HasDocument;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit.editdocs.AuditDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.service.EditDocsCaseNoteService;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.security.idam.IdamUserDetails;

class EditDocsCaseNoteServiceTest extends SpringBootIntegrationTest {

    @MockBean
    private UserDetailsProvider userDetailsProvider;

    @Autowired
    private EditDocsCaseNoteService editDocsCaseNoteService;

    @Test
    void shouldWriteAuditCaseNote() {
        BailCase bailCaseBefore = new BailCase();
        mockBailCaseBeforeToHaveOneDocument(bailCaseBefore);
        mockUserDetailsProvider();
        BailCase bailCaseAfterWithDocumentDeleted = new BailCase();
        bailCaseAfterWithDocumentDeleted.write(EDIT_DOCUMENTS_REASON, "some reasons");

        editDocsCaseNoteService
            .writeAuditCaseNoteForGivenCaseId(1L, bailCaseAfterWithDocumentDeleted, bailCaseBefore);

        String editDocsReason =
            bailCaseAfterWithDocumentDeleted.read(EDIT_DOCUMENTS_REASON, String.class).orElse(null);
        assertNull(editDocsReason);

        Optional<List<IdValue<CaseNote>>> idCaseNoteValues =
            bailCaseAfterWithDocumentDeleted.read(BailCaseFieldDefinition.CASE_NOTES);
        if (idCaseNoteValues.isPresent()) {
            IdValue<CaseNote> caseNoteIdValue = idCaseNoteValues.get().get(0);
            CaseNote caseNote = caseNoteIdValue.getValue();
            assertCaseNote(caseNote);
        } else {
            fail("case note is not present");
        }
    }

    private void mockBailCaseBeforeToHaveOneDocument(BailCase bailCaseBefore) {
        Document doc = buildTestDoc("some doc name");
        DocumentWithMetadata metadata = buildTestMetadata(doc);

        Document doc2 = buildTestDoc("some other doc name");
        DocumentWithMetadata metadata2 = buildTestMetadata(doc2);

        bailCaseBefore.write(APPLICANT_DOCUMENTS_WITH_METADATA,
            Arrays.asList(
                new IdValue<HasDocument>("1", metadata),
                new IdValue<HasDocument>("2", metadata2)
            )
        );
    }

    @NotNull
    private DocumentWithMetadata buildTestMetadata(Document doc) {
        return new DocumentWithMetadata(
            doc,
            "desc",
            "1-1-2020",
            BAIL_SUBMISSION,
            null
        );
    }

    @NotNull
    private Document buildTestDoc(String filename) {
        return new Document(
            "http://dm-store:89/someId",
            "",
            filename,
            null
        );
    }

    private void assertCaseNote(CaseNote caseNote) {
        assertEquals("A document was edited or deleted", caseNote.getCaseNoteSubject());
        assertCaseNoteDescription(caseNote);
        assertEquals("some forename some surname", caseNote.getUser());
        assertNull(caseNote.getCaseNoteDocument());
        assertEquals(LocalDate.now().toString(), caseNote.getDateAdded());
    }

    private void assertCaseNoteDescription(CaseNote caseNote) {
        String expectedCaseNoteDescription = getExpectedCaseNoteDescription();
        String actualCaseNoteDescription = caseNote.getCaseNoteDescription();
        assertThat(actualCaseNoteDescription).startsWith(expectedCaseNoteDescription);
    }

    private String getExpectedCaseNoteDescription() {
        AuditDetails expectedAudit = AuditDetails.builder()
            .documentIds(Arrays.asList("someId", "someId"))
            .documentNames(Arrays.asList("some doc name", "some other doc name"))
            .reason("some reasons")
            .build();
        return String.format(
            "Document names: %s" + System.lineSeparator() + "Reason: %s",
            expectedAudit.getDocumentNames(),
            expectedAudit.getReason()
        );
    }

    private void mockUserDetailsProvider() {
        UserDetails userDetails = new IdamUserDetails(
            "Bearer eyJ0eXAiOiJKV1QiLCJ6aXAiOiJOT05FIiwia2lkIjoiYi9PNk92VnYxK3krV2dySDV",
            "75211309-2318-451a-8cd3-00cdccb4be76",
            Collections.singletonList("ia-caseworker"),
            "some@email.com",
            "some forename",
            "some surname");
        BDDMockito.given(userDetailsProvider.getUserDetails()).willReturn(userDetails);
    }
}
