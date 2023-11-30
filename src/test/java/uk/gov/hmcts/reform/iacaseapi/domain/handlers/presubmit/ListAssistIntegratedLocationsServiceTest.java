package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CASE_MANAGEMENT_LOCATION;
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
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
public class ListAssistIntegratedLocationsServiceTest {

    private static final String LIST_ASSIST_INTEGRATED_LOCATIONS = "list-assist-integrated-locations";
    private static final LDValue DEFAULT_VALUE = LDValue.parse("{\"epimsIds\":[]}");
    private static final LDValue EXAMPLE_VALUE = LDValue.parse("{\"epimsIds\":[111111,22222]}");
    private static final String INTEGRATED_LOCATION = "111111";
    private static final String UNINTEGRATED_LOCATION = "333333";
    @Mock
    private FeatureToggler featureToggler;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private CaseManagementLocation caseManagementLocation;
    @Mock
    private BaseLocation baseLocation;

    private ListAssistIntegratedLocationsService listAssistIntegratedLocationsService;

    @BeforeEach
    void setup() {
        listAssistIntegratedLocationsService = new ListAssistIntegratedLocationsService(featureToggler);

        when(featureToggler.getJsonValue(LIST_ASSIST_INTEGRATED_LOCATIONS, DEFAULT_VALUE)).thenReturn(EXAMPLE_VALUE);
        when(asylumCase.read(CASE_MANAGEMENT_LOCATION, CaseManagementLocation.class))
            .thenReturn(Optional.of(caseManagementLocation));
        when(caseManagementLocation.getBaseLocation()).thenReturn(baseLocation);
    }

    @Test
    void isListAssistIntegratedLocated_should_return_yes() {
        when(baseLocation.getId()).thenReturn(INTEGRATED_LOCATION);

        assertEquals(YES, listAssistIntegratedLocationsService.isListAssistIntegratedLocated(asylumCase));

    }

    @Test
    void isListAssistIntegratedLocated_should_return_no() {
        when(baseLocation.getId()).thenReturn(UNINTEGRATED_LOCATION);

        assertEquals(NO, listAssistIntegratedLocationsService.isListAssistIntegratedLocated(asylumCase));

    }

}
