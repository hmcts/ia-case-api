package uk.gov.hmcts.reform.iacaseapi.infrastructure.eventvalidation;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Parties;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.JourneyType;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.util.LoggerUtil;

@ExtendWith(MockitoExtension.class)
class AsylumCaseSendDirectionEventValidForJourneyTypeCheckerTest {
    @Mock private
    Callback<AsylumCase> callback;
    @Mock private
    CaseDetails<AsylumCase> caseDetails;
    @Mock private
    AsylumCase asylumCase;

    ListAppender<ILoggingEvent> loggingEventListAppender;

    @BeforeEach
    void setUp() {

        loggingEventListAppender = LoggerUtil.getListAppenderForClass(AsylumCaseSendDirectionEventValidForJourneyTypeChecker.class);
    }

    @Test
    void cannotSendDirectionForAipCase() {
        setupCallback(Event.SEND_DIRECTION, JourneyType.AIP, Parties.APPELLANT);
        EventValid eventValid = new AsylumCaseSendDirectionEventValidForJourneyTypeChecker().check(callback);

        assertThat(eventValid, is(new EventValid("You cannot use this function to send a direction to an appellant in person.")));

        Assertions.assertThat(loggingEventListAppender.list)
            .extracting(ILoggingEvent::getMessage, ILoggingEvent::getLevel)
            .contains(Tuple.tuple("Cannot send a direction for an AIP case", Level.ERROR));
    }

    @Test
    void canSendDirectionForAipCaseToRespondent() {
        setupCallback(Event.SEND_DIRECTION, JourneyType.AIP, Parties.RESPONDENT);
        EventValid eventValid = new AsylumCaseSendDirectionEventValidForJourneyTypeChecker().check(callback);

        assertThat(eventValid, is(EventValid.VALID_EVENT));
    }

    @Test
    void cannotSendDirectionToAppellantForReppedCase() {
        setupCallback(Event.SEND_DIRECTION, JourneyType.REP, Parties.APPELLANT);
        EventValid eventValid = new AsylumCaseSendDirectionEventValidForJourneyTypeChecker().check(callback);

        assertThat(eventValid, is(new EventValid("This is a legally represented case. You cannot select appellant as the recipient.")));

        Assertions.assertThat(loggingEventListAppender.list)
            .extracting(ILoggingEvent::getMessage, ILoggingEvent::getLevel)
            .contains(Tuple.tuple("Cannot send an appellant a direction for a repped case", Level.ERROR));
    }

    @Test
    void canSendDirectionToLegalRepForReppedCase() {
        setupCallback(Event.SEND_DIRECTION, JourneyType.REP, Parties.LEGAL_REPRESENTATIVE);
        EventValid eventValid = new AsylumCaseSendDirectionEventValidForJourneyTypeChecker().check(callback);

        assertThat(eventValid, is(EventValid.VALID_EVENT));
    }

    void setupCallback(Event event, JourneyType journeyType, Parties party) {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(event);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(AsylumCaseFieldDefinition.JOURNEY_TYPE)).thenReturn(Optional.of(journeyType));
        when(asylumCase.read(AsylumCaseFieldDefinition.SEND_DIRECTION_PARTIES, Parties.class)).thenReturn(Optional.of(party));
    }
}
