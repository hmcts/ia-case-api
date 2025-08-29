package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_IN_UK;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_PARTY_ID;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HAS_SPONSOR;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.JOURNEY_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.LEGAL_REP_INDIVIDUAL_PARTY_ID;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.LEGAL_REP_ORGANISATION_PARTY_ID;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.SPONSOR_PARTY_ID;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_DETAILS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.WitnessDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.JourneyType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.service.PartyIdService;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class PartyIdServiceTest {

    private static final String WITNESS_1_PARTY_ID = "111222333";
    private static final String WITNESS_2_PARTY_ID = "222111333";
    private static final String PARTY_ID = "111222333";

    @Mock
    private AsylumCase asylumCase;
    @Mock
    private List<IdValue<WitnessDetails>> witnessDetails;
    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Captor
    private ArgumentCaptor<String> partyId;
    @Captor
    private ArgumentCaptor<List<IdValue<WitnessDetails>>> witnessDetailsCaptor;

    private Pattern partyIdRegexPattern;

    @BeforeEach
    public void setUp() {

        partyIdRegexPattern = Pattern.compile(
            "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

    }

    @Test
    void should_append_witnesses_party_id() {
        witnessDetails = Arrays.asList(
            new IdValue<>("1", new WitnessDetails("witness1", "family1")),
            new IdValue<>("2", new WitnessDetails("witness2", "family2"))
        );

        when(asylumCase.read(WITNESS_DETAILS)).thenReturn(Optional.of(witnessDetails));

        PartyIdService.appendWitnessPartyId(asylumCase);

        verify(asylumCase).write(eq(WITNESS_DETAILS), witnessDetailsCaptor.capture());
        assertNotNull(witnessDetailsCaptor.getValue().get(0).getValue().getWitnessPartyId());
        assertNotNull(witnessDetailsCaptor.getValue().get(1).getValue().getWitnessPartyId());
    }

    @Test
    void should_not_append_witnesses_party_id() {
        WitnessDetails witnessDetails1 = new WitnessDetails(WITNESS_1_PARTY_ID, "witness1", "family1", NO);
        WitnessDetails witnessDetails2 = new WitnessDetails(WITNESS_2_PARTY_ID, "witness2", "family2", NO);
        witnessDetails = Arrays.asList(
            new IdValue<>("1", witnessDetails1),
            new IdValue<>("2", witnessDetails2)
        );

        when(asylumCase.read(WITNESS_DETAILS)).thenReturn(Optional.of(witnessDetails));

        PartyIdService.appendWitnessPartyId(asylumCase);

        verify(asylumCase).write(eq(WITNESS_DETAILS), witnessDetailsCaptor.capture());
        assertEquals(WITNESS_1_PARTY_ID, witnessDetailsCaptor.getValue().get(0).getValue().getWitnessPartyId());
        assertEquals(WITNESS_2_PARTY_ID, witnessDetailsCaptor.getValue().get(1).getValue().getWitnessPartyId());
    }

    @Test
    void should_set_appellant_partyId() {

        PartyIdService.setAppellantPartyId(asylumCase);

        verify(asylumCase).write(eq(APPELLANT_PARTY_ID), partyId.capture());

        assertTrue(partyIdRegexPattern.matcher(partyId.getValue()).matches());

    }

    @Test
    void should_not_set_appellant_partyId() {

        when(asylumCase.read(APPELLANT_PARTY_ID, String.class)).thenReturn(Optional.of(PARTY_ID));

        PartyIdService.setAppellantPartyId(asylumCase);

        verify(asylumCase, never()).write(eq(APPELLANT_PARTY_ID), anyString());

    }

    @Test
    void should_set_legal_rep_partyId() {

        PartyIdService.setLegalRepPartyId(asylumCase);

        verify(asylumCase).write(eq(LEGAL_REP_INDIVIDUAL_PARTY_ID), partyId.capture());
        assertTrue(partyIdRegexPattern.matcher(partyId.getValue()).matches());

        verify(asylumCase).write(eq(LEGAL_REP_ORGANISATION_PARTY_ID), partyId.capture());
        assertTrue(partyIdRegexPattern.matcher(partyId.getValue()).matches());

    }

    @Test
    void should_set_only_legal_rep_individual_partyId() {

        when(asylumCase.read(LEGAL_REP_ORGANISATION_PARTY_ID, String.class)).thenReturn(Optional.of(PARTY_ID));

        PartyIdService.setLegalRepPartyId(asylumCase);

        verify(asylumCase).write(eq(LEGAL_REP_INDIVIDUAL_PARTY_ID), partyId.capture());
        assertTrue(partyIdRegexPattern.matcher(partyId.getValue()).matches());
        verify(asylumCase, never()).write(eq(LEGAL_REP_ORGANISATION_PARTY_ID), anyString());
    }

    @Test
    void should_set_only_legal_rep_org_partyId() {

        when(asylumCase.read(LEGAL_REP_INDIVIDUAL_PARTY_ID, String.class)).thenReturn(Optional.of("111222333"));

        PartyIdService.setLegalRepPartyId(asylumCase);

        verify(asylumCase).write(eq(LEGAL_REP_ORGANISATION_PARTY_ID), partyId.capture());
        assertTrue(partyIdRegexPattern.matcher(partyId.getValue()).matches());
        verify(asylumCase, never()).write(eq(LEGAL_REP_INDIVIDUAL_PARTY_ID), anyString());

    }

    @Test
    void should_not_set_legal_rep_partyId_when_aip_journey() {

        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.AIP));

        PartyIdService.setLegalRepPartyId(asylumCase);

        verify(asylumCase, never()).write(eq(LEGAL_REP_INDIVIDUAL_PARTY_ID), anyString());
        verify(asylumCase, never()).write(eq(LEGAL_REP_ORGANISATION_PARTY_ID), anyString());

    }

    @Test
    void should_not_set_legal_rep_partyId() {

        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.AIP));
        when(asylumCase.read(LEGAL_REP_INDIVIDUAL_PARTY_ID, String.class)).thenReturn(Optional.of("111222333"));
        when(asylumCase.read(LEGAL_REP_ORGANISATION_PARTY_ID, String.class)).thenReturn(Optional.of("111222333"));

        PartyIdService.setLegalRepPartyId(asylumCase);

        verify(asylumCase, never()).write(eq(LEGAL_REP_INDIVIDUAL_PARTY_ID), anyString());
        verify(asylumCase, never()).write(eq(LEGAL_REP_ORGANISATION_PARTY_ID), anyString());

    }

    @Test
    void should_set_sponsor_partyId() {

        when(asylumCase.read(APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(NO));
        when(asylumCase.read(HAS_SPONSOR, YesOrNo.class)).thenReturn(Optional.of(YES));

        PartyIdService.setSponsorPartyId(asylumCase);

        verify(asylumCase).write(eq(SPONSOR_PARTY_ID), partyId.capture());
        assertTrue(partyIdRegexPattern.matcher(partyId.getValue()).matches());

    }

    @Test
    void should_not_set_sponsor_partyId_when_appellant_has_no_sponsor() {

        when(asylumCase.read(HAS_SPONSOR, YesOrNo.class)).thenReturn(Optional.of(NO));

        PartyIdService.setSponsorPartyId(asylumCase);

        verify(asylumCase, never()).write(eq(SPONSOR_PARTY_ID), anyString());

    }

    @Test
    void should_not_set_sponsor_partyId_when_in_country_and_appellant_has_no_sponsor() {

        when(asylumCase.read(HAS_SPONSOR, YesOrNo.class)).thenReturn(Optional.of(NO));

        PartyIdService.setSponsorPartyId(asylumCase);

        verify(asylumCase, never()).write(eq(SPONSOR_PARTY_ID), anyString());

    }

    @Test
    void should_not_set_sponsor_partyId_when_already_set() {

        when(asylumCase.read(APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(NO));
        when(asylumCase.read(HAS_SPONSOR, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(SPONSOR_PARTY_ID, String.class)).thenReturn(Optional.of(PARTY_ID));

        PartyIdService.setSponsorPartyId(asylumCase);

        verify(asylumCase, never()).write(eq(SPONSOR_PARTY_ID), anyString());

    }

    @Test
    void should_reset_legal_rep_partyId_when_aip_journey() {

        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.empty());

        PartyIdService.resetLegalRepPartyId(asylumCase);

        verify(asylumCase, times(1)).write(eq(LEGAL_REP_INDIVIDUAL_PARTY_ID), anyString());
        verify(asylumCase, times(1)).write(eq(LEGAL_REP_ORGANISATION_PARTY_ID), anyString());

    }
}
