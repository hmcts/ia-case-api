package uk.gov.hmcts.reform.iacaseapi.infrastructure.eventvalidation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.JourneyType.AIP;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.JourneyType.REP;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.JourneyType;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.util.LoggerUtil;

@ExtendWith(MockitoExtension.class)
class AsylumCaseEventValidForJourneyTypeCheckerTest {
    @Mock private
    Callback<AsylumCase> callback;
    @Mock private
    CaseDetails<AsylumCase> caseDetails;
    @Mock private
    AsylumCase asylumCase;
    AsylumCaseEventValidForJourneyTypeChecker asylumCaseEventValidForJourneyTypeChecker = new AsylumCaseEventValidForJourneyTypeChecker();

    ListAppender<ILoggingEvent> loggingEventListAppender;

    @BeforeEach
    void setUp() {

        loggingEventListAppender = LoggerUtil.getListAppenderForClass(AsylumCaseEventValidForJourneyTypeChecker.class);
    }

    void setupCallback(Event sendDirection, JourneyType journeyType) {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(sendDirection);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(AsylumCaseFieldDefinition.JOURNEY_TYPE)).thenReturn(Optional.of(journeyType));
    }

    @Test
    void canSendValidEvents() {
        setupCallback(Event.CHANGE_DIRECTION_DUE_DATE, AIP);

        EventValid check = asylumCaseEventValidForJourneyTypeChecker.check(callback);

        assertSame(check.isValid(), true);
    }

    @Test
    void cannotSendInvalidEventsForAip() {
        setupCallback(Event.REQUEST_CASE_BUILDING, AIP);

        EventValid check = asylumCaseEventValidForJourneyTypeChecker.check(callback);

        assertSame(check.isValid(), false);
        assertSame(check.getInvalidReason(), "You've made an invalid request. The hearing must be submitted by a representative to make this request.");

        Assertions.assertThat(loggingEventListAppender.list)
            .extracting(ILoggingEvent::getMessage, ILoggingEvent::getLevel)
            .contains(Tuple.tuple("[requestCaseBuilding] is invalid for case id [0] the hearing must be submitted by a representative to handle this event.", Level.ERROR));
    }

    @Test
    void cannotSendInvalidEventsForLegalRep() {
        setupCallback(Event.REQUEST_REASONS_FOR_APPEAL, REP);

        EventValid check = asylumCaseEventValidForJourneyTypeChecker.check(callback);

        assertSame(check.isValid(), false);
        assertSame(check.getInvalidReason(), "You've made an invalid request. The hearing must be submitted by an appellant to make this request.");

        Assertions.assertThat(loggingEventListAppender.list)
            .extracting(ILoggingEvent::getMessage, ILoggingEvent::getLevel)
            .contains(Tuple.tuple("[requestReasonsForAppeal] is invalid for case id [0] the hearing must be submitted by an appellant to handle this event.", Level.ERROR));
    }
}
