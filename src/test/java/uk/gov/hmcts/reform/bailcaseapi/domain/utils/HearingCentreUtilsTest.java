package uk.gov.hmcts.reform.bailcaseapi.domain.utils;

import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.Mock;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.CaseManagementLocation;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.CaseManagementLocationRefData;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.Value;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.bailcaseapi.domain.handlers.presubmit.CaseManagementLocationService;
import uk.gov.hmcts.reform.bailcaseapi.domain.service.LocationRefDataService;

class HearingCentreUtilsTest {

    @Mock
    private BailCase bailCase;
    private HearingCentre hearingCentre;
    @Mock
    private CaseManagementLocationService caseManagementLocationService;
    @Mock
    private LocationRefDataService locationRefDataService;
    @Mock
    private CaseManagementLocation caseManagementLocation;
    @Mock
    private CaseManagementLocationRefData caseManagementLocationRefData;

    @BeforeEach
    void setUp() {
        openMocks(this);
        hearingCentre = HearingCentre.GLASGOW;
    }

    @Test
    void should_set_all_fields_when_location_ref_data_enabled_via_bail_case() {
        when(bailCase.read(IS_BAILS_LOCATION_REFERENCE_DATA_ENABLED_FT, YesOrNo.class))
            .thenReturn(Optional.of(YesOrNo.YES));
        when(locationRefDataService.getCaseManagementLocationDynamicList())
            .thenReturn(dynamicListWithMatchingItem());
        when(caseManagementLocationService.getCaseManagementLocation(hearingCentre)).thenReturn(caseManagementLocation);
        when(caseManagementLocationService.getRefDataCaseManagementLocation(hearingCentre)).thenReturn(
            caseManagementLocationRefData);

        HearingCentreUtils.setHearingCentre(
            bailCase, hearingCentre, caseManagementLocationService, locationRefDataService);

        verify(bailCase).write(HEARING_CENTRE, hearingCentre);
        verify(bailCase).write(STAFF_LOCATION, "Glasgow");
        verify(bailCase).write(CASE_MANAGEMENT_LOCATION, caseManagementLocation);
        verify(bailCase).write(IS_BAILS_LOCATION_REFERENCE_DATA_ENABLED, YesOrNo.YES);
        verify(bailCase).write(SELECTED_HEARING_CENTRE_REF_DATA, "TestLabel");
        verify(bailCase).write(eq(HEARING_CENTRE_REF_DATA), any(DynamicList.class));
        verify(bailCase).write(CASE_MANAGEMENT_LOCATION_REF_DATA, caseManagementLocationRefData);
    }

    @Test
    void should_set_all_fields() {
        when(bailCase.read(IS_BAILS_LOCATION_REFERENCE_DATA_ENABLED_FT, YesOrNo.class))
            .thenReturn(Optional.empty());
        when(locationRefDataService.getCaseManagementLocationDynamicList())
            .thenReturn(dynamicListWithMatchingItem());
        when(caseManagementLocationService.getCaseManagementLocation(hearingCentre)).thenReturn(caseManagementLocation);
        when(caseManagementLocationService.getRefDataCaseManagementLocation(hearingCentre)).thenReturn(
            caseManagementLocationRefData);

        HearingCentreUtils.setHearingCentre(
            bailCase, hearingCentre, caseManagementLocationService, locationRefDataService);

        verify(bailCase).write(IS_BAILS_LOCATION_REFERENCE_DATA_ENABLED, YesOrNo.YES);
        verify(bailCase).write(SELECTED_HEARING_CENTRE_REF_DATA, "TestLabel");
        verify(bailCase).write(eq(HEARING_CENTRE_REF_DATA), any(DynamicList.class));
        verify(bailCase).write(CASE_MANAGEMENT_LOCATION_REF_DATA, caseManagementLocationRefData);
    }

