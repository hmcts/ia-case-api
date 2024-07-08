package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CASE_MANAGEMENT_LOCATION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CASE_MANAGEMENT_LOCATION_REF_DATA;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_CASE_USING_LOCATION_REF_DATA;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;

import com.launchdarkly.sdk.LDValue;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.BaseLocation;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.CaseManagementLocation;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.CaseManagementLocationRefData;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Value;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
public class LocationBasedFeatureTogglerTest {

    private static final String LIST_ASSIST_INTEGRATED_LOCATIONS = "list-assist-integrated-locations";
    private static final String AUTO_HEARING_REQUEST_LOCATIONS_LIST = "auto-hearing-request-locations-list";
    private static final LDValue DEFAULT_VALUE = LDValue.parse("{\"epimsIds\":[]}");
    private static final LDValue EXAMPLE_VALUE = LDValue.parse("{\"epimsIds\":[111111,22222]}");
    private static final String ENABLED_LOCATION = "111111";
    private static final String DISABLED_LOCATION = "333333";
    @Mock
    private FeatureToggler featureToggler;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private CaseManagementLocation caseManagementLocation;
    @Mock
    private CaseManagementLocationRefData caseManagementLocationRefData;
    @Mock
    private BaseLocation baseLocation;
    @Mock
    private DynamicList refDataBaseLocationList;
    @Mock
    private Value refDataBaseLocation;
    private LocationBasedFeatureToggler locationBasedFeatureToggler;

    @BeforeEach
    void setup() {
        locationBasedFeatureToggler = new LocationBasedFeatureToggler(featureToggler);

        when(featureToggler.getJsonValue(LIST_ASSIST_INTEGRATED_LOCATIONS, DEFAULT_VALUE)).thenReturn(EXAMPLE_VALUE);
        when(featureToggler.getJsonValue(AUTO_HEARING_REQUEST_LOCATIONS_LIST, DEFAULT_VALUE)).thenReturn(EXAMPLE_VALUE);
        when(asylumCase.read(CASE_MANAGEMENT_LOCATION, CaseManagementLocation.class))
            .thenReturn(Optional.of(caseManagementLocation));
        when(caseManagementLocation.getBaseLocation()).thenReturn(baseLocation);
        when(asylumCase.read(CASE_MANAGEMENT_LOCATION_REF_DATA, CaseManagementLocationRefData.class))
            .thenReturn(Optional.of(caseManagementLocationRefData));
        when(caseManagementLocationRefData.getBaseLocation()).thenReturn(refDataBaseLocationList);
        when(refDataBaseLocationList.getValue()).thenReturn(refDataBaseLocation);

    }

    @Test
    void isListAssistIntegratedLocated_should_return_yes() {
        when(baseLocation.getId()).thenReturn(ENABLED_LOCATION);

        assertEquals(YES, locationBasedFeatureToggler.isListAssistEnabled(asylumCase));

    }

    @Test
    void isListAssistIntegratedLocated_should_return_no() {
        when(baseLocation.getId()).thenReturn(DISABLED_LOCATION);

        assertEquals(NO, locationBasedFeatureToggler.isListAssistEnabled(asylumCase));

    }

    @Test
    void isAutoHearingRequestEnabled_should_return_yes() {
        when(baseLocation.getId()).thenReturn(ENABLED_LOCATION);

        assertEquals(YES, locationBasedFeatureToggler.isAutoHearingRequestEnabled(asylumCase));

    }

    @Test
    void isAutoHearingRequestEnabled_should_return_no() {
        when(baseLocation.getId()).thenReturn(DISABLED_LOCATION);

        assertEquals(NO, locationBasedFeatureToggler.isAutoHearingRequestEnabled(asylumCase));

    }

    @Test
    void isAutoHearingRequestEnabled_should_return_yes_when_ref_data_enabled() {
        when(asylumCase.read(IS_CASE_USING_LOCATION_REF_DATA, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(refDataBaseLocation.getCode()).thenReturn(ENABLED_LOCATION);

        assertEquals(YES, locationBasedFeatureToggler.isAutoHearingRequestEnabled(asylumCase));

    }

    @Test
    void isAutoHearingRequestEnabled_should_return_no_when_ref_data_enabled() {
        when(asylumCase.read(IS_CASE_USING_LOCATION_REF_DATA, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(refDataBaseLocation.getCode()).thenReturn(DISABLED_LOCATION);

        assertEquals(NO, locationBasedFeatureToggler.isAutoHearingRequestEnabled(asylumCase));

    }

}
