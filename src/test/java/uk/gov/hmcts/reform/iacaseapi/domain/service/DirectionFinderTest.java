package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.DIRECTIONS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.DirectionTag.REQUEST_CLARIFYING_QUESTIONS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.DirectionTag.REQUEST_REASONS_FOR_APPEAL;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.Parties.APPELLANT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State.AWAITING_CLARIFYING_QUESTIONS_ANSWERS;

import java.util.Arrays;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import org.mockito.Mockito;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Direction;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DirectionTag;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;

public class DirectionFinderTest {
    @Test
    public void findsCorrectDirectionForState() {
        DirectionFinder directionFinder = new DirectionFinder();

        AsylumCase asylumCase = Mockito.mock(AsylumCase.class);
        IdValue<Direction> otherDirection = createDirection("1", REQUEST_REASONS_FOR_APPEAL);
        IdValue<Direction> expectedDirection = createDirection("2", REQUEST_CLARIFYING_QUESTIONS);
        when(asylumCase.read(DIRECTIONS)).thenReturn(Optional.of(Arrays.asList(otherDirection, expectedDirection)));

        Optional<IdValue<Direction>> updatableDirectionForState =
                directionFinder.getUpdatableDirectionForState(AWAITING_CLARIFYING_QUESTIONS_ANSWERS, asylumCase);

        assertThat(updatableDirectionForState, is(Optional.of(expectedDirection)));
    }

    @Test
    public void findsLastDirectionForState() {
        DirectionFinder directionFinder = new DirectionFinder();

        AsylumCase asylumCase = Mockito.mock(AsylumCase.class);
        IdValue<Direction> otherDirection = createDirection("1", REQUEST_CLARIFYING_QUESTIONS);
        IdValue<Direction> expectedDirection = createDirection("2", REQUEST_CLARIFYING_QUESTIONS);
        when(asylumCase.read(DIRECTIONS)).thenReturn(Optional.of(Arrays.asList(otherDirection, expectedDirection)));

        Optional<IdValue<Direction>> updatableDirectionForState =
                directionFinder.getUpdatableDirectionForState(AWAITING_CLARIFYING_QUESTIONS_ANSWERS, asylumCase);

        assertThat(updatableDirectionForState, is(Optional.of(expectedDirection)));
    }

    @Test
    public void doesNotFindADirection() {
        DirectionFinder directionFinder = new DirectionFinder();

        AsylumCase asylumCase = Mockito.mock(AsylumCase.class);
        IdValue<Direction> otherDirection = createDirection("1", REQUEST_REASONS_FOR_APPEAL);

        when(asylumCase.read(DIRECTIONS)).thenReturn(Optional.of(singletonList(otherDirection)));

        Optional<IdValue<Direction>> updatableDirectionForState =
                directionFinder.getUpdatableDirectionForState(AWAITING_CLARIFYING_QUESTIONS_ANSWERS, asylumCase);

        assertThat(updatableDirectionForState, is(Optional.empty()));
    }

    @NotNull
    private IdValue<Direction> createDirection(String id, DirectionTag directionTag) {
        return new IdValue<>(id,
                new Direction("explanation", APPELLANT, "2020-01-01", "2020-02-02", directionTag, emptyList())
        );
    }
}
