package uk.gov.hmcts.reform.bailcaseapi.domain.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class DocumentTagTest {

    @Test
    void has_correct_values() {

        assertEquals("uploadTheBailEvidenceDocs", DocumentTag.GROUNDS_FOR_BAIL_EVIDENCE.toString());
        assertEquals("", DocumentTag.NONE.toString());

    }

    @Test
    void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertEquals(2, DocumentTag.values().length);
    }

}
