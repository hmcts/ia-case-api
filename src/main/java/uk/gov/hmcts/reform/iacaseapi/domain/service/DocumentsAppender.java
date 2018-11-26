package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentWithDescription;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;

@Service
public class DocumentsAppender {

    private final DateProvider dateProvider;

    public DocumentsAppender(
        DateProvider dateProvider
    ) {
        this.dateProvider = dateProvider;
    }

    public List<IdValue<DocumentWithMetadata>> append(
        List<IdValue<DocumentWithMetadata>> existingDocuments,
        List<IdValue<DocumentWithDescription>> newDocuments
    ) {
        requireNonNull(existingDocuments, "existingDocuments must not be null");
        requireNonNull(newDocuments, "newDocuments must not be null");

        final List<DocumentWithMetadata> newDocumentsWithMetadata =
            mapToDocumentsWithMetadata(newDocuments);

        final List<IdValue<DocumentWithMetadata>> allDocuments = new ArrayList<>();

        int index = existingDocuments.size() + newDocumentsWithMetadata.size();

        for (DocumentWithMetadata newDocument : newDocumentsWithMetadata) {
            allDocuments.add(new IdValue<>(String.valueOf(index--), newDocument));
        }

        for (IdValue<DocumentWithMetadata> existingDocument : existingDocuments) {
            allDocuments.add(new IdValue<>(String.valueOf(index--), existingDocument.getValue()));
        }

        return allDocuments;
    }

    private List<DocumentWithMetadata> mapToDocumentsWithMetadata(
        List<IdValue<DocumentWithDescription>> newDocuments
    ) {
        if (newDocuments.isEmpty()) {
            return Collections.emptyList();
        }

        final String now = dateProvider.now().toString();

        return newDocuments
            .stream()
            .map(IdValue::getValue)
            .map(documentWithDescription ->
                new DocumentWithMetadata(
                    documentWithDescription.getDocument(),
                    documentWithDescription.getDescription(),
                    now
                )
            )
            .collect(Collectors.toList());
    }
}
