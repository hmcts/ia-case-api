package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;


import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;

public class HomeOfficeAppealTypeChecker {

    private static final String HO_UAN_PA_RP_FEATURE = "home-office-uan-pa-rp-feature";
    private static final String HO_UAN_DC_EA_HU_FEATURE = "home-office-uan-dc-ea-hu-feature";

    private HomeOfficeAppealTypeChecker() {
    }

    public static boolean isAppealTypeEnabled(FeatureToggler featureToggler, AppealType appealType) {

        switch (appealType) {
            case DC:
            case EA:
            case HU:
                return featureToggler.getValue(HO_UAN_DC_EA_HU_FEATURE, false) ? true : false;

            case PA:
            case RP:
                return featureToggler.getValue(HO_UAN_PA_RP_FEATURE, false) ? true : false;

            default:
                return false;
        }
    }

}
