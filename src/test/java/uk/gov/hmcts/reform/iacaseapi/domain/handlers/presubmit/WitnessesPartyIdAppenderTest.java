package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_DETAILS;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.WitnessDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class WitnessesPartyIdAppenderTest {

    private static final String WITNESS_1_PARTY_ID = "111222333";
    private static final String WITNESS_2_PARTY_ID = "222111333";

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private List<IdValue<WitnessDetails>> witnessDetails;

    private WitnessesPartyIdAppender witnessesPartyIdAppender;

    @BeforeEach
    public void setUp() {
        witnessesPartyIdAppender = new WitnessesPartyIdAppender();

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getPageId()).thenReturn("isWitnessesAttending");
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

    }

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

        when(callback.getEvent()).thenReturn(event);
        when(asylumCase.read(WITNESS_DETAILS)).thenReturn(Optional.of(witnessDetails));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                witnessesPartyIdAppender.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        assertNotNull(witnessDetails.get(0).getValue().getWitnessPartyId());
        assertNotNull(witnessDetails.get(1).getValue().getWitnessPartyId());

        verify(asylumCase, times(1)).write(eq(WITNESS_DETAILS), any());

    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {
        "UPDATE_HEARING_REQUIREMENTS",
        "DRAFT_HEARING_REQUIREMENTS"
    })
    void should_not_append_witnesses_party_id(Event event) {
        WitnessDetails witnessDetails1 = new WitnessDetails("witness1", "family1");
        witnessDetails1.setWitnessPartyId(WITNESS_1_PARTY_ID);
        WitnessDetails witnessDetails2 = new WitnessDetails("witness2", "family2");
        witnessDetails1.setWitnessPartyId(WITNESS_2_PARTY_ID);
        witnessDetails = Arrays.asList(
            new IdValue<>("1", witnessDetails1),
            new IdValue<>("2", witnessDetails2)
        );

        when(callback.getEvent()).thenReturn(event);
        when(asylumCase.read(WITNESS_DETAILS)).thenReturn(Optional.of(witnessDetails));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                witnessesPartyIdAppender.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).write(WITNESS_DETAILS, Optional.of(witnessDetails));

    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> witnessesPartyIdAppender.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> witnessesPartyIdAppender.handle(PreSubmitCallbackStage.MID_EVENT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}