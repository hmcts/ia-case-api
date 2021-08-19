package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;

class DirectionTest {

    private final String explanation = "Do the thing";
    private final Parties parties = Parties.RESPONDENT;
    private final String dateDue = "2018-12-31T12:34:56";
    private final String dateSent = "2018-12-25";
    private DirectionTag tag = DirectionTag.LEGAL_REPRESENTATIVE_REVIEW;
    private List<IdValue<PreviousDates>> previousDates = Collections.emptyList();
    private List<IdValue<ClarifyingQuestion>> clarifyingQuestions = Collections.emptyList();
    private final String uniqueId = UUID.randomUUID().toString();
    private final String directionType = "someEventDirectionType";

    private Direction direction = new Direction(
        explanation,
        parties,
        dateDue,
        dateSent,
        tag,
        previousDates,
        Collections.emptyList(),
        UUID.randomUUID().toString(),
        "someDirectionType"
    );

    private Direction directionWithQuestions = new Direction(
        explanation,
        parties,
        dateDue,
        dateSent,
        tag,
        previousDates,
        clarifyingQuestions,
        uniqueId,
        directionType
    );

    @Test
    void should_hold_onto_values() {

        assertEquals(explanation, direction.getExplanation());
        assertEquals(parties, direction.getParties());
        assertEquals(dateDue, direction.getDateDue());
        assertEquals(dateSent, direction.getDateSent());
        assertEquals(tag, direction.getTag());
        assertEquals(previousDates, direction.getPreviousDates());
    }

    @Test
    void should_hold_onto_values_for_clarifying_questions() {

        assertEquals(explanation, directionWithQuestions.getExplanation());
        assertEquals(parties, directionWithQuestions.getParties());
        assertEquals(dateDue, directionWithQuestions.getDateDue());
        assertEquals(dateSent, directionWithQuestions.getDateSent());
        assertEquals(tag, directionWithQuestions.getTag());
        assertEquals(previousDates, directionWithQuestions.getPreviousDates());
        assertEquals(clarifyingQuestions, directionWithQuestions.getClarifyingQuestions());
        assertEquals(uniqueId, directionWithQuestions.getUniqueId());
        assertEquals(directionType, directionWithQuestions.getDirectionType());
    }

    @Test
    void should_not_allow_null_arguments() {

        List<IdValue<ClarifyingQuestion>> clarifyingQuestions = Collections.emptyList();
        String uniqueId = UUID.randomUUID().toString();
        assertThatThrownBy(() -> new Direction(null, parties, dateDue, dateSent, tag, previousDates,
                clarifyingQuestions,
                uniqueId,
                "someDirectionType"
        ))
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> new Direction(explanation, null, dateDue, dateSent, tag, previousDates,
                clarifyingQuestions,
                uniqueId,

                "someDirectionType"
        ))
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> new Direction(explanation, parties, null, dateSent, tag, previousDates,
                clarifyingQuestions,
                uniqueId,
                "someDirectionType"
        ))
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> new Direction(explanation, parties, dateDue, null, tag, previousDates,
                clarifyingQuestions,
                uniqueId,
                "someDirectionType"
        ))
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> new Direction(explanation, parties, dateDue, dateSent, null, previousDates,
                clarifyingQuestions,
                uniqueId,
                "someDirectionType"
        ))
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> new Direction(explanation, parties, dateDue, dateSent, tag, null,
                clarifyingQuestions,
                uniqueId,
                "someDirectionType"
        ))
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
