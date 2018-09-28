package uk.gov.hmcts.reform.iacaseapi.domain.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Documents;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.IdValue;

@Service
public class DocumentAppender {

    public void append(
        AsylumCase asylumCase,
        Document document
    ) {
        append(
            asylumCase,
            new DocumentWithMetadata(
                document
            )
        );
    }

    public void append(
        AsylumCase asylumCase,
        Document document,
        String description
    ) {
        append(
            asylumCase,
            new DocumentWithMetadata(
                document,
                description
            )
        );
    }

    public void append(
        AsylumCase asylumCase,
        DocumentWithMetadata document
    ) {
        Documents documents =
            asylumCase
                .getDocuments()
                .orElse(new Documents());

        if (documents.getDocuments().isPresent()) {

            if (documents
                .getDocuments()
                .get()
                .stream()
                .map(IdValue::getValue)
                .anyMatch(existingDocument ->
                    existingDocument
                        .getDocument()
                        .get()
                        .getDocumentUrl()
                        .equals(
                            document
                                .getDocument()
                                .get()
                                .getDocumentUrl()
                        )
                )) {
                // document already uploaded
                return;
            }
        }

        document.setStored(Optional.of("Yes"));
        document.setDateUploaded(LocalDate.now().toString());

        List<IdValue<DocumentWithMetadata>> allDocuments = new ArrayList<>();

        allDocuments.add(
            new IdValue<>(
                document
                    .getDocument()
                    .get()
                    .getDocumentUrl(),
                document
            )
        );

        if (documents.getDocuments().isPresent()) {
            allDocuments.addAll(
                documents.getDocuments().get()
            );
        }

        documents.setDocuments(allDocuments);

        asylumCase.setDocuments(documents);
    }
}
