package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Mockito.mock;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;

class DocumentWithDescriptionTest {

    final Document document = mock(Document.class);
    final String description = "Some evidence";

    DocumentWithDescription documentWithDescription =
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
