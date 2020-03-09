package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.EDIT_DOCUMENTS_REASON;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.iacaseapi.Application;
import uk.gov.hmcts.reform.iacaseapi.domain.UserDetailsProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.CaseNote;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit.editdocs.AuditDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.editdocs.EditDocsCaseNoteService;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.security.idam.IdamUserDetails;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@ActiveProfiles("integration")
public class EditDocsCaseNoteServiceTest {

    @MockBean
    private UserDetailsProvider userDetailsProvider;

    @Autowired
    private EditDocsCaseNoteService editDocsCaseNoteService;

    @Test
    public void shouldWriteAuditCaseNote() {
        AsylumCase asylumCase = new AsylumCase();
        asylumCase.write(AsylumCaseFieldDefinition.EDIT_DOCUMENTS_REASON, "some reasons");
        mockUserDetailsProvider();

        editDocsCaseNoteService.writeAuditCaseNoteForGivenCaseId(1L, asylumCase, asylumCase);

        String editDocsReason = asylumCase.read(EDIT_DOCUMENTS_REASON, String.class).orElse(null);
        assertNull(editDocsReason);

        Optional<List<IdValue<CaseNote>>> idCaseNoteValues = asylumCase.read(AsylumCaseFieldDefinition.CASE_NOTES);
        if (idCaseNoteValues.isPresent()) {
            IdValue<CaseNote> caseNoteIdValue = idCaseNoteValues.get().get(0);
            CaseNote caseNote = caseNoteIdValue.getValue();
            assertCaseNote(caseNote);
        } else {
            fail("case note is not present");
        }
    }

    private void assertCaseNote(CaseNote caseNote) {
        assertEquals("Edit documents audit note", caseNote.getCaseNoteSubject());
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
            .documentIds(Collections.emptyList())
            .reason("some reasons")
            .build();
        return String.format("documentIds: %s" + System.lineSeparator() + "reason: %s" + System.lineSeparator(),
            expectedAudit.getDocumentIds(),
            expectedAudit.getReason());
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
