package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class DecisionAndReasons {

    private String updatedDecisionDate;
    private String dateCoverLetterDocumentUploaded;
    private Document coverLetterDocument;
    private String dateDocumentAndReasonsDocumentUploaded;
    private Document documentAndReasonsDocument;
    private String summariseChanges;

    @JsonCreator
    private DecisionAndReasons() {
        // noop -- for deserializer
    }
}
