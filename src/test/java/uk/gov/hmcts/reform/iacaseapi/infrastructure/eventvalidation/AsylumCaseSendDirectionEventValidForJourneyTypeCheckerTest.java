package uk.gov.hmcts.reform.iacaseapi.infrastructure.eventvalidation;

import static org.assertj.core.api.Assertions.assertThat;
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Parties;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.JourneyType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.util.LoggerUtil;

@ExtendWith(MockitoExtension.class)
class AsylumCaseSendDirectionEventValidForJourneyTypeCheckerTest {
    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;

    private ListAppender<ILoggingEvent> loggingEventListAppender;

    @BeforeEach
    public void setUp() {

        loggingEventListAppender =
            LoggerUtil.getListAppenderForClass(AsylumCaseSendDirectionEventValidForJourneyTypeChecker.class);
    }

    @Test
    void canSendDirectionForAipCaseToAppellant() {
        setupCallback(Event.SEND_DIRECTION, JourneyType.AIP, Parties.APPELLANT);
        EventValid eventValid = new AsylumCaseSendDirectionEventValidForJourneyTypeChecker().check(callback);

        assertThat(eventValid).isEqualTo(EventValid.VALID_EVENT);
    }

    @Test
    void canSendDirectionForAipCaseToRespondent() {
        setupCallback(Event.SEND_DIRECTION, JourneyType.AIP, Parties.RESPONDENT);
        EventValid eventValid = new AsylumCaseSendDirectionEventValidForJourneyTypeChecker().check(callback);

        assertThat(eventValid).isEqualTo(EventValid.VALID_EVENT);
    }

    @ParameterizedTest
    @EnumSource(value = Parties.class, names = {
        "LEGAL_REPRESENTATIVE", "BOTH"
    })
    void cannotSendDirectionToLegalRepForAipCase(Parties parties) {
        setupCallback(Event.SEND_DIRECTION, JourneyType.AIP, parties);
        EventValid eventValid = new AsylumCaseSendDirectionEventValidForJourneyTypeChecker().check(callback);

        assertThat(eventValid).isEqualTo(
                new EventValid("This is an appellant in person case. You cannot select legal representative as the recipient."));

        Assertions.assertThat(loggingEventListAppender.list)
                .extracting(ILoggingEvent::getMessage, ILoggingEvent::getLevel)
                .contains(Tuple.tuple("Cannot send a legal representative a direction for an appellant in person case", Level.ERROR));
    }

    @Test
    void cannotSendDirectionToAppellantForReppedCase() {
        setupCallback(Event.SEND_DIRECTION, JourneyType.REP, Parties.APPELLANT);
        EventValid eventValid = new AsylumCaseSendDirectionEventValidForJourneyTypeChecker().check(callback);

        assertThat(eventValid).isEqualTo(
            new EventValid("This is a legally represented case. You cannot select appellant as the recipient."));

        Assertions.assertThat(loggingEventListAppender.list)
            .extracting(ILoggingEvent::getMessage, ILoggingEvent::getLevel)
            .contains(Tuple.tuple("Cannot send an appellant a direction for a repped case", Level.ERROR));
    }

    @Test
    void cannotSendDirectionToAppellantAndRespondentForReppedCase() {
        setupCallback(Event.SEND_DIRECTION, JourneyType.REP, Parties.APPELLANT_AND_RESPONDENT);
        EventValid eventValid = new AsylumCaseSendDirectionEventValidForJourneyTypeChecker().check(callback);

        assertThat(eventValid).isEqualTo(
                new EventValid("This is a legally represented case. You cannot select appellant as the recipient."));

        Assertions.assertThat(loggingEventListAppender.list)
                .extracting(ILoggingEvent::getMessage, ILoggingEvent::getLevel)
                .contains(Tuple.tuple("Cannot send an appellant a direction for a repped case", Level.ERROR));
    }

    @Test
    void canSendDirectionToLegalRepForReppedCase() {
        setupCallback(Event.SEND_DIRECTION, JourneyType.REP, Parties.LEGAL_REPRESENTATIVE);
        EventValid eventValid = new AsylumCaseSendDirectionEventValidForJourneyTypeChecker().check(callback);

        assertThat(eventValid).isEqualTo(EventValid.VALID_EVENT);
    }

    private void setupCallback(Event event, JourneyType journeyType, Parties party) {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(event);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(AsylumCaseFieldDefinition.JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(journeyType));
        when(asylumCase.read(AsylumCaseFieldDefinition.SEND_DIRECTION_PARTIES, Parties.class))
            .thenReturn(Optional.of(party));
    }

    @ParameterizedTest
    @EnumSource(value = Parties.class, names = {
        "LEGAL_REPRESENTATIVE", "BOTH"
    })
    void cannotSendDirectionToLegalRepForInternalCase(Parties party) {
        setupInternalCaseCallback(party);
        EventValid eventValid = new AsylumCaseSendDirectionEventValidForJourneyTypeChecker().check(callback);

        assertThat(eventValid).isEqualTo(
                new EventValid("This is an appellant in person case. You cannot select legal representative as the recipient."));

        Assertions.assertThat(loggingEventListAppender.list)
                .extracting(ILoggingEvent::getMessage, ILoggingEvent::getLevel)
                .contains(Tuple.tuple("Cannot send legal representative a direction for an internal case", Level.ERROR));
    }

    @Test
    void canSendDirectionToRespondentForInternalCase() {
        setupInternalCaseCallback(Parties.RESPONDENT);
        EventValid eventValid = new AsylumCaseSendDirectionEventValidForJourneyTypeChecker().check(callback);

        assertThat(eventValid).isEqualTo(EventValid.VALID_EVENT);
    }

    private void setupInternalCaseCallback(Parties party) {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.SEND_DIRECTION);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(AsylumCaseFieldDefinition.JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.empty());
        when(asylumCase.read(AsylumCaseFieldDefinition.IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(AsylumCaseFieldDefinition.SEND_DIRECTION_PARTIES, Parties.class))
                .thenReturn(Optional.of(party));
    }
}
