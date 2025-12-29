package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition.CASE_NOTES;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition.EDIT_DOCUMENTS_REASON;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.CaseNote;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit.editdocs.AuditDetails;

@MockitoSettings(strictness = Strictness.LENIENT)
@RunWith(MockitoJUnitRunner.class)
public class EditDocsCaseNoteServiceTest {

    @Autowired
    @InjectMocks
    EditDocsCaseNoteService editDocsCaseNoteService;

    @Mock
    Appender<CaseNote> appender;
    @Mock
    EditDocsAuditLogService editDocsAuditLogService;
    @Mock
    BailCase bailCase;
    @Mock
    BailCase bailCaseBefore;
    @Mock
    AuditDetails auditDetails;
    @Mock
    IdValue<CaseNote> caseNoteIdValue;
    @Mock
    CaseNote caseNote;
    @Mock
    IdValue<CaseNote> newCaseNoteIdValue;
    @Mock
    CaseNote newCaseNote;

    long caseId = 1L;

    @BeforeEach
    void setup() {
        when(editDocsAuditLogService.buildAuditDetails(caseId, bailCase, bailCaseBefore))
            .thenReturn(auditDetails);
        when(bailCase.read(CASE_NOTES)).thenReturn(Optional.of(List.of(caseNoteIdValue)));

        when(caseNoteIdValue.getValue()).thenReturn(caseNote);


        when(auditDetails.getUser()).thenReturn("someUser");
        when(auditDetails.getDocumentNames()).thenReturn(List.of("document1", "document2"));
        when(auditDetails.getReason()).thenReturn("someReason");
        newCaseNote = buildNewCaseNote(auditDetails);
        when(newCaseNoteIdValue.getValue()).thenReturn(newCaseNote);

        when(appender.append(newCaseNote, List.of(caseNoteIdValue))).thenReturn(List.of(caseNoteIdValue,
                                                                                        newCaseNoteIdValue));
    }

    @Test
    void shouldWriteAuditCaseNoteForGivenCaseId() {
        editDocsCaseNoteService.writeAuditCaseNoteForGivenCaseId(caseId, bailCase, bailCaseBefore);

        verify(bailCase, times(1)).write(CASE_NOTES, List.of(caseNoteIdValue, newCaseNoteIdValue));
        verify(bailCase, times(1)).clear(EDIT_DOCUMENTS_REASON);
    }

    private CaseNote buildNewCaseNote(AuditDetails auditDetails) {
        return new CaseNote(
            "A document was edited or deleted",
            getAuditDetailsFormatted(auditDetails),
            auditDetails.getUser(),
            LocalDate.now().toString()
        );
    }

    private String getAuditDetailsFormatted(AuditDetails auditDetails) {
        return String.format("Document names: %s" + System.lineSeparator() + "Reason: %s",
                             auditDetails.getDocumentNames(),
                             auditDetails.getReason()
        );
    }

}
