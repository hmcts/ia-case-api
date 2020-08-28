package uk.gov.hmcts.reform.iacaseapi.infrastructure.eventvalidation;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.JourneyType.AIP;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.JourneyType.REP;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.assertj.core.groups.Tuple;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.JourneyType;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.util.LoggerUtil;

@RunWith(MockitoJUnitRunner.class)
public class AsylumCaseEventValidForJourneyTypeCheckerTest {
    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    private AsylumCaseEventValidForJourneyTypeChecker asylumCaseEventValidForJourneyTypeChecker = new AsylumCaseEventValidForJourneyTypeChecker();

    private ListAppender<ILoggingEvent> loggingEventListAppender;

    @Before
    public void setUp() {

        loggingEventListAppender = LoggerUtil.getListAppenderForClass(AsylumCaseEventValidForJourneyTypeChecker.class);
    }

    private void setupCallback(Event sendDirection, JourneyType journeyType) {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(sendDirection);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(AsylumCaseFieldDefinition.JOURNEY_TYPE)).thenReturn(Optional.of(journeyType));
    }

    @Test
    public void canSendValidEvents() {
        setupCallback(Event.CHANGE_DIRECTION_DUE_DATE, AIP);

        EventValid check = asylumCaseEventValidForJourneyTypeChecker.check(callback);

        assertThat(check.isValid(), is(true));
    }

    @Test
    public void cannotSendInvalidEventsForAip() {
        setupCallback(Event.REQUEST_CASE_BUILDING, AIP);

        EventValid check = asylumCaseEventValidForJourneyTypeChecker.check(callback);

        assertThat(check.isValid(), is(false));
        assertThat(check.getInvalidReason(), is("You've made an invalid request. The hearing must be submitted by a representative to make this request."));

        Assertions.assertThat(loggingEventListAppender.list)
            .extracting(ILoggingEvent::getMessage, ILoggingEvent::getLevel)
            .contains(Tuple.tuple("[requestCaseBuilding] is invalid for case id [0] the hearing must be submitted by a representative to handle this event.", Level.ERROR));
    }

    @Test
    public void cannotSendInvalidEventsForLegalRep() {
        setupCallback(Event.REQUEST_REASONS_FOR_APPEAL, REP);

        EventValid check = asylumCaseEventValidForJourneyTypeChecker.check(callback);

        assertThat(check.isValid(), is(false));
        assertThat(check.getInvalidReason(), is("You've made an invalid request. The hearing must be submitted by an appellant to make this request."));

        Assertions.assertThat(loggingEventListAppender.list)
            .extracting(ILoggingEvent::getMessage, ILoggingEvent::getLevel)
            .contains(Tuple.tuple("[requestReasonsForAppeal] is invalid for case id [0] the hearing must be submitted by an appellant to handle this event.", Level.ERROR));
    }
}
