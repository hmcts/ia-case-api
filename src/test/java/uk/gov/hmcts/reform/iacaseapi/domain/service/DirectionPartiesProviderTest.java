package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.JOURNEY_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.Parties.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Value;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.JourneyType;


@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class DirectionPartiesProviderTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private DirectionPartiesProvider directionPartiesProvider;

    @BeforeEach
    public void setup() {

        directionPartiesProvider = new DirectionPartiesProvider(userDetails);

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.SEND_DIRECTION);
    }

    @Test
    void should_prepare_roles_for_direction_parties_list() {
        final List<Value> values = new ArrayList<>();
        Collections.addAll(values,
                new Value(LEGAL_REPRESENTATIVE.name(), LEGAL_REPRESENTATIVE.toString()),
                new Value(RESPONDENT.name(), RESPONDENT.toString()),
                new Value(BOTH.name(), BOTH.toString()),
                new Value(APPELLANT.name(), APPELLANT.toString()));
        DynamicList actualList =
                new DynamicList(values.get(0), values);

        DynamicList expectedList = directionPartiesProvider.getDirectionParties(callback);
        assertNotNull(expectedList);
        assertThat(expectedList).isEqualTo(actualList);
    }

    @Test
    void should_prepare_roles_for_direction_parties_for_aip_list() {

        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.AIP));

        final List<Value> values = new ArrayList<>();
        Collections.addAll(values,
                new Value(RESPONDENT.name(), RESPONDENT.toString()),
                new Value(APPELLANT.name(), APPELLANT.toString()),
                new Value(RESPONDENT_AND_APPELLANT.name(), RESPONDENT_AND_APPELLANT.toString()));
        DynamicList actualList =
                new DynamicList(values.get(0), values);

        DynamicList expectedList = directionPartiesProvider.getDirectionParties(callback);
        assertNotNull(expectedList);
        assertThat(expectedList).isEqualTo(actualList);
    }

}
