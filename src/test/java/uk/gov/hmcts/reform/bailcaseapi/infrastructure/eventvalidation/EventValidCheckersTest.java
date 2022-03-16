package uk.gov.hmcts.reform.bailcaseapi.infrastructure.eventvalidation;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.bailcaseapi.infrastructure.eventvalidation.EventValid.VALID_EVENT;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;

@ExtendWith(MockitoExtension.class)
class EventValidCheckersTest {

    @Mock
    private Callback<BailCase> callback;
    @Mock
    private EventValidChecker<BailCase> eventValidChecker1;
    @Mock
    private EventValidChecker<BailCase> eventValidChecker2;
    private EventValidCheckers<BailCase> bailCaseEventValidCheckers;

    @BeforeEach
    public void setUp() {
        List<EventValidChecker<BailCase>> checkers = asList(eventValidChecker1, eventValidChecker2);
        bailCaseEventValidCheckers = new EventValidCheckers<>(checkers);
    }

    @Test
    void eventIsValid() {
        when(eventValidChecker1.check(callback)).thenReturn(VALID_EVENT);
        when(eventValidChecker2.check(callback)).thenReturn(VALID_EVENT);

        EventValid eventValid = bailCaseEventValidCheckers.check(callback);

        assertThat(eventValid).isEqualTo(VALID_EVENT);
    }

    @Test
    void eventIsInValid() {
        when(eventValidChecker1.check(callback)).thenReturn(VALID_EVENT);
        EventValid invalidEvent = new EventValid("Some error");
        when(eventValidChecker2.check(callback)).thenReturn(invalidEvent);

        EventValid eventValid = bailCaseEventValidCheckers.check(callback);

        assertThat(eventValid).isEqualTo(invalidEvent);
    }
}
