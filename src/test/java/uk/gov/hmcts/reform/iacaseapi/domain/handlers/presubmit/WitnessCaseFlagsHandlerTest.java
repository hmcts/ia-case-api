package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.powermock.api.mockito.PowerMockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_1;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_1_INTERPRETER_SIGN_LANGUAGE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_1_INTERPRETER_SPOKEN_LANGUAGE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_DETAILS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_LEVEL_FLAGS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.StrategicCaseFlagType.INTERPRETER_LANGUAGE_FLAG;
import static uk.gov.hmcts.reform.iacaseapi.domain.service.StrategicCaseFlagService.ACTIVE_STATUS;
import static uk.gov.hmcts.reform.iacaseapi.domain.service.StrategicCaseFlagService.ROLE_ON_CASE_WITNESS;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.CaseFlagDetail;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.CaseFlagValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.InterpreterLanguageRefData;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Language;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.PartyFlagIdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.StrategicCaseFlag;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Value;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.WitnessDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class WitnessCaseFlagsHandlerTest {
    @Mock
    AsylumCase asylumCase;

    private WitnessCaseFlagsHandler witnessCaseFlagsHandler;
    private List<IdValue<WitnessDetails>> witnessDetailsList;
    private WitnessDetails witnessDetails;
    private PartyFlagIdValue partyFlagIdValue;
    private InterpreterLanguageRefData interpreterLanguageRefData;

    @BeforeEach
    public void setUp() {
        CaseFlagValue activeCaseFlagValue = CaseFlagValue
            .builder()
            .flagCode(INTERPRETER_LANGUAGE_FLAG.getFlagCode())
            .name(INTERPRETER_LANGUAGE_FLAG.getName())
            .status(ACTIVE_STATUS)
            .build();
        partyFlagIdValue = new PartyFlagIdValue("partyId1", new StrategicCaseFlag(
            "Tester Name", ROLE_ON_CASE_WITNESS, List.of(new CaseFlagDetail("123", activeCaseFlagValue))));
        witnessDetails = new WitnessDetails("1234", "Witness1Given", "Witness1Family");
        witnessDetailsList = List.of(new IdValue<>("1", witnessDetails));

        witnessCaseFlagsHandler = new WitnessCaseFlagsHandler();
    }

    @Test
    void getWitnessFlags_should_return_non_empty_map_of_witness_partyIds_to_flags() {
        when(asylumCase.read(WITNESS_LEVEL_FLAGS)).thenReturn(Optional.of(List.of(partyFlagIdValue)));

        Map<String, StrategicCaseFlag> witnessFlagsMap = witnessCaseFlagsHandler.getWitnessFlagsMap(asylumCase);

        assertFalse(witnessFlagsMap.isEmpty());
        assertTrue(witnessFlagsMap.containsKey(partyFlagIdValue.getPartyId()));
        assertTrue(witnessFlagsMap.containsValue(partyFlagIdValue.getValue()));
    }

    @Test
    void getWitnessFlags_should_return_empty_map() {
        Map<String, StrategicCaseFlag> witnessFlagsMap = witnessCaseFlagsHandler.getWitnessFlagsMap(asylumCase);

        assertTrue(witnessFlagsMap.isEmpty());
    }

    @Test
    void getWitnessDetails_should_return_non_empty_map_of_witness_partyIds_to_details() {
        when(asylumCase.read(WITNESS_DETAILS)).thenReturn(Optional.of(witnessDetailsList));

        Map<String, WitnessDetails> witnessDetailsMap = witnessCaseFlagsHandler.getWitnessDetailsMap(asylumCase);

        assertFalse(witnessDetailsMap.isEmpty());
        assertTrue(witnessDetailsMap.containsKey(witnessDetails.getWitnessPartyId()));
        assertTrue(witnessDetailsMap.containsValue(witnessDetails));
    }

    @Test
    void getWitnessDetails_should_return_empty_map() {
        Map<String, WitnessDetails> witnessDetailsMap = witnessCaseFlagsHandler.getWitnessDetailsMap(asylumCase);

        assertTrue(witnessDetailsMap.isEmpty());
    }

    @Test
    void getWitnessInterpreterLanguageMap_should_return_non_empty_map_of_witness_partyIds_to_language() {
        when(asylumCase.read(WITNESS_1, WitnessDetails.class))
            .thenReturn(Optional.of(witnessDetails));
        DynamicList dynamicList = new DynamicList("");
        dynamicList.setValue(new Value("Spa", "Spanish"));
        interpreterLanguageRefData = new InterpreterLanguageRefData(dynamicList, null, null);
        when(asylumCase.read(WITNESS_1_INTERPRETER_SPOKEN_LANGUAGE, InterpreterLanguageRefData.class))
            .thenReturn(Optional.of(interpreterLanguageRefData));

        Map<String, WitnessDetails> witnessDetailsMap = new HashMap<>();
        witnessDetailsMap.put(witnessDetails.getWitnessPartyId(), witnessDetails);

        Map<String, Language> witnessInterpreterLanguageMap = witnessCaseFlagsHandler
            .getWitnessInterpreterLanguageMap(asylumCase, witnessDetailsMap);

        Language spokenLanguage = witnessInterpreterLanguageMap.get(witnessDetails.getWitnessPartyId());

        assertNotNull(spokenLanguage);
        assertEquals(spokenLanguage.getLanguageCode(), "Spa");
        assertEquals(spokenLanguage.getLanguageText(), "Spanish");
    }

    @Test
    void getWitnessInterpreterLanguageMap_should_return_empty_map() {

        Map<String, WitnessDetails> witnessDetailsMap = new HashMap<>();
        witnessDetailsMap.put(witnessDetails.getWitnessPartyId(), witnessDetails);

        assertTrue(witnessCaseFlagsHandler
            .getWitnessInterpreterLanguageMap(asylumCase, witnessDetailsMap).isEmpty());

        when(asylumCase.read(WITNESS_1, WitnessDetails.class))
            .thenReturn(Optional.of(witnessDetails));

        assertTrue(witnessCaseFlagsHandler
            .getWitnessInterpreterLanguageMap(asylumCase, witnessDetailsMap).isEmpty());
    }

    @Test
    void getWitnessSignLanguageMap_should_return_non_empty_map_of_witness_partyIds_to_language() {
        when(asylumCase.read(WITNESS_1, WitnessDetails.class))
            .thenReturn(Optional.of(witnessDetails));

        interpreterLanguageRefData = new InterpreterLanguageRefData(
            null, List.of("Lipspeaker"), "Lipspeaker");
        when(asylumCase.read(WITNESS_1_INTERPRETER_SIGN_LANGUAGE, InterpreterLanguageRefData.class))
            .thenReturn(Optional.of(interpreterLanguageRefData));

        Map<String, WitnessDetails> witnessDetailsMap = new HashMap<>();
        witnessDetailsMap.put(witnessDetails.getWitnessPartyId(), witnessDetails);

        Map<String, Language> witnessSignLanguageMap = witnessCaseFlagsHandler
            .getWitnessSignLanguageMap(asylumCase, witnessDetailsMap);

        Language signLanguageManualEntry = witnessSignLanguageMap.get(witnessDetails.getWitnessPartyId());

        assertNotNull(signLanguageManualEntry);
        assertNull(signLanguageManualEntry.getLanguageCode());
        assertEquals(signLanguageManualEntry.getLanguageText(), "Lipspeaker");
    }

    @Test
    void getWitnessSignLanguageMap_should_return_empty_map() {

        Map<String, WitnessDetails> witnessDetailsMap = new HashMap<>();

        assertTrue(witnessCaseFlagsHandler
            .getWitnessSignLanguageMap(asylumCase, witnessDetailsMap).isEmpty());

        interpreterLanguageRefData = new InterpreterLanguageRefData(
            null, List.of("Spanish"), "Spanish");
        when(asylumCase.read(WITNESS_1_INTERPRETER_SPOKEN_LANGUAGE, InterpreterLanguageRefData.class))
            .thenReturn(Optional.of(interpreterLanguageRefData));

        assertTrue(witnessCaseFlagsHandler
            .getWitnessSignLanguageMap(asylumCase, witnessDetailsMap).isEmpty());
    }

}
