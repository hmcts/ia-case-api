package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.clients.LaunchDarklyFeatureToggler;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
class FeatureToggleServiceTest {

    @Mock
    private LaunchDarklyFeatureToggler launchDarklyFeatureToggler;

    private FeatureToggleService featureToggleService;

    @BeforeEach
    void setUp() {
        featureToggleService = new FeatureToggleService(launchDarklyFeatureToggler);
    }

    @ParameterizedTest
    @ValueSource(booleans =  {true, false})
    void test_imaEnabled(boolean expected) {

        when(launchDarklyFeatureToggler.getValue("ima-feature-flag", false)).thenReturn(expected);

        assertEquals(expected, featureToggleService.imaEnabled());
    }

    @ParameterizedTest
    @ValueSource(booleans =  {true, false})
    void test_locationRefDataEnabled(boolean expected) {

        when(launchDarklyFeatureToggler.getValue("bails-location-reference-data", false))
            .thenReturn(expected);

        assertEquals(expected, featureToggleService.locationRefDataEnabled());
    }

}
