package uk.gov.hmcts.reform.bailcaseapi.domain.service;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.bailcaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.DocumentWithDescription;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.IdValue;

@Service
public class DocumentReceiver {

    private final DateProvider dateProvider;

    public DocumentReceiver(
        DateProvider dateProvider
    ) {
        this.dateProvider = dateProvider;
    }

    public DocumentWithMetadata receive(
        Document document,
        String description,
        DocumentTag tag
    ) {
        requireNonNull(document, "document must not be null");
        requireNonNull(description, "description must not be null");
        requireNonNull(tag, "tag must not be null");

        final String dateUploaded = dateProvider.now().toString();

        return new DocumentWithMetadata(
            document,
            description,
            dateUploaded,
            tag
        );
    }

    public Optional<DocumentWithMetadata> tryReceive(
        DocumentWithDescription documentWithDescription,
        DocumentTag tag) {
        return tryReceive(documentWithDescription, tag, null);
    }

    public Optional<DocumentWithMetadata> tryReceive(
        DocumentWithDescription documentWithDescription,
        DocumentTag tag,
        String suppliedBy
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
                    tag,
                    suppliedBy
                )
            );
        }

        return Optional.empty();
    }

    public List<DocumentWithMetadata> tryReceiveAll(
        List<IdValue<DocumentWithDescription>> documentsWithDescription,
        DocumentTag tag
    ) {
        requireNonNull(documentsWithDescription, "documentWithDescription must not be null");

        return documentsWithDescription
            .stream()
            .map(IdValue::getValue)
            .map(document -> tryReceive(document, tag))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.toList());
    }

    public List<DocumentWithMetadata> tryReceiveAll(List<IdValue<DocumentWithDescription>> documentsWithDescription,
                                                    DocumentTag tag,
                                                    String suppliedBy) {
        requireNonNull(documentsWithDescription, "documentWithDescription must not be null");

        return documentsWithDescription
            .stream()
            .map(IdValue::getValue)
            .map(document -> tryReceive(document, tag, suppliedBy))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.toList());
    }
}
