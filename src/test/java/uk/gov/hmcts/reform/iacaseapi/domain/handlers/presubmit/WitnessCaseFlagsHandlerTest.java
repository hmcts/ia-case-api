package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static uk.gov.hmcts.reform.iacaseapi.domain.entities.StrategicCaseFlagType.INTERPRETER_LANGUAGE_FLAG;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class WitnessCaseFlagsHandlerTest {
    private WitnessCaseFlagsHandler witnessCaseFlagsHandler;
    private List<CaseFlagDetail> activeCaseFlagDetails;
    private PartyFlagIdValue partyFlagIdValue;
    private CaseFlagValue activeCaseFlagValue;
    private StrategicCaseFlag caseFlag;
    private final String witnessId = "partyId1";

    @BeforeEach
    public void setUp() {
        activeCaseFlagValue = CaseFlagValue
                .builder()
                .flagCode(INTERPRETER_LANGUAGE_FLAG.getFlagCode())
                .name(INTERPRETER_LANGUAGE_FLAG.getName())
                .status("Active")
                .build();
        activeCaseFlagDetails = List.of(new CaseFlagDetail("123", activeCaseFlagValue));
        witnessCaseFlagsHandler = new WitnessCaseFlagsHandler();
        caseFlag = new StrategicCaseFlag("Tester Name", StrategicCaseFlag.ROLE_ON_CASE_WITNESS, activeCaseFlagDetails);
        partyFlagIdValue = new PartyFlagIdValue(witnessId, caseFlag);
    }

    @Test
    void getWitnessCaseFlags_should_return_case_flags() {
        Optional<List<PartyFlagIdValue>> partyFlagIdValues = Optional.of(new ArrayList<>());
        partyFlagIdValues.get().add(partyFlagIdValue);
        assertFalse(witnessCaseFlagsHandler.getWitnessCaseFlags(partyFlagIdValues, witnessId).isEmpty());
    }

    @Test
    void getWitnessCaseFlags_should_return_no_case_flags() {
        Optional<List<PartyFlagIdValue>> partyFlagIdValues = Optional.of(new ArrayList<>());
        partyFlagIdValues.get().add(partyFlagIdValue);
        assertTrue(witnessCaseFlagsHandler.getWitnessCaseFlags(partyFlagIdValues,"fakeWitnessId").isEmpty());
    }

}
