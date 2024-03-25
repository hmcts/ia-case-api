package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
// Complex object in CCD to represent the collection of the
// Remittal Decision Document & Other remittal documents.
public class RemittalDocument {

    private DocumentWithMetadata decisionDocument;
    private List<IdValue<DocumentWithMetadata>> otherRemittalDocs;
}