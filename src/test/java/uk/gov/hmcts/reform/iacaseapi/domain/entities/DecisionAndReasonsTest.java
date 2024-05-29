package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;

class DecisionAndReasonsTest {

    private final String updatedDecisionDate = "some-date";
    private final String dateCoverLetterDocumentUploaded = "some-date";
    private final Document coverLetterDocument = mock(Document.class);
    private final String dateDocumentAndReasonsDocumentUploaded = "some-other-date";
    private final String summariseChanges = "summarise-example";
    private final Document documentAndReasonsDocument = mock(Document.class);

    private DecisionAndReasons decisionAndReasons;

    @BeforeEach
    public void setUp() {
        decisionAndReasons = new DecisionAndReasons(
                updatedDecisionDate,
                dateCoverLetterDocumentUploaded,
                coverLetterDocument,
                dateDocumentAndReasonsDocumentUploaded,
                documentAndReasonsDocument,
                summariseChanges);
    }

    @Test
    void should_hold_onto_values() {

        assertThat(decisionAndReasons.getUpdatedDecisionDate()).isEqualTo(updatedDecisionDate);
        assertThat(decisionAndReasons.getDateCoverLetterDocumentUploaded()).isEqualTo(dateCoverLetterDocumentUploaded);
        assertThat(decisionAndReasons.getCoverLetterDocument()).isEqualTo(coverLetterDocument);
        assertThat(decisionAndReasons.getDateDocumentAndReasonsDocumentUploaded()).isEqualTo(dateDocumentAndReasonsDocumentUploaded);
        assertThat(decisionAndReasons.getDocumentAndReasonsDocument()).isEqualTo(documentAndReasonsDocument);
        assertThat(decisionAndReasons.getSummariseChanges()).isEqualTo(summariseChanges);
    }
}
