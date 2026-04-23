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

    private static final String HO_UAN_DC_EA_HU_FEATURE = "home-office-uan-dc-ea-hu-feature";

    @Mock private FeatureToggler featureToggler;

    @ParameterizedTest
    @EnumSource(value = AppealType.class, names = { "DC", "EA", "HU" })
    void should_check_the_flag_for_asylum_non_appeal_types(AppealType appealType) {

        assertFalse(HomeOfficeAppealTypeChecker.isAppealTypeEnabled(featureToggler, appealType));
    }

    @ParameterizedTest
    @EnumSource(value = AppealType.class, names = { "DC", "EA", "HU" })
    void should_check_flag_for_supported_appeal_types(AppealType appealType) {

        when(featureToggler.getValue(HO_UAN_DC_EA_HU_FEATURE, false)).thenReturn(true);

    }

}
