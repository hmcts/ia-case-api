package uk.gov.hmcts.reform.iacaseapi.infrastructure.eventvalidation;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.hmcts.reform.iacaseapi.infrastructure.eventvalidation.EventValid.VALID_EVENT;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;

@ExtendWith(MockitoExtension.class)
class EventValidCheckersTest {

    @Mock private
    Callback<AsylumCase> callback;
    @Mock private
    EventValidChecker<AsylumCase> eventValidChecker1;
    @Mock private
    EventValidChecker<AsylumCase> eventValidChecker2;
    EventValidCheckers<AsylumCase> asylumCaseEventValidChecker;

    @BeforeEach
    void setUp() {

        List<EventValidChecker<AsylumCase>> checkers = asList(eventValidChecker1, eventValidChecker2);
        asylumCaseEventValidChecker = new EventValidCheckers<AsylumCase>(checkers);
    }

    @Test
    void eventIsValid() {
        Mockito.when(eventValidChecker1.check(callback)).thenReturn(VALID_EVENT);
        Mockito.when(eventValidChecker2.check(callback)).thenReturn(VALID_EVENT);

        EventValid eventValid = asylumCaseEventValidChecker.check(callback);

        assertThat(eventValid, is(VALID_EVENT));
    }

    @Test
    void eventIsInValid() {
        Mockito.when(eventValidChecker1.check(callback)).thenReturn(VALID_EVENT);
        EventValid invalidEvent = new EventValid("Some error");
        Mockito.when(eventValidChecker2.check(callback)).thenReturn(invalidEvent);

        EventValid eventValid = asylumCaseEventValidChecker.check(callback);

        assertThat(eventValid, is(invalidEvent));
    }
}
