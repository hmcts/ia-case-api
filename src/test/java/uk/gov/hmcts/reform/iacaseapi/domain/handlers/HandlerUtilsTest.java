package uk.gov.hmcts.reform.iacaseapi.domain.handlers;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.JourneyType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.service.LocationBasedFeatureToggler;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class HandlerUtilsTest {

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
}