package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType.DC;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType.HU;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType.PA;

import java.util.Arrays;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class HomeOfficeAppealTypeCheckerTest {

    private static final String HO_UAN_PA_FEATURE = "home-office-uan-pa-feature";
    private static final String HO_UAN_RP_FEATURE = "home-office-uan-rp-feature";
    private static final String HO_UAN_EA_FEATURE = "home-office-uan-ea-feature";
    private static final String HO_UAN_HU_FEATURE = "home-office-uan-hu-feature";
    private static final String HO_UAN_DC_FEATURE = "home-office-uan-dc-feature";
    private static final String HO_UAN_EU_FEATURE = "home-office-uan-eu-feature";
    private static final String HO_UAN_AG_FEATURE = "home-office-uan-ag-feature";

    @Mock private FeatureToggler featureToggler;

    @ParameterizedTest
    @EnumSource(value = AppealType.class, names = { "DC", "EA", "HU", "PA", "RP", "EU", "AG" })
    void should_check_flag_for_enabled_appeal_types(AppealType appealType) {

        when(featureToggler.getValue("home-office-uan-pa-feature", false)).thenReturn(true);
        when(featureToggler.getValue("home-office-uan-rp-feature", false)).thenReturn(false);
        when(featureToggler.getValue("home-office-uan-ea-feature", false)).thenReturn(false);
        when(featureToggler.getValue("home-office-uan-hu-feature", false)).thenReturn(true);
        when(featureToggler.getValue("home-office-uan-dc-feature", false)).thenReturn(true);
        when(featureToggler.getValue("home-office-uan-eu-feature", false)).thenReturn(false);
        when(featureToggler.getValue("home-office-uan-ag-feature", false)).thenReturn(false);

        if (Arrays.asList(DC, PA, HU).contains(appealType)) {
            assertTrue(HomeOfficeAppealTypeChecker.isAppealTypeEnabled(featureToggler, appealType));
        } else {
            assertFalse(HomeOfficeAppealTypeChecker.isAppealTypeEnabled(featureToggler, appealType));
        }

    }

}
