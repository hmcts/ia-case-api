package uk.gov.hmcts.reform.iacaseapi.domain.entities.em;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;


public class BundleTest {

    private final String id = "some id";
    private final String title = "bundle title";
    private final String description = "bundle desc";
    private final String eligibleForStitching = "yes";
    private final List<IdValue<BundleDocument>> documents = Collections.emptyList();
    private final Optional<String> stitchStatus = Optional.of("DONE");
    private final Optional<Document> stitchedDocument = Optional.of(mock(Document.class));

    private final YesOrNo hasCoverSheets = YesOrNo.NO;
    private final YesOrNo hasTableOfContents = YesOrNo.YES;
    private final String filename = "bundle file name";

    private Bundle bundle = new Bundle(id, title, description, eligibleForStitching, documents, stitchStatus, stitchedDocument, hasCoverSheets, hasTableOfContents, filename);


    @Test
    public void should_hold_onto_values() {

        assertEquals(id, bundle.getId());
        assertEquals(title, bundle.getTitle());
        assertEquals(description, bundle.getDescription());
        assertEquals(eligibleForStitching, bundle.getEligibleForStitching());
        assertEquals(documents, bundle.getDocuments());
        assertEquals(stitchStatus, bundle.getStitchStatus());
        assertEquals(stitchedDocument, bundle.getStitchedDocument());
        assertEquals(hasCoverSheets, bundle.getHasCoversheets());
        assertEquals(hasTableOfContents, bundle.getHasTableOfContents());
        assertEquals(filename, bundle.getFilename());
    }

}
