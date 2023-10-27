package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_DETAILS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.NO;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.WitnessDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.service.WitnessesService;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class WitnessesServiceTest {

    private static final String WITNESS_1_PARTY_ID = "111222333";
    private static final String WITNESS_2_PARTY_ID = "222111333";

    @Captor
    private ArgumentCaptor<List<IdValue<WitnessDetails>>> witnessDetailsCaptor;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private List<IdValue<WitnessDetails>> witnessDetails;

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {
        "UPDATE_HEARING_REQUIREMENTS",
        "DRAFT_HEARING_REQUIREMENTS"
    })
    void should_append_witnesses_party_id(Event event) {
        witnessDetails = Arrays.asList(
            new IdValue<>("1", new WitnessDetails("witness1", "family1")),
            new IdValue<>("2", new WitnessDetails("witness2", "family2"))
        );

        when(asylumCase.read(WITNESS_DETAILS)).thenReturn(Optional.of(witnessDetails));

        WitnessesService.appendWitnessPartyId(asylumCase);

        verify(asylumCase).write(eq(WITNESS_DETAILS), witnessDetailsCaptor.capture());
        assertNotNull(witnessDetailsCaptor.getValue().get(0).getValue().getWitnessPartyId());
        assertNotNull(witnessDetailsCaptor.getValue().get(1).getValue().getWitnessPartyId());
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {
        "UPDATE_HEARING_REQUIREMENTS",
        "DRAFT_HEARING_REQUIREMENTS"
    })
    void should_not_append_witnesses_party_id(Event event) {
        WitnessDetails witnessDetails1 = new WitnessDetails(WITNESS_1_PARTY_ID, "witness1", "family1", NO);
        WitnessDetails witnessDetails2 = new WitnessDetails(WITNESS_2_PARTY_ID, "witness2", "family2", NO);
        witnessDetails = Arrays.asList(
            new IdValue<>("1", witnessDetails1),
            new IdValue<>("2", witnessDetails2)
        );

        when(asylumCase.read(WITNESS_DETAILS)).thenReturn(Optional.of(witnessDetails));

        WitnessesService.appendWitnessPartyId(asylumCase);

        verify(asylumCase).write(eq(WITNESS_DETAILS), witnessDetailsCaptor.capture());
        assertEquals(WITNESS_1_PARTY_ID, witnessDetailsCaptor.getValue().get(0).getValue().getWitnessPartyId());
        assertEquals(WITNESS_2_PARTY_ID, witnessDetailsCaptor.getValue().get(1).getValue().getWitnessPartyId());
    }
}