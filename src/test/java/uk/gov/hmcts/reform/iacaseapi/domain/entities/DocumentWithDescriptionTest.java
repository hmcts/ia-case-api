package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;

class DocumentWithDescriptionTest {

    private final Document document = mock(Document.class);
    private final String description = "Some evidence";

    private DocumentWithDescription documentWithDescription =
        new DocumentWithDescription(
            document,
            description
        );

    @Test
    void should_hold_onto_values() {

        assertEquals(Optional.of(document), documentWithDescription.getDocument());
        assertEquals(Optional.of(description), documentWithDescription.getDescription());
    }
}
