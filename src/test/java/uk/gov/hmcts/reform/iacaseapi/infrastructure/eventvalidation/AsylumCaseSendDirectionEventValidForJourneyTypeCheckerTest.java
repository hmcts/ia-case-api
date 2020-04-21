package uk.gov.hmcts.reform.iacaseapi.infrastructure.eventvalidation;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Parties;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.JourneyType;

@RunWith(MockitoJUnitRunner.class)
public class AsylumCaseSendDirectionEventValidForJourneyTypeCheckerTest {
    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;

    @Test
    public void cannotSendDirectionForAipCase() {
        setupCallback(Event.SEND_DIRECTION, JourneyType.AIP, Parties.APPELLANT);
        EventValid eventValid = new AsylumCaseSendDirectionEventValidForJourneyTypeChecker().check(callback);

        assertThat(eventValid, is(new EventValid("You cannot use this function to send a direction to an appellant in person.")));
    }

    @Test
    public void canSendDirectionForAipCaseToRespondent() {
        setupCallback(Event.SEND_DIRECTION, JourneyType.AIP, Parties.RESPONDENT);
        EventValid eventValid = new AsylumCaseSendDirectionEventValidForJourneyTypeChecker().check(callback);

        assertThat(eventValid, is(EventValid.VALID_EVENT));
    }

    @Test
    public void cannotSendDirectionToAppellantForReppedCase() {
        setupCallback(Event.SEND_DIRECTION, JourneyType.REP, Parties.APPELLANT);
        EventValid eventValid = new AsylumCaseSendDirectionEventValidForJourneyTypeChecker().check(callback);

        assertThat(eventValid, is(new EventValid("This is a legally represented case. You cannot select appellant as the recipient.")));
    }

    @Test
    public void canSendDirectionToLegalRepForReppedCase() {
        setupCallback(Event.SEND_DIRECTION, JourneyType.REP, Parties.LEGAL_REPRESENTATIVE);
        EventValid eventValid = new AsylumCaseSendDirectionEventValidForJourneyTypeChecker().check(callback);

        assertThat(eventValid, is(EventValid.VALID_EVENT));
    }

    private void setupCallback(Event event, JourneyType journeyType, Parties party) {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(event);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(AsylumCaseFieldDefinition.JOURNEY_TYPE)).thenReturn(Optional.of(journeyType));
        when(asylumCase.read(AsylumCaseFieldDefinition.SEND_DIRECTION_PARTIES, Parties.class)).thenReturn(Optional.of(party));
    }
}
