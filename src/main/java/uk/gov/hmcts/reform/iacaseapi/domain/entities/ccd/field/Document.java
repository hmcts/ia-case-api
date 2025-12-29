package uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field;

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.ToString;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
//Ignoring unknown property 'document_hash' for now until we are integrating it later.
@JsonIgnoreProperties(ignoreUnknown = true)
@EqualsAndHashCode
@ToString
public class Document {

    private String documentUrl;
    private String documentBinaryUrl;
    @Setter
    private String documentFilename;

    private Document() {
        super();
        // noop -- for deserializer
    }

    public Document(
        String documentUrl,
        String documentBinaryUrl,
        String documentFilename
    ) {
        requireNonNull(documentUrl);
        requireNonNull(documentBinaryUrl);
        requireNonNull(documentFilename);

        this.documentUrl = documentUrl;
        this.documentBinaryUrl = documentBinaryUrl;
        this.documentFilename = documentFilename;
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
}
