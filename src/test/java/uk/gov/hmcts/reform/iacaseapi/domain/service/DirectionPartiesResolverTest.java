package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.SEND_DIRECTION_PARTIES;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Parties;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class DirectionPartiesResolverTest {

    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;

    private DirectionPartiesResolver directionPartiesResolver;

    @Before
    public void setUp() {
        directionPartiesResolver = new DirectionPartiesResolver();
    }

    @Test
    public void should_return_parties_for_send_direction_events() {

        Parties expectedDirectionParties = Parties.BOTH;

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(SEND_DIRECTION_PARTIES)).thenReturn(Optional.of(expectedDirectionParties));

        Map<Event, Parties> exampleInputOutputs =
            ImmutableMap
                .<Event, Parties>builder()
                .put(Event.SEND_DIRECTION, expectedDirectionParties)
                .put(Event.REQUEST_CASE_EDIT, Parties.LEGAL_REPRESENTATIVE)
                .put(Event.REQUEST_RESPONDENT_EVIDENCE, Parties.RESPONDENT)
                .put(Event.REQUEST_RESPONDENT_REVIEW, Parties.RESPONDENT)
                .put(Event.REQUEST_RESPONSE_REVIEW, Parties.LEGAL_REPRESENTATIVE)
                .put(Event.REQUEST_RESPONSE_AMEND, Parties.RESPONDENT)
                .put(Event.REQUEST_REASONS_FOR_APPEAL, Parties.APPELLANT)
                .put(Event.REQUEST_NEW_HEARING_REQUIREMENTS, Parties.LEGAL_REPRESENTATIVE)
                .build();

        exampleInputOutputs
            .entrySet()
            .forEach(inputOutput -> {

                final Event event = inputOutput.getKey();
                final Parties expectedParties = inputOutput.getValue();

                when(callback.getEvent()).thenReturn(event);

                Parties actualDirectionParties = directionPartiesResolver.resolve(callback);

                assertEquals(expectedParties, actualDirectionParties);

                reset(callback);
            });
    }

    @Test
    public void should_throw_when_callback_is_not_for_sending_a_direction() {

        when(callback.getEvent()).thenReturn(Event.ADD_APPEAL_RESPONSE);

        assertThatThrownBy(() -> directionPartiesResolver.resolve(callback))
            .hasMessage("Callback event is not for sending a direction")
            .isExactlyInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> directionPartiesResolver.resolve(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
