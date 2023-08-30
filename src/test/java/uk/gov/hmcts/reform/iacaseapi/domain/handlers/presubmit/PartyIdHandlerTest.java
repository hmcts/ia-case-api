package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.InterpreterLanguagesUtils.WITNESS_N_FIELD;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class PartyIdHandlerTest {

    private static final String APPELLANT_PARTY_ID_GENERATION = "111222333";
    private static final String LEGAL_REP_INDIVIDUAL_PARTY_ID_GENERATION = "222111333";
    private static final String LEGAL_REP_ORGANISATION_PARTY_ID_GENERATION = "333222111";
    private static final String SPONSOR_PARTY_ID_GENERATION = "444555666";

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Captor
    private ArgumentCaptor<String> partyId;
    @Captor
    private ArgumentCaptor<WitnessDetails> witness;
    @Mock
    private List<IdValue<WitnessDetails>> witnessDetails;

    private PartyIdHandler partyIdHandler;

    @BeforeEach
    public void setUp() {
        partyIdHandler = new PartyIdHandler();

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(asylumCase.read(APPELLANT_PARTY_ID, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(LEGAL_REP_INDIVIDUAL_PARTY_ID, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(LEGAL_REP_ORGANISATION_PARTY_ID, String.class)).thenReturn(Optional.empty());

        when(asylumCase.read(APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.empty());
        when(asylumCase.read(HAS_SPONSOR, YesOrNo.class)).thenReturn(Optional.empty());

    }

    @Test
    void should_generate_party_id_when_event_is_start_appeal_with_in_uk() {
        when(callback.getEvent()).thenReturn(START_APPEAL);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                partyIdHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).write(eq(APPELLANT_PARTY_ID), partyId.capture());
        assertNotNull(partyId.getValue());

        verify(asylumCase, times(1)).write(eq(LEGAL_REP_INDIVIDUAL_PARTY_ID), partyId.capture());
        assertNotNull(partyId.getValue());

        verify(asylumCase, times(1)).write(eq(LEGAL_REP_ORGANISATION_PARTY_ID), partyId.capture());
        assertNotNull(partyId.getValue());

    }

    @Test
    void should_generate_party_id_when_event_is_start_appeal_with_sponsor_and_not_in_uk() {
        when(callback.getEvent()).thenReturn(START_APPEAL);

        when(asylumCase.read(APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(HAS_SPONSOR, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(SPONSOR_PARTY_ID, String.class)).thenReturn(Optional.empty());

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                partyIdHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).write(eq(APPELLANT_PARTY_ID), partyId.capture());
        assertNotNull(partyId.getValue());

        verify(asylumCase, times(1)).write(eq(LEGAL_REP_INDIVIDUAL_PARTY_ID), partyId.capture());
        assertNotNull(partyId.getValue());

        verify(asylumCase, times(1)).write(eq(LEGAL_REP_ORGANISATION_PARTY_ID), partyId.capture());
        assertNotNull(partyId.getValue());

        verify(asylumCase, times(1)).write(eq(SPONSOR_PARTY_ID), partyId.capture());
        assertNotNull(partyId.getValue());

    }

    @Test
    void should_generate_party_id_when_event_is_start_appeal_with_no_sponsor_and_not_in_uk() {
        when(callback.getEvent()).thenReturn(START_APPEAL);

        when(asylumCase.read(APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(HAS_SPONSOR, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                partyIdHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).write(eq(APPELLANT_PARTY_ID), partyId.capture());
        assertNotNull(partyId.getValue());

        verify(asylumCase, times(1)).write(eq(LEGAL_REP_INDIVIDUAL_PARTY_ID), partyId.capture());
        assertNotNull(partyId.getValue());

        verify(asylumCase, times(1)).write(eq(LEGAL_REP_ORGANISATION_PARTY_ID), partyId.capture());
        assertNotNull(partyId.getValue());

        verify(asylumCase, never()).write(eq(SPONSOR_PARTY_ID), anyString());
    }

    @Test
    void should_not_generate_party_id_when_event_is_edit_appeal() {
        when(callback.getEvent()).thenReturn(EDIT_APPEAL);

        when(asylumCase.read(APPELLANT_PARTY_ID, String.class)).thenReturn(Optional.of(APPELLANT_PARTY_ID_GENERATION));
        when(asylumCase.read(LEGAL_REP_INDIVIDUAL_PARTY_ID, String.class)).thenReturn(Optional.of(LEGAL_REP_INDIVIDUAL_PARTY_ID_GENERATION));
        when(asylumCase.read(LEGAL_REP_ORGANISATION_PARTY_ID, String.class)).thenReturn(Optional.of(LEGAL_REP_ORGANISATION_PARTY_ID_GENERATION));

        when(asylumCase.read(APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(HAS_SPONSOR, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(SPONSOR_PARTY_ID, String.class)).thenReturn(Optional.of(SPONSOR_PARTY_ID_GENERATION));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                partyIdHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, never()).write(eq(APPELLANT_PARTY_ID), anyString());
        verify(asylumCase, never()).write(eq(LEGAL_REP_INDIVIDUAL_PARTY_ID), anyString());
        verify(asylumCase, never()).write(eq(LEGAL_REP_ORGANISATION_PARTY_ID), anyString());
        verify(asylumCase, never()).write(eq(SPONSOR_PARTY_ID), anyString());

    }

    @Test
    void should_generate_witness_party_id_when_event_is_draft_hearing_requirements() {
        witnessDetails = Arrays.asList(
                new IdValue<>("1", new WitnessDetails("witness", "1")),
                new IdValue<>("2", new WitnessDetails("witness", "2"))
        );

        when(callback.getEvent()).thenReturn(DRAFT_HEARING_REQUIREMENTS);

        when(asylumCase.read(WITNESS_DETAILS)).thenReturn(Optional.of(witnessDetails));

        when(asylumCase.read(WITNESS_N_FIELD.get(0), WitnessDetails.class))
                .thenReturn(Optional.of(new WitnessDetails("witness", "1")));
        when(asylumCase.read(WITNESS_N_FIELD.get(1), WitnessDetails.class))
                .thenReturn(Optional.of(new WitnessDetails("witness", "2")));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                partyIdHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        assertNotNull(witnessDetails.get(0).getValue().getWitnessPartyId());
        assertNotNull(witnessDetails.get(1).getValue().getWitnessPartyId());

        verify(asylumCase, times(1)).write(eq(WITNESS_N_FIELD.get(0)), witness.capture());
        assertEquals(witness.getValue().getWitnessPartyId(), witnessDetails.get(0).getValue().getWitnessPartyId());
        verify(asylumCase, times(1)).write(eq(WITNESS_N_FIELD.get(1)), witness.capture());
        assertEquals(witness.getValue().getWitnessPartyId(), witnessDetails.get(1).getValue().getWitnessPartyId());

        verify(asylumCase, times(1)).write(eq(WITNESS_DETAILS), any());

    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> partyIdHandler.canHandle(null, callback))
                .hasMessage("callbackStage must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(
                () -> partyIdHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
                .hasMessage("callback must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> partyIdHandler.handle(null, callback))
                .hasMessage("callbackStage must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> partyIdHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
                .hasMessage("callback must not be null")
                .isExactlyInstanceOf(NullPointerException.class);
    }
}
