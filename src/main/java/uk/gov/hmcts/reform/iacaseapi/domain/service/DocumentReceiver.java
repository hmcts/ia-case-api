package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static java.util.Objects.requireNonNull;

import java.util.Optional;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentWithDescription;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;

@Service
public class DocumentReceiver {

    private final DateProvider dateProvider;

    public DocumentReceiver(
        DateProvider dateProvider
    ) {
        this.dateProvider = dateProvider;
    }

    public Optional<DocumentWithMetadata> receive(
        Document document,
        String description,
        DocumentTag tag
    ) {
        requireNonNull(document, "document must not be null");
        requireNonNull(description, "description must not be null");
        requireNonNull(tag, "tag must not be null");

        return receive(
            new DocumentWithDescription(
                document,
                description
            ),
            tag
        );
    }

    public Optional<DocumentWithMetadata> receive(
        DocumentWithDescription documentWithDescription,
        DocumentTag tag
    ) {
        requireNonNull(documentWithDescription, "documentWithDescription must not be null");
        requireNonNull(tag, "tag must not be null");

        Optional<Document> optionalDocument = documentWithDescription.getDocument();

        if (optionalDocument.isPresent()) {

            Document document = optionalDocument.get();
            final String dateUploaded = dateProvider.now().toString();

            return Optional.of(
                new DocumentWithMetadata(
                    document,
                    documentWithDescription.getDescription().orElse(""),
                    dateUploaded,
                    tag
                )
            );
        }

        return Optional.empty();
    }
}
