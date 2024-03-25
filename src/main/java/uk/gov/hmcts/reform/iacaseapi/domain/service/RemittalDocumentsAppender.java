package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.RemittalDocument;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;

@Service
public class RemittalDocumentsAppender {
    public List<IdValue<RemittalDocument>> prepend(
        List<IdValue<RemittalDocument>> existingRemittalDocuments,
        RemittalDocument newRemittalDocument
    ) {
        requireNonNull(existingRemittalDocuments, "existingDocuments must not be null");
        requireNonNull(newRemittalDocument, "newRemittalDocument must not be null");

        final List<IdValue<RemittalDocument>> allDocuments = new ArrayList<>();

        int index = existingRemittalDocuments.size() + 1;

        allDocuments.add(new IdValue<>(String.valueOf(index--), newRemittalDocument));

        for (IdValue<RemittalDocument> existingDocument : existingRemittalDocuments) {
            allDocuments.add(new IdValue<>(String.valueOf(index--), existingDocument.getValue()));
        }

        return allDocuments;
    }
}
