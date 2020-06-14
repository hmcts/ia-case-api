package uk.gov.hmcts.reform.iacaseapi.domain.entities.em;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import java.util.Optional;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseData;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

@JsonIgnoreProperties(ignoreUnknown = true)
@EqualsAndHashCode
@ToString
public class Bundle implements CaseData {

    private String id;
    private String title;
    private String description;
    private String eligibleForStitching;
    private List<IdValue<BundleDocument>> documents;
    private Optional<String> stitchStatus;
    private Optional<Document> stitchedDocument;

    private YesOrNo hasCoversheets;
    private YesOrNo hasTableOfContents;
    private String filename;

    private Bundle() {
        // noop -- for deserializer
    }

    public Bundle(
        String id,
        String title,
        String description,
        String eligibleForStitching,
        List<IdValue<BundleDocument>> documents,
        String filename
    ) {
        this(
            id,
            title,
            description,
            eligibleForStitching,
            documents,
            Optional.empty(),
            Optional.empty(),
            YesOrNo.YES,
            YesOrNo.YES,
            filename
        );
    }

    public Bundle(
        String id,
        String title,
        String description,
        String eligibleForStitching,
        List<IdValue<BundleDocument>> documents,
        Optional<String> stitchStatus,
        Optional<Document> stitchedDocument,
        YesOrNo hasCoversheets,
        YesOrNo hasTableOfContents,
        String filename
    ) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.eligibleForStitching = eligibleForStitching;
        this.documents = documents;
        this.stitchStatus = stitchStatus;
        this.stitchedDocument = stitchedDocument;
        this.hasCoversheets = hasCoversheets;
        this.hasTableOfContents = hasTableOfContents;
        this.filename = filename;

    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getEligibleForStitching() {
        return eligibleForStitching;
    }

    public List<IdValue<BundleDocument>> getDocuments() {
        return documents;
    }

    public Optional<String> getStitchStatus() {
        return checkIsOptional(stitchStatus);
    }

    public Optional<Document> getStitchedDocument() {
        return checkIsOptional(stitchedDocument);
    }

    public YesOrNo getHasCoversheets() {
        return hasCoversheets;
    }

    public YesOrNo getHasTableOfContents() {
        return hasTableOfContents;
    }

    public String getFilename() {
        return filename;
    }

    //It is possible for the Optional types to be instantiated as null e.g. through Jackson
    private <T> Optional<T> checkIsOptional(Optional<T> field) {
        if (null == field) { //NOSONAR
            return Optional.empty();
        }

        return field;
    }
}
