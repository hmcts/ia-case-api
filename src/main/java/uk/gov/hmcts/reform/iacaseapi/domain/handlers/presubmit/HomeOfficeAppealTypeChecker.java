package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;


import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;

public class HomeOfficeAppealTypeChecker {

    private static final String HO_UAN_PA_FEATURE = "home-office-uan-pa-feature";
    private static final String HO_UAN_RP_FEATURE = "home-office-uan-rp-feature";
    private static final String HO_UAN_EA_FEATURE = "home-office-uan-ea-feature";
    private static final String HO_UAN_HU_FEATURE = "home-office-uan-hu-feature";
    private static final String HO_UAN_DC_FEATURE = "home-office-uan-dc-feature";
    private static final String HO_UAN_EU_FEATURE = "home-office-uan-eu-feature";
    private static final String HO_UAN_AG_FEATURE = "home-office-uan-ag-feature";

    private HomeOfficeAppealTypeChecker() {
    }

    protected static boolean isAppealTypeEnabled(FeatureToggler featureToggler, AppealType appealType) {

        switch (appealType) {
            case DC:
                return featureToggler.getValue(HO_UAN_DC_FEATURE, false);
            case EA:
                return featureToggler.getValue(HO_UAN_EA_FEATURE, false);
            case HU:
                return featureToggler.getValue(HO_UAN_HU_FEATURE, false);
            case EU:
                return featureToggler.getValue(HO_UAN_EU_FEATURE, false);
            case PA:
                return featureToggler.getValue(HO_UAN_PA_FEATURE, false);
            case RP:
                return featureToggler.getValue(HO_UAN_RP_FEATURE, false);
            case AG:
                return featureToggler.getValue(HO_UAN_AG_FEATURE, false);

            default:
                return false;
        }
    }

}
