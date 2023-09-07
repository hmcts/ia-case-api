package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_LEVEL_FLAGS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_NAME_FOR_DISPLAY;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.StrategicCaseFlagType.HEARING_LOOP;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.StrategicCaseFlagType.STEP_FREE_WHEELCHAIR_ACCESS;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.StrategicCaseFlag;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AppellantCaseFlagsHandlerTest {

    @Mock
    private AsylumCase asylumCase;

    private AppellantCaseFlagsHandler appellantCaseFlagsHandler;
    private List<CaseFlagDetail> activeCaseFlagDetails;
    private List<CaseFlagDetail> inactiveCaseFlagDetails;
    private CaseFlagValue activeCaseFlagValue;
    private CaseFlagValue inactiveCaseFlagValue;
    private final String fullName = "fullName";

    @BeforeEach
    void setup() {
        appellantCaseFlagsHandler = new AppellantCaseFlagsHandler();

        activeCaseFlagValue = CaseFlagValue
            .builder()
            .flagCode(HEARING_LOOP.getFlagCode())
            .name(HEARING_LOOP.getName())
            .status("Active")
            .build();
        activeCaseFlagDetails = List.of(new CaseFlagDetail("123", activeCaseFlagValue));
        inactiveCaseFlagValue = CaseFlagValue
            .builder()
            .flagCode(HEARING_LOOP.getFlagCode())
            .name(HEARING_LOOP.getName())
            .status("Inactive")
            .build();
        inactiveCaseFlagDetails = List.of(new CaseFlagDetail("123", inactiveCaseFlagValue));

        when(asylumCase.read(APPELLANT_NAME_FOR_DISPLAY, String.class))
            .thenReturn(Optional.of(fullName));
    }

    @Test
    void hasActiveTargetCaseFlag_should_return_true() {
        assertTrue(appellantCaseFlagsHandler.hasActiveTargetCaseFlag(activeCaseFlagDetails, HEARING_LOOP));
    }

    @Test
    void hasActiveTargetCaseFlag_should_return_false() {
        assertFalse(appellantCaseFlagsHandler.hasActiveTargetCaseFlag(Collections.emptyList(), HEARING_LOOP));
        assertFalse(appellantCaseFlagsHandler.hasActiveTargetCaseFlag(inactiveCaseFlagDetails, HEARING_LOOP));
        assertFalse(
            appellantCaseFlagsHandler.hasActiveTargetCaseFlag(activeCaseFlagDetails, STEP_FREE_WHEELCHAIR_ACCESS));
    }

    @Test
    void isActiveTargetCaseFlag_should_return_true() {
        assertTrue(appellantCaseFlagsHandler.isActiveTargetCaseFlag(activeCaseFlagValue, HEARING_LOOP));
    }

    @Test
    void isActiveTargetCaseFlag_should_return_false() {
        assertFalse(appellantCaseFlagsHandler.isActiveTargetCaseFlag(inactiveCaseFlagValue, HEARING_LOOP));
        assertFalse(
            appellantCaseFlagsHandler.isActiveTargetCaseFlag(activeCaseFlagValue, STEP_FREE_WHEELCHAIR_ACCESS));
    }

    @Test
    void getOrCreateAppellantCaseFlags_should_always_return_appellant_case_flag() {
        StrategicCaseFlag appellantCaseFlag = new StrategicCaseFlag(
            fullName, StrategicCaseFlag.ROLE_ON_CASE_APPELLANT);

        StrategicCaseFlag actual = appellantCaseFlagsHandler.getOrCreateAppellantCaseFlags(asylumCase);
        assertEquals(actual.getPartyName(), appellantCaseFlag.getPartyName());
        assertEquals(actual.getRoleOnCase(), appellantCaseFlag.getRoleOnCase());

        when(asylumCase.read(APPELLANT_LEVEL_FLAGS, StrategicCaseFlag.class))
            .thenReturn(Optional.of(appellantCaseFlag));

        actual = appellantCaseFlagsHandler.getOrCreateAppellantCaseFlags(asylumCase);
        assertEquals(actual.getPartyName(), appellantCaseFlag.getPartyName());
        assertEquals(actual.getRoleOnCase(), appellantCaseFlag.getRoleOnCase());
    }

    @Test
    void should_activate_case_flag() {
        boolean activatedWithExistingCaseFlag = appellantCaseFlagsHandler
            .activateCaseFlag(asylumCase, activeCaseFlagDetails, STEP_FREE_WHEELCHAIR_ACCESS, "06-09-2023")
            .stream()
            .anyMatch(details -> Objects.equals(details.getCaseFlagValue().getStatus(), "Active"));
        boolean activatedWithNoExistingCaseFlag = appellantCaseFlagsHandler
            .activateCaseFlag(asylumCase, Collections.emptyList(), HEARING_LOOP, "06-09-2023")
            .stream()
            .anyMatch(details -> Objects.equals(details.getCaseFlagValue().getStatus(), "Active"));

        assertTrue(activatedWithExistingCaseFlag);
        assertTrue(activatedWithNoExistingCaseFlag);
    }

    @Test
    void should_deactivate_case_flag() {
        boolean deactivated = appellantCaseFlagsHandler
            .deactivateCaseFlag(activeCaseFlagDetails, HEARING_LOOP, "06-09-2023")
            .stream()
            .anyMatch(details -> Objects.equals(details.getCaseFlagValue().getStatus(), "Inactive"));

        assertTrue(deactivated);
    }

}