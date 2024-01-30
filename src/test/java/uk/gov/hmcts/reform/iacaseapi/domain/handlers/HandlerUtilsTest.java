package uk.gov.hmcts.reform.iacaseapi.domain.handlers;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.isPanelRequired;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.CaseFlagDetail;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.CaseFlagValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.StrategicCaseFlag;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Value;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.JourneyType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.service.LocationBasedFeatureToggler;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class HandlerUtilsTest {
    private static final String ON_THE_PAPERS = "ONPPRS";

    @Mock
    private AsylumCase asylumCase;
    @Mock
    private LocationBasedFeatureToggler locationBasedFeatureToggler;

    @Test
    void given_journey_type_aip_returns_true() {
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.AIP));
        assertTrue(HandlerUtils.isAipJourney(asylumCase));
    }

    @Test
    void given_journey_type_rep_returns_true() {
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.REP));
        assertTrue(HandlerUtils.isRepJourney(asylumCase));
    }

    @Test
    void no_journey_type_defaults_to_rep() {
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.empty());
        assertTrue(HandlerUtils.isRepJourney(asylumCase));
    }

    @Test
    void given_aip_journey_rep_test_should_fail() {
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.AIP));
        assertFalse(HandlerUtils.isRepJourney(asylumCase));
    }

    @Test
    void given_rep_journey_aip_test_should_fail() {
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.REP));
        assertFalse(HandlerUtils.isAipJourney(asylumCase));
    }

    @Test
    void get_appellant_full_name_should_return_appellant_display_name() {
        String appellantDisplayName = "FirstName FamilyName";
        when(asylumCase.read(APPELLANT_NAME_FOR_DISPLAY, String.class)).thenReturn(Optional.of(appellantDisplayName));

        assertEquals(appellantDisplayName, HandlerUtils.getAppellantFullName(asylumCase));
    }

    @Test
    void get_appellant_full_name_should_return_appellant_given_names_and_family_name() {
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class))
            .thenReturn(Optional.of("FirstName SecondName"));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class))
            .thenReturn(Optional.of("FamilyName"));

        assertEquals("FirstName SecondName FamilyName", HandlerUtils.getAppellantFullName(asylumCase));
    }

    @ParameterizedTest
    @EnumSource(value = YesOrNo.class, names = {"NO", "YES"})
    void should_check_set_value_in_auto_hearing_enabled_field(YesOrNo value) {
        when(locationBasedFeatureToggler.isAutoHearingRequestEnabled(asylumCase)).thenReturn(value);

        HandlerUtils.checkAndUpdateAutoHearingRequestEnabled(locationBasedFeatureToggler, asylumCase);

        verify(asylumCase, times(1)).write(
                AUTO_HEARING_REQUEST_ENABLED,
                value);
    }

    @Test
    void get_appellant_full_name_should_throw_exception() {
        assertThatThrownBy(() -> HandlerUtils.getAppellantFullName(asylumCase))
            .hasMessage("Appellant given names required")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class))
            .thenReturn(Optional.of("FirstName SecondName"));

        assertThatThrownBy(() -> HandlerUtils.getAppellantFullName(asylumCase))
            .hasMessage("Appellant family name required")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class))
            .thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class))
            .thenReturn(Optional.of("FamilyName"));

        assertThatThrownBy(() -> HandlerUtils.getAppellantFullName(asylumCase))
            .hasMessage("Appellant given names required")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @ParameterizedTest
    @CsvSource({
        "true, false, true, Active, RA0042, NO", // Only appellantLevelFlag RA0042 present
        "true, false, true, Active, PF0012, NO", // Only appellantLevelFlag PF0012 present
        "true, false, true, Active, PF0014, NO", // Only appellantLevelFlags PF0014 present
        "true, false, true, Active, PF0017, NO", // Only appellantLevelFlags PF0017 present
        "true, false, true, Active, PF0018, NO", // Only appellantLevelFlags PF0018 present
        "false, true, true, Active, CF0011, NO", // Only caseLevelFlags CF0011 present
        "true, true, true, Active, CF0011, NO", // All flags active and present
        "false, false, false, Inactive, PF0018, YES", // Only inactive flags present
        "false, false, false, Inactive, CF0011, YES", // No flags present
        "false, false, true, Active, CF0011, NO", // No flags present and Hearing is on the papers
    })
    void setDefaultAutoListHearingValue_ActiveFlagPresent(boolean hasAppellantFlags, boolean hasCaseFlags,
                                                          boolean isHearingOnThePaper, String active,
                                                          String flagCode, String expected) {
        when(asylumCase.read(HEARING_CHANNEL, DynamicList.class)).thenReturn(
            isHearingOnThePaper ?
                Optional.of(new DynamicList(new Value(ON_THE_PAPERS, "On the Papers"),
                    List.of(new Value(ON_THE_PAPERS, "On the Papers"))))
                : Optional.empty());

        List<CaseFlagDetail> existingFlags = List.of(
            new CaseFlagDetail("1",
                CaseFlagValue
                    .builder()
                    .flagCode(flagCode)
                    .name("flagName")
                    .status(active)
                    .build())
        );

        when(asylumCase.read(APPELLANT_LEVEL_FLAGS, StrategicCaseFlag.class)).thenReturn(
            hasAppellantFlags ? Optional.of(new StrategicCaseFlag("name", "role", existingFlags))
                : Optional.empty());

        when(asylumCase.read(CASE_LEVEL_FLAGS, StrategicCaseFlag.class)).thenReturn(
            hasCaseFlags ? Optional.of(new StrategicCaseFlag("name", "role", existingFlags))
                : Optional.empty());

        HandlerUtils.setDefaultAutoListHearingValue(asylumCase);

        verify(asylumCase, times(1)).write(AUTO_LIST_HEARING, YesOrNo.valueOf(expected));
    }

    @ParameterizedTest
    @EnumSource(value = YesOrNo.class, names = {"YES","NO"})
    void should_return_whether_panel_is_required(YesOrNo yesOrNo) {
        when(asylumCase.read(IS_PANEL_REQUIRED, YesOrNo.class)).thenReturn(Optional.of(yesOrNo));

        assertEquals(yesOrNo == YES, isPanelRequired(asylumCase));
    }
}