package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;

@Service
public class DocumentsAppender {

    public List<IdValue<DocumentWithMetadata>> append(
        List<IdValue<DocumentWithMetadata>> existingDocuments,
        List<DocumentWithMetadata> newDocuments
    ) {
        requireNonNull(existingDocuments, "existingDocuments must not be null");
        requireNonNull(newDocuments, "newDocuments must not be null");

        final List<IdValue<DocumentWithMetadata>> allDocuments = new ArrayList<>();

        int index = existingDocuments.size() + newDocuments.size();

        for (DocumentWithMetadata newDocument : newDocuments) {
            allDocuments.add(new IdValue<>(String.valueOf(index--), newDocument));
        }

        for (IdValue<DocumentWithMetadata> existingDocument : existingDocuments) {
            allDocuments.add(new IdValue<>(String.valueOf(index--), existingDocument.getValue()));
        }

        return allDocuments;
    }
}
