package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static java.util.Collections.emptyList;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.DIRECTIONS;

import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Direction;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DirectionTag;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;

@Service
public class DirectionFinder {
    private static final Map<State, DirectionTag> stateToDirectionTag = ImmutableMap.of(
            State.AWAITING_REASONS_FOR_APPEAL, DirectionTag.REQUEST_REASONS_FOR_APPEAL,
            State.AWAITING_CLARIFYING_QUESTIONS_ANSWERS, DirectionTag.REQUEST_CLARIFYING_QUESTIONS
    );

    public Optional<IdValue<Direction>> getUpdatableDirectionForState(State state, AsylumCase asylumCase) {
        Optional<List<IdValue<Direction>>> maybeDirections = asylumCase.read(DIRECTIONS);

        return maybeDirections.orElse(emptyList())
                .stream()
                .filter(directionIdVale -> directionIdVale.getValue().getTag().equals(stateToDirectionTag.get(state)))
                .reduce((first, second) -> second);
    }
}
