package uk.gov.hmcts.reform.bailcaseapi.domain.utils;

import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.CASE_MANAGEMENT_LOCATION;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.CASE_MANAGEMENT_LOCATION_REF_DATA;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.HEARING_CENTRE;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.HEARING_CENTRE_REF_DATA;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.IS_BAILS_LOCATION_REFERENCE_DATA_ENABLED;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SELECTED_HEARING_CENTRE_REF_DATA;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.STAFF_LOCATION;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.YesOrNo.YES;

import java.util.Objects;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.bailcaseapi.domain.handlers.presubmit.CaseManagementLocationService;
import uk.gov.hmcts.reform.bailcaseapi.domain.service.LocationRefDataService;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.utils.StaffLocation;

public class HearingCentreUtils {
    private HearingCentreUtils() {
        // Utility class
    }

    public static void setHearingCentre(BailCase bailCase, HearingCentre hearingCentre, CaseManagementLocationService caseManagementLocationService, LocationRefDataService locationRefDataService) {
        bailCase.write(HEARING_CENTRE, hearingCentre);
        String staffLocationName = StaffLocation.getLocation(hearingCentre).getName();
        bailCase.write(STAFF_LOCATION, staffLocationName);
        bailCase.write(
            CASE_MANAGEMENT_LOCATION,
            caseManagementLocationService.getCaseManagementLocation(hearingCentre)
        );

        setHearingCentreRefData(bailCase, hearingCentre, locationRefDataService, caseManagementLocationService);
        bailCase.write(IS_BAILS_LOCATION_REFERENCE_DATA_ENABLED, YES);
    }

    private static void setHearingCentreRefData(BailCase bailCase, HearingCentre hearingCentre, LocationRefDataService locationRefDataService, CaseManagementLocationService caseManagementLocationService) {
        DynamicList hearingCentreDynamicList = locationRefDataService.getCaseManagementLocationDynamicList();
        if (hearingCentreDynamicList != null) {
            hearingCentreDynamicList.getListItems().stream()
                .filter(value -> Objects.equals(value.getCode(), hearingCentre.getEpimsId()))
                .findFirst().ifPresent((value) -> {
                    hearingCentreDynamicList.setValue(value);
                    bailCase.write(SELECTED_HEARING_CENTRE_REF_DATA, value.getLabel());
                });
            bailCase.write(HEARING_CENTRE_REF_DATA, hearingCentreDynamicList);
            bailCase.write(
                CASE_MANAGEMENT_LOCATION_REF_DATA,
                caseManagementLocationService.getRefDataCaseManagementLocation(hearingCentre)
            );
        }
    }
}