    @Test
    void should_not_set_selected_hearing_centre_ref_data_if_no_matching_dynamic_list_item() {
        when(bailCase.read(IS_BAILS_LOCATION_REFERENCE_DATA_ENABLED_FT, YesOrNo.class))
            .thenReturn(Optional.of(YesOrNo.YES));
        when(locationRefDataService.getCaseManagementLocationDynamicList())
            .thenReturn(dynamicListWithoutMatchingItem());
        when(caseManagementLocationService.getCaseManagementLocation(hearingCentre)).thenReturn(caseManagementLocation);
        when(caseManagementLocationService.getRefDataCaseManagementLocation(hearingCentre)).thenReturn(
            caseManagementLocationRefData);

        HearingCentreUtils.setHearingCentre(
            bailCase, hearingCentre, caseManagementLocationService, locationRefDataService);

        verify(bailCase, never()).write(eq(SELECTED_HEARING_CENTRE_REF_DATA), any());
        verify(bailCase).write(eq(HEARING_CENTRE_REF_DATA), any(DynamicList.class));
        verify(bailCase).write(CASE_MANAGEMENT_LOCATION_REF_DATA, caseManagementLocationRefData);
    }

    @Test
    void should_handle_empty_dynamic_list_from_location_ref_data_service() {
        DynamicList emptyDynamicList = new DynamicList(null, List.of());
        when(bailCase.read(IS_BAILS_LOCATION_REFERENCE_DATA_ENABLED_FT, YesOrNo.class))
            .thenReturn(Optional.of(YesOrNo.YES));
        when(locationRefDataService.getCaseManagementLocationDynamicList())
            .thenReturn(emptyDynamicList);
        when(caseManagementLocationService.getCaseManagementLocation(hearingCentre)).thenReturn(caseManagementLocation);
        when(caseManagementLocationService.getRefDataCaseManagementLocation(hearingCentre)).thenReturn(
            caseManagementLocationRefData);

        HearingCentreUtils.setHearingCentre(
            bailCase, hearingCentre, caseManagementLocationService, locationRefDataService);

        verify(bailCase, never()).write(eq(SELECTED_HEARING_CENTRE_REF_DATA), any());
        verify(bailCase).write(HEARING_CENTRE_REF_DATA, emptyDynamicList);
        verify(bailCase).write(CASE_MANAGEMENT_LOCATION_REF_DATA, caseManagementLocationRefData);
    }

    @Test
    void should_handle_null_epims_id_in_hearing_centre() {
        HearingCentre mockHearingCentre = mock(HearingCentre.class);
        when(mockHearingCentre.getEpimsId()).thenReturn(null);
        when(mockHearingCentre.getValue()).thenReturn(HearingCentre.GLASGOW.getValue());
        when(bailCase.read(IS_BAILS_LOCATION_REFERENCE_DATA_ENABLED_FT, YesOrNo.class))
            .thenReturn(Optional.of(YesOrNo.YES));
        when(locationRefDataService.getCaseManagementLocationDynamicList())
            .thenReturn(dynamicListWithMatchingItem());
        when(caseManagementLocationService.getCaseManagementLocation(mockHearingCentre)).thenReturn(
            caseManagementLocation);
        when(caseManagementLocationService.getRefDataCaseManagementLocation(mockHearingCentre)).thenReturn(
            caseManagementLocationRefData);

        HearingCentreUtils.setHearingCentre(
            bailCase, mockHearingCentre, caseManagementLocationService, locationRefDataService);

        verify(bailCase, never()).write(eq(SELECTED_HEARING_CENTRE_REF_DATA), any());
        verify(bailCase).write(eq(HEARING_CENTRE_REF_DATA), any(DynamicList.class));
        verify(bailCase).write(CASE_MANAGEMENT_LOCATION_REF_DATA, caseManagementLocationRefData);
    }

    // Helper methods for test data
    private DynamicList dynamicListWithMatchingItem() {
        Value item = new Value(HearingCentre.GLASGOW.getEpimsId(), "TestLabel");
        return new DynamicList(item, List.of(item));
    }

    private DynamicList dynamicListWithoutMatchingItem() {
        Value item = new Value("otherId", "OtherLabel");
        return new DynamicList(item, List.of(item));
    }
}
