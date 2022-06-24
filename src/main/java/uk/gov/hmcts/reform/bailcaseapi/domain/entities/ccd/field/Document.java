package uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field;

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@EqualsAndHashCode
@ToString
public class Document {

    private String documentUrl;
    private String documentBinaryUrl;
    private String documentFilename;
    private String documentHash;

    private Document() {
        // noop -- for deserializer
    }

    public Document(
        String documentUrl,
        String documentBinaryUrl,
        String documentFilename,
        String documentHash
    ) {
        requireNonNull(documentUrl);
        requireNonNull(documentBinaryUrl);
        requireNonNull(documentFilename);

        this.documentUrl = documentUrl;
        this.documentBinaryUrl = documentBinaryUrl;
        this.documentFilename = documentFilename;
        this.documentHash = documentHash;
    }

    public String getDocumentUrl() {
        return documentUrl;
    }

    public String getDocumentBinaryUrl() {
        return documentBinaryUrl;
    }

    public String getDocumentFilename() {
        return documentFilename;
    }

    public String getDocumentHash() {
        return documentHash;
    }
}
