package uk.gov.hmcts.reform.iacaseapi.infrastructure.eventvalidation;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.JourneyType.AIP;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.JourneyType.REP;

import java.util.Optional;
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

@RunWith(MockitoJUnitRunner.class)
public class AsylumCaseEventValidForJourneyTypeCheckerTest {
    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    private AsylumCaseEventValidForJourneyTypeChecker asylumCaseEventValidForJourneyTypeChecker = new AsylumCaseEventValidForJourneyTypeChecker();


    private void setupCallback(Event sendDirection, JourneyType journeyType) {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(sendDirection);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(AsylumCaseFieldDefinition.JOURNEY_TYPE)).thenReturn(Optional.of(journeyType));
    }

    @Test
    public void canSendValidEvents() {
        setupCallback(Event.SEND_DIRECTION, AIP);

        EventValidForJourneyType check = asylumCaseEventValidForJourneyTypeChecker.check(callback);

        assertThat(check.isValid(), is(true));
    }

    @Test
    public void cannotSendInvalidEventsForAip() {
        setupCallback(Event.REQUEST_CASE_BUILDING, AIP);

        EventValidForJourneyType check = asylumCaseEventValidForJourneyTypeChecker.check(callback);

        assertThat(check.isValid(), is(false));
        assertThat(check.getInvalidReason(), is("You've made an invalid request. The hearing must be submitted by a representative to make this request."));
    }

    @Test
    public void cannotSendInvalidEventsForLegalRep() {
        setupCallback(Event.REQUEST_REASONS_FOR_APPEAL, REP);

        EventValidForJourneyType check = asylumCaseEventValidForJourneyTypeChecker.check(callback);

        assertThat(check.isValid(), is(false));
        assertThat(check.getInvalidReason(), is("You've made an invalid request. The hearing must be submitted by an appellant to make this request."));
    }

}
