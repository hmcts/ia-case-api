package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_LEVEL_FLAGS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_NAME_FOR_DISPLAY;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CASE_LEVEL_FLAGS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_DETAILS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_LEVEL_FLAGS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;

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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.CaseFlagDetail;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.CaseFlagValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.PartyFlagIdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.StrategicCaseFlag;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.WitnessDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;


@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class CreateFlagHandlerTest {

    @Captor
    private ArgumentCaptor<List<PartyFlagIdValue>> witnessFlagsCaptor;
    @Captor
    private ArgumentCaptor<StrategicCaseFlag> appellantFlagsCaptor;
    @Captor
    private ArgumentCaptor<StrategicCaseFlag> caseFlagsCaptor;

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;

    private CreateFlagHandler createFlagHandler;

    private final String appellantNameForDisplay = "some-name";

    private final String partyId1 = "witnessPartyId1";
    private final String witnessName1 = "witnessName1";
    private final String witnessFamilyName1 = "witnessFamilyName1";
    private final String partyId2 = "witnessPartyId2";
    private final String witnessName2 = "witnessName2";
    private final String witnessFamilyName2 = "witnessFamilyName2";


    @BeforeEach
    public void setUp() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.CREATE_FLAG);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(APPELLANT_NAME_FOR_DISPLAY, String.class)).thenReturn(Optional.of(appellantNameForDisplay));

        String witnessName3 = "witnessName3";
        String witnessFamilyName3 = "witnessFamilyName3";
        when(asylumCase.read(WITNESS_DETAILS))
            .thenReturn(Optional.of(List.of(
                new IdValue<>(witnessName1, new WitnessDetails(partyId1, witnessName1, witnessFamilyName1)),
                new IdValue<>(witnessName2, new WitnessDetails(partyId2, witnessName2, witnessFamilyName2)),
                new IdValue<>(witnessName3, new WitnessDetails(witnessName3, witnessFamilyName3)))));

        createFlagHandler = new CreateFlagHandler();
    }

    @Test
    void test_handle_appellant_level_flags() {

        PreSubmitCallbackResponse<AsylumCase> callbackResponse = createFlagHandler
            .handle(ABOUT_TO_START, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase).write(eq(APPELLANT_LEVEL_FLAGS), appellantFlagsCaptor.capture());
        assertTrue(appellantFlagsCaptor.getValue().getDetails().isEmpty());
        assertEquals(StrategicCaseFlag.ROLE_ON_CASE_APPELLANT, appellantFlagsCaptor.getValue().getRoleOnCase());
        assertEquals(appellantNameForDisplay, appellantFlagsCaptor.getValue().getPartyName());

        verify(asylumCase, times(1)).write(eq(APPELLANT_LEVEL_FLAGS), any());
    }

    @Test
    void test_handle_appellant_level_flags_when_appellant_has_existing_flags() {

        List<CaseFlagDetail> caseFlagDetails =
            List.of(new CaseFlagDetail("flagId", CaseFlagValue.builder().build()));
        StrategicCaseFlag appellantFlag = new StrategicCaseFlag(
            appellantNameForDisplay, StrategicCaseFlag.ROLE_ON_CASE_APPELLANT, caseFlagDetails);
        when(asylumCase.read(APPELLANT_LEVEL_FLAGS, StrategicCaseFlag.class))
            .thenReturn(Optional.of(appellantFlag));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse = createFlagHandler
            .handle(ABOUT_TO_START, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, never()).write(eq(APPELLANT_LEVEL_FLAGS), any());
    }

    @Test
    void should_update_appellant_name_in_existing_flags() {

        List<CaseFlagDetail> caseFlagDetails =
            List.of(new CaseFlagDetail("flagId", CaseFlagValue.builder().build()));
        StrategicCaseFlag appellantFlag = new StrategicCaseFlag(
            "appellantOldName", StrategicCaseFlag.ROLE_ON_CASE_APPELLANT, caseFlagDetails);
        when(asylumCase.read(APPELLANT_LEVEL_FLAGS, StrategicCaseFlag.class))
            .thenReturn(Optional.of(appellantFlag));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse = createFlagHandler
            .handle(ABOUT_TO_START, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase).write(eq(APPELLANT_LEVEL_FLAGS), appellantFlagsCaptor.capture());
        assertEquals("flagId", appellantFlagsCaptor.getValue().getDetails().get(0).getId());
        assertEquals(StrategicCaseFlag.ROLE_ON_CASE_APPELLANT, appellantFlagsCaptor.getValue().getRoleOnCase());
        assertEquals(appellantNameForDisplay, appellantFlagsCaptor.getValue().getPartyName());

        verify(asylumCase, times(1)).write(eq(APPELLANT_LEVEL_FLAGS), any());
    }

    @Test
    void test_handle_witness_level_flags() {

        final String fullName1 = witnessName1 + " " + witnessFamilyName1;
        final String fullName2 = witnessName2 + " " + witnessFamilyName2;
        final StrategicCaseFlag witnessFlag1 = new StrategicCaseFlag(
            fullName1, StrategicCaseFlag.ROLE_ON_CASE_WITNESS, Collections.emptyList());
        final StrategicCaseFlag witnessFlag2 = new StrategicCaseFlag(
            fullName2, StrategicCaseFlag.ROLE_ON_CASE_WITNESS, Collections.emptyList());
        final List<PartyFlagIdValue> expected = List.of(
            new PartyFlagIdValue(partyId1, witnessFlag1), new PartyFlagIdValue(partyId2, witnessFlag2));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            createFlagHandler.handle(ABOUT_TO_START, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase).write(eq(WITNESS_LEVEL_FLAGS), witnessFlagsCaptor.capture());
        assertNotNull(witnessFlagsCaptor.getValue());
        assertEquals(2, witnessFlagsCaptor.getValue().size());
        assertTrue(witnessFlagsCaptor.getValue().contains(expected.get(0)));
        assertTrue(witnessFlagsCaptor.getValue().contains(expected.get(1)));

        verify(asylumCase, times(1)).write(eq(WITNESS_LEVEL_FLAGS), any());
    }

    @Test
    void test_handle_witness_level_flags_when_some_witnesses_have_existing_flags() {

        final List<CaseFlagDetail> caseFlagDetails =
            List.of(new CaseFlagDetail("flagId", CaseFlagValue.builder().build()));
        final String fullName1 = witnessName1 + " " + witnessFamilyName1;
        final String fullName2 = witnessName2 + " " + witnessFamilyName2;
        final StrategicCaseFlag witnessFlag1 = new StrategicCaseFlag(
            fullName1, StrategicCaseFlag.ROLE_ON_CASE_WITNESS, caseFlagDetails);
        final StrategicCaseFlag witnessFlag2 = new StrategicCaseFlag(
            fullName2, StrategicCaseFlag.ROLE_ON_CASE_WITNESS, Collections.emptyList());
        final List<PartyFlagIdValue> expected = List.of(
            new PartyFlagIdValue(partyId1, witnessFlag1), new PartyFlagIdValue(partyId2, witnessFlag2));
        when(asylumCase.read(WITNESS_LEVEL_FLAGS))
            .thenReturn(Optional.of(List.of(new PartyFlagIdValue(partyId1, witnessFlag1))));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            createFlagHandler.handle(ABOUT_TO_START, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase).write(eq(WITNESS_LEVEL_FLAGS), witnessFlagsCaptor.capture());
        assertNotNull(witnessFlagsCaptor.getValue());
        assertEquals(2, witnessFlagsCaptor.getValue().size());
        assertTrue(witnessFlagsCaptor.getValue().contains(expected.get(0)));
        assertTrue(witnessFlagsCaptor.getValue().contains(expected.get(1)));

        verify(asylumCase, times(1)).write(eq(WITNESS_LEVEL_FLAGS), any());
    }

    @Test
    void test_handle_case_level_flags() {

        PreSubmitCallbackResponse<AsylumCase> callbackResponse = createFlagHandler
            .handle(ABOUT_TO_START, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase).write(eq(APPELLANT_LEVEL_FLAGS), caseFlagsCaptor.capture());
        assertNotNull(caseFlagsCaptor.getValue());

        verify(asylumCase, times(1)).write(eq(CASE_LEVEL_FLAGS), any());
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> createFlagHandler.handle(ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = createFlagHandler.canHandle(callbackStage, callback);

                if (event == Event.CREATE_FLAG
                    && callbackStage == ABOUT_TO_START) {
                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }

            reset(callback);
        }
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> createFlagHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
