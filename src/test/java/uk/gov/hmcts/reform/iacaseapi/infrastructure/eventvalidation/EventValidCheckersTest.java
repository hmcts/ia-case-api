package uk.gov.hmcts.reform.iacaseapi.infrastructure.eventvalidation;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.hmcts.reform.iacaseapi.infrastructure.eventvalidation.EventValid.VALID_EVENT;

import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;

@RunWith(MockitoJUnitRunner.class)
public class EventValidCheckersTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private EventValidChecker<AsylumCase> eventValidChecker1;
    @Mock
    private EventValidChecker<AsylumCase> eventValidChecker2;
    private EventValidCheckers<AsylumCase> asylumCaseEventValidChecker;

    @Before
    public void setUp() {
        List<EventValidChecker<AsylumCase>> checkers = asList(eventValidChecker1, eventValidChecker2);
        asylumCaseEventValidChecker = new EventValidCheckers<AsylumCase>(checkers);
    }

    @Test
    public void eventIsValid() {
        Mockito.when(eventValidChecker1.check(callback)).thenReturn(VALID_EVENT);
        Mockito.when(eventValidChecker2.check(callback)).thenReturn(VALID_EVENT);

        EventValid eventValid = asylumCaseEventValidChecker.check(callback);

        assertThat(eventValid, is(VALID_EVENT));
    }

    @Test
    public void eventIsInValid() {
        Mockito.when(eventValidChecker1.check(callback)).thenReturn(VALID_EVENT);
        EventValid invalidEvent = new EventValid("Some error");
        Mockito.when(eventValidChecker2.check(callback)).thenReturn(invalidEvent);

        EventValid eventValid = asylumCaseEventValidChecker.check(callback);

        assertThat(eventValid, is(invalidEvent));
    }
}
