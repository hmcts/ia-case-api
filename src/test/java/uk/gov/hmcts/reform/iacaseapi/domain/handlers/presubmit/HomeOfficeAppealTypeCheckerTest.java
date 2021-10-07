package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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

    private static final String HO_UAN_PA_RP_FEATURE = "home-office-uan-pa-rp-feature";
    private static final String HO_UAN_DC_EA_HU_FEATURE = "home-office-uan-dc-ea-hu-feature";

    @Mock private FeatureToggler featureToggler;

    @ParameterizedTest
    @EnumSource(value = AppealType.class, names = { "PA", "RP" })
    void should_check_the_flag_for_asylum_appeal_types(AppealType appealType) {

        when(featureToggler.getValue(HO_UAN_PA_RP_FEATURE, false)).thenReturn(true);

        assertTrue(HomeOfficeAppealTypeChecker.isAppealTypeEnabled(featureToggler, appealType));
    }

    @ParameterizedTest
    @EnumSource(value = AppealType.class, names = { "DC", "EA", "HU" })
    void should_check_the_flag_for_asylum_non_appeal_types(AppealType appealType) {

        assertFalse(HomeOfficeAppealTypeChecker.isAppealTypeEnabled(featureToggler, appealType));
    }

    @ParameterizedTest
    @EnumSource(value = AppealType.class, names = { "DC", "EA", "HU", "PA", "RP" })
    void should_check_flag_for_all_appeal_types(AppealType appealType) {

        when(featureToggler.getValue(HO_UAN_DC_EA_HU_FEATURE, false)).thenReturn(true);
        when(featureToggler.getValue(HO_UAN_PA_RP_FEATURE, false)).thenReturn(true);

        assertTrue(HomeOfficeAppealTypeChecker.isAppealTypeEnabled(featureToggler, appealType));
    }

}
