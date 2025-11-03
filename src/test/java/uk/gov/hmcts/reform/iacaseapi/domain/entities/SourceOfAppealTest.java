package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class SourceOfAppealTest {

    @Test
    void test_all_values() {
        assertEquals(SourceOfAppeal.PAPER_FORM.getValue(), "paperForm");
        assertEquals(SourceOfAppeal.TRANSFERRED_FROM_UPPER_TRIBUNAL.getValue(), "transferredFromUpperTribunal");
    }

    @Test
    void test_toString_gives_same_value() {
        assertEquals(SourceOfAppeal.PAPER_FORM.getValue(), SourceOfAppeal.PAPER_FORM.toString());
        assertEquals(SourceOfAppeal.TRANSFERRED_FROM_UPPER_TRIBUNAL.getValue(), SourceOfAppeal.TRANSFERRED_FROM_UPPER_TRIBUNAL.toString());
    }

    @Test
    void should_throw_error_for_unknown_value() {
        assertThatThrownBy(() -> SourceOfAppeal.from("unknown"))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessage("unknown is not a valid source of appeal");
    }

    @Test
    void should_create_one_facility_from_given_value() {
        assertEquals(SourceOfAppeal.PAPER_FORM, SourceOfAppeal.from("paperForm"));
        assertEquals(SourceOfAppeal.TRANSFERRED_FROM_UPPER_TRIBUNAL, SourceOfAppeal.from("transferredFromUpperTribunal"));
    }

    @Test
    void should_break_if_new_facility_is_added() {
        assertEquals(3, SourceOfAppeal.values().length);
    }

}
