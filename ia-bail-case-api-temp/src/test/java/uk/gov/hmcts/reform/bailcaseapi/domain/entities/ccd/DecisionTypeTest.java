package uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class DecisionTypeTest {

    @ParameterizedTest
    @EnumSource(value = DecisionType.class, mode = EnumSource.Mode.EXCLUDE, names = {"CONDITIONAL_GRANT"})
    void decisionTypeIsValidForUploadSignedDecisionNotice(DecisionType decisionType) {
        assertTrue(decisionType.isValidFor(Event.UPLOAD_SIGNED_DECISION_NOTICE));
        assertFalse(decisionType.isValidFor(Event.UPLOAD_SIGNED_DECISION_NOTICE_CONDITIONAL_GRANT));
    }

    @ParameterizedTest
    @EnumSource(value = DecisionType.class, mode = EnumSource.Mode.INCLUDE, names = {"CONDITIONAL_GRANT"})
    void decisionTypeIsNotValidForUploadSignedDecisionNotice(DecisionType decisionType) {
        assertFalse(decisionType.isValidFor(Event.UPLOAD_SIGNED_DECISION_NOTICE));
        assertTrue(decisionType.isValidFor(Event.UPLOAD_SIGNED_DECISION_NOTICE_CONDITIONAL_GRANT));
    }

    @Test
    void getEnum_returns_unknown_for_unmatched_value() {
        assertEquals(DecisionType.UNKNOWN, DecisionType.getEnum("unmatchedValue"));
    }

    @Test
    void getEnum_returns_correct_enum_for_matched_value() {
        assertEquals(DecisionType.GRANTED, DecisionType.getEnum("granted"));
        assertEquals(DecisionType.REFUSED, DecisionType.getEnum("refused"));
        assertEquals(DecisionType.CONDITIONAL_GRANT, DecisionType.getEnum("conditionalGrant"));
        assertEquals(DecisionType.REFUSED_UNDER_IMA, DecisionType.getEnum("refusedUnderIma"));
    }
}
