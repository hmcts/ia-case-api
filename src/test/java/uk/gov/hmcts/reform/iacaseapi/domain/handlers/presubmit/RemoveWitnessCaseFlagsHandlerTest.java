package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_DETAILS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_LEVEL_FLAGS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.UPDATE_HEARING_REQUIREMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.iacaseapi.domain.service.StrategicCaseFlagService.ROLE_ON_CASE_WITNESS;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.PartyFlagIdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.StrategicCaseFlag;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.WitnessDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class RemoveWitnessCaseFlagsHandlerTest {

    @Captor
    private ArgumentCaptor<List<PartyFlagIdValue>> witnessFlagsCaptor;
    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    private RemoveWitnessCaseFlagsHandler removeWitnessCaseFlagsHandler;
    private PartyFlagIdValue witness2PartyFlagIdValue;
    private PartyFlagIdValue witness1PartyFlagIdVAlue;
    private final String witnessPartyId2 = "witnessPartyId2";
    private final String witnessName2 = "witnessName2";
    private final String witnessFamilyName2 = "witnessFamilyName2";

    @BeforeEach
    public void setUp() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.UPDATE_HEARING_REQUIREMENTS);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        String witnessName1 = "witnessName1";
        String witnessFamilyName1 = "witnessFamilyName1";
        final String fullName1 = witnessName1 + " " + witnessFamilyName1;
        final String fullName2 = witnessName2 + " " + witnessFamilyName2;
        StrategicCaseFlag witnessFlag1 = new StrategicCaseFlag(
            fullName1, ROLE_ON_CASE_WITNESS, Collections.emptyList());
        StrategicCaseFlag witnessFlag2 = new StrategicCaseFlag(
            fullName2, ROLE_ON_CASE_WITNESS, Collections.emptyList());
        witness2PartyFlagIdValue = new PartyFlagIdValue(witnessPartyId2, witnessFlag2);
        witness1PartyFlagIdVAlue = new PartyFlagIdValue("witnessPartyId1", witnessFlag1);

        removeWitnessCaseFlagsHandler = new RemoveWitnessCaseFlagsHandler();

    }

    @Test
    void should_remove_one_witness_flag() {
        when(asylumCase.read(WITNESS_LEVEL_FLAGS))
            .thenReturn(Optional.of(List.of(witness1PartyFlagIdVAlue, witness2PartyFlagIdValue)));
        when(asylumCase.read(WITNESS_DETAILS))
            .thenReturn(Optional.of(List.of(
                new IdValue<>("2", new WitnessDetails(witnessPartyId2, witnessName2, witnessFamilyName2, YesOrNo.NO)))));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            removeWitnessCaseFlagsHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase).write(eq(WITNESS_LEVEL_FLAGS), witnessFlagsCaptor.capture());
        assertEquals(1, witnessFlagsCaptor.getValue().size());
        assertTrue(witnessFlagsCaptor.getValue().contains(witness2PartyFlagIdValue));
    }

    @Test
    void should_remove_all_witness_flags() {
        when(asylumCase.read(WITNESS_LEVEL_FLAGS))
            .thenReturn(Optional.of(List.of(witness1PartyFlagIdVAlue, witness2PartyFlagIdValue)));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            removeWitnessCaseFlagsHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase).write(eq(WITNESS_LEVEL_FLAGS), witnessFlagsCaptor.capture());
        assertTrue(witnessFlagsCaptor.getValue().isEmpty());
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = removeWitnessCaseFlagsHandler.canHandle(callbackStage, callback);

                if (event == UPDATE_HEARING_REQUIREMENTS
                    && callbackStage == ABOUT_TO_SUBMIT) {
                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }
        }
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> removeWitnessCaseFlagsHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> removeWitnessCaseFlagsHandler.handle(ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }
}