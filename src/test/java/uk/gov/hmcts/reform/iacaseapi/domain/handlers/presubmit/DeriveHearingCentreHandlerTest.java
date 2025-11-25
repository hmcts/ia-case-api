package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.AGE_ASSESSMENT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_IN_DETENTION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CASE_MANAGEMENT_LOCATION_REF_DATA;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.DETENTION_FACILITY;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.DETENTION_POSTCODE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IRC_NAME;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_ACCELERATED_DETAINED_APPEAL;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.LEGAL_PRACTICE_ADDRESS_EJP;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.PRISON_NAME;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.SELECTED_HEARING_CENTRE_REF_DATA;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_ADDRESS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_HAS_FIXED_ADDRESS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPLICATION_CHANGE_DESIGNATED_HEARING_CENTRE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CASE_MANAGEMENT_LOCATION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HAS_SPONSOR;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HEARING_CENTRE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HEARING_CENTRE_DYNAMIC_LIST;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_CASE_USING_LOCATION_REF_DATA;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.LEGAL_REP_COMPANY_ADDRESS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.SPONSOR_ADDRESS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.STAFF_LOCATION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WA_DUMMY_POSTCODE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.SUBMIT_APPEAL;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.DetentionFacility.IRC;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.DetentionFacility.PRISON;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.DetentionFacility.OTHER;


import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.BaseLocation;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.CaseManagementLocation;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.CaseManagementLocationRefData;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Region;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Value;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.AddressUk;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.service.HearingCentreFinder;
import uk.gov.hmcts.reform.iacaseapi.domain.service.LocationRefDataService;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.utils.StaffLocation;

@SuppressWarnings("unchecked")
@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class DeriveHearingCentreHandlerTest {

    public static final String HARMONDSWORTH_HEARING_CENTRE = "Harmondsworth Tribunal Hearing Centre";
    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private AddressUk addressUk;
    @Mock
    private AddressUk sponsorOrCompanyAddressUk;

    @Mock
    private HearingCentreFinder hearingCentreFinder;
    @Mock
    private CaseManagementLocationService caseManagementLocationService;
    @Mock
    private LocationRefDataService locationRefDataService;

    private DeriveHearingCentreHandler deriveHearingCentreHandler;

    @BeforeEach
    public void setUp() {
        deriveHearingCentreHandler =
            new DeriveHearingCentreHandler(hearingCentreFinder, caseManagementLocationService, locationRefDataService);
    }

    @ParameterizedTest
    @CsvSource({
        "SUBMIT_APPEAL, MANCHESTER, Manchester, MANCHESTER",
        "EDIT_APPEAL_AFTER_SUBMIT, MANCHESTER, Manchester, MANCHESTER",

        "SUBMIT_APPEAL, TAYLOR_HOUSE, Taylor House, TAYLOR_HOUSE",
        "EDIT_APPEAL_AFTER_SUBMIT, TAYLOR_HOUSE, Taylor House, TAYLOR_HOUSE",

        "SUBMIT_APPEAL, HATTON_CROSS, Hatton Cross, HATTON_CROSS",
        "EDIT_APPEAL_AFTER_SUBMIT, HATTON_CROSS, Hatton Cross, HATTON_CROSS",

        "SUBMIT_APPEAL, NEWCASTLE, Newcastle,",
        "EDIT_APPEAL_AFTER_SUBMIT, NEWCASTLE, Newcastle,"
    })
    void should_derive_hearing_centre_from_appellant_postcode(
        Event event, HearingCentre hearingCentre, String staffLocation, BaseLocation baseLocation) {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(event);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(HEARING_CENTRE)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_ADDRESS)).thenReturn(Optional.of(addressUk));
        when(addressUk.getPostCode()).thenReturn(Optional.of("A123 4BC"));
        when(hearingCentreFinder.find("A123 4BC")).thenReturn(hearingCentre);

        CaseManagementLocation expectedCaseManagementLocation =
            new CaseManagementLocation(Region.NATIONAL, baseLocation);
        when(caseManagementLocationService.getCaseManagementLocation(staffLocation))
            .thenReturn(expectedCaseManagementLocation);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            deriveHearingCentreHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());
        verify(hearingCentreFinder, times(1)).find("A123 4BC");
        verify(asylumCase, times(1)).write(HEARING_CENTRE, hearingCentre);

        verify(asylumCase, times(1)).write(STAFF_LOCATION, staffLocation);
        verify(asylumCase, times(1))
            .write(CASE_MANAGEMENT_LOCATION, expectedCaseManagementLocation);

        verify(asylumCase, times(1)).write(APPLICATION_CHANGE_DESIGNATED_HEARING_CENTRE, hearingCentre);
    }

    @ParameterizedTest
    @CsvSource({
        "SUBMIT_APPEAL, MANCHESTER, Manchester, MANCHESTER, Manchester Tribunal Hearing Centre - Piccadilly Exchange,",
        "EDIT_APPEAL_AFTER_SUBMIT, MANCHESTER, Manchester, MANCHESTER, Manchester Tribunal Hearing Centre - Piccadilly Exchange,",

        "SUBMIT_APPEAL, TAYLOR_HOUSE, Taylor House, TAYLOR_HOUSE, Taylor House Tribunal Hearing Centre,",
        "EDIT_APPEAL_AFTER_SUBMIT, TAYLOR_HOUSE, Taylor House, TAYLOR_HOUSE, Taylor House Tribunal Hearing Centre,",

        "SUBMIT_APPEAL, HATTON_CROSS, Hatton Cross, HATTON_CROSS, Hatton Cross Tribunal Hearing Centre,",
        "EDIT_APPEAL_AFTER_SUBMIT, HATTON_CROSS, Hatton Cross, HATTON_CROSS, Hatton Cross Tribunal Hearing Centre,",

        "SUBMIT_APPEAL, NEWCASTLE, Newcastle, NEWCASTLE, Newcastle Civil And Family Courts And Tribunals Centre,",
        "EDIT_APPEAL_AFTER_SUBMIT, NEWCASTLE, Newcastle, NEWCASTLE, Newcastle Civil And Family Courts And Tribunals Centre,"
    })
    void should_set_hearing_centre_dynamic_list_based_on_appellant_postcode(
        Event event, HearingCentre hearingCentre, String staffLocation, BaseLocation baseLocation, String courtName) {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(event);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(HEARING_CENTRE)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_HAS_FIXED_ADDRESS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(APPELLANT_ADDRESS)).thenReturn(Optional.of(addressUk));
        when(addressUk.getPostCode()).thenReturn(Optional.of("A123 4BC"));
        when(hearingCentreFinder.find("A123 4BC")).thenReturn(hearingCentre);

        Value courtVenue = new Value(hearingCentre.getEpimsId(), courtName);
        DynamicList hearingCentreDynamicList = new DynamicList(courtVenue, List.of(courtVenue));
        when(locationRefDataService.getCaseManagementLocationDynamicList()).thenReturn(hearingCentreDynamicList);

        CaseManagementLocation expectedCaseManagementLocation =
            new CaseManagementLocation(Region.NATIONAL, baseLocation);
        when(caseManagementLocationService.getCaseManagementLocation(staffLocation))
            .thenReturn(expectedCaseManagementLocation);
        when(asylumCase.read(IS_CASE_USING_LOCATION_REF_DATA, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            deriveHearingCentreHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());
        verify(hearingCentreFinder, times(1)).find("A123 4BC");
        verify(asylumCase, times(1)).write(HEARING_CENTRE, hearingCentre);
        verify(asylumCase, times(1)).write(HEARING_CENTRE_DYNAMIC_LIST, hearingCentreDynamicList);

        verify(asylumCase, times(1)).write(STAFF_LOCATION, staffLocation);
        verify(asylumCase, times(1))
            .write(CASE_MANAGEMENT_LOCATION, expectedCaseManagementLocation);

        verify(asylumCase, times(1)).write(APPLICATION_CHANGE_DESIGNATED_HEARING_CENTRE, hearingCentre);
    }

    @ParameterizedTest
    @CsvSource({
        "SUBMIT_APPEAL, MANCHESTER, Manchester, MANCHESTER",
        "EDIT_APPEAL_AFTER_SUBMIT, MANCHESTER, Manchester, MANCHESTER",

        "SUBMIT_APPEAL, TAYLOR_HOUSE, Taylor House, TAYLOR_HOUSE",
        "EDIT_APPEAL_AFTER_SUBMIT, TAYLOR_HOUSE, Taylor House, TAYLOR_HOUSE",

        "SUBMIT_APPEAL, HATTON_CROSS, Hatton Cross, HATTON_CROSS",
        "EDIT_APPEAL_AFTER_SUBMIT, HATTON_CROSS, Hatton Cross, HATTON_CROSS",

        "SUBMIT_APPEAL, NEWCASTLE, Newcastle,",
        "EDIT_APPEAL_AFTER_SUBMIT, NEWCASTLE, Newcastle,"
    })
    void should_derive_hearing_centre_from_sponsor_postcode(
        Event event, HearingCentre hearingCentre, String staffLocation, BaseLocation baseLocation) {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(event);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(HEARING_CENTRE)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_HAS_FIXED_ADDRESS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(HAS_SPONSOR, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(SPONSOR_ADDRESS, AddressUk.class)).thenReturn(Optional.of(sponsorOrCompanyAddressUk));
        when(sponsorOrCompanyAddressUk.getPostCode()).thenReturn(Optional.of("A456 4XY"));
        when(hearingCentreFinder.find("A456 4XY")).thenReturn(hearingCentre);

        CaseManagementLocation expectedCaseManagementLocation =
            new CaseManagementLocation(Region.NATIONAL, baseLocation);
        when(caseManagementLocationService.getCaseManagementLocation(staffLocation))
            .thenReturn(expectedCaseManagementLocation);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            deriveHearingCentreHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());
        verify(hearingCentreFinder, times(1)).find("A456 4XY");
        verify(asylumCase, times(1)).write(HEARING_CENTRE, hearingCentre);

        verify(asylumCase, times(1)).write(STAFF_LOCATION, staffLocation);
        verify(asylumCase, times(1))
            .write(CASE_MANAGEMENT_LOCATION, expectedCaseManagementLocation);

        verify(asylumCase, times(1)).write(APPLICATION_CHANGE_DESIGNATED_HEARING_CENTRE, hearingCentre);
    }

    @ParameterizedTest
    @CsvSource({
        "SUBMIT_APPEAL, MANCHESTER, Manchester, MANCHESTER, Manchester Tribunal Hearing Centre - Piccadilly Exchange,",
        "EDIT_APPEAL_AFTER_SUBMIT, MANCHESTER, Manchester, MANCHESTER, Manchester Tribunal Hearing Centre - Piccadilly Exchange,",

        "SUBMIT_APPEAL, TAYLOR_HOUSE, Taylor House, TAYLOR_HOUSE, Taylor House Tribunal Hearing Centre,",
        "EDIT_APPEAL_AFTER_SUBMIT, TAYLOR_HOUSE, Taylor House, TAYLOR_HOUSE, Taylor House Tribunal Hearing Centre,",

        "SUBMIT_APPEAL, HATTON_CROSS, Hatton Cross, HATTON_CROSS, Hatton Cross Tribunal Hearing Centre,",
        "EDIT_APPEAL_AFTER_SUBMIT, HATTON_CROSS, Hatton Cross, HATTON_CROSS, Hatton Cross Tribunal Hearing Centre,",

        "SUBMIT_APPEAL, NEWCASTLE, Newcastle, NEWCASTLE, Newcastle Civil And Family Courts And Tribunals Centre,",
        "EDIT_APPEAL_AFTER_SUBMIT, NEWCASTLE, Newcastle, NEWCASTLE, Newcastle Civil And Family Courts And Tribunals Centre,"
    })
    void should_set_hearing_centre_dynamic_list_based_on_sponsor_postcode(
        Event event, HearingCentre hearingCentre, String staffLocation, BaseLocation baseLocation, String courtName) {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(event);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(HEARING_CENTRE)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_HAS_FIXED_ADDRESS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(HAS_SPONSOR, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(SPONSOR_ADDRESS, AddressUk.class)).thenReturn(Optional.of(sponsorOrCompanyAddressUk));
        when(sponsorOrCompanyAddressUk.getPostCode()).thenReturn(Optional.of("A456 4XY"));
        when(hearingCentreFinder.find("A456 4XY")).thenReturn(hearingCentre);


        Value courtVenue = new Value(hearingCentre.getEpimsId(), courtName);
        DynamicList hearingCentreDynamicList = new DynamicList(courtVenue, List.of(courtVenue));
        when(locationRefDataService.getCaseManagementLocationDynamicList()).thenReturn(hearingCentreDynamicList);

        CaseManagementLocation expectedCaseManagementLocation =
            new CaseManagementLocation(Region.NATIONAL, baseLocation);
        when(caseManagementLocationService.getCaseManagementLocation(staffLocation))
            .thenReturn(expectedCaseManagementLocation);
        when(asylumCase.read(IS_CASE_USING_LOCATION_REF_DATA, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            deriveHearingCentreHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());
        verify(hearingCentreFinder, times(1)).find("A456 4XY");
        verify(asylumCase, times(1)).write(HEARING_CENTRE, hearingCentre);
        verify(asylumCase, times(1)).write(HEARING_CENTRE_DYNAMIC_LIST, hearingCentreDynamicList);

        verify(asylumCase, times(1)).write(STAFF_LOCATION, staffLocation);
        verify(asylumCase, times(1))
            .write(CASE_MANAGEMENT_LOCATION, expectedCaseManagementLocation);

        verify(asylumCase, times(1)).write(APPLICATION_CHANGE_DESIGNATED_HEARING_CENTRE, hearingCentre);
    }

    @ParameterizedTest
    @CsvSource({
        "SUBMIT_APPEAL, MANCHESTER, Manchester, MANCHESTER, true",
        "EDIT_APPEAL_AFTER_SUBMIT, MANCHESTER, Manchester, MANCHESTER, true",

        "SUBMIT_APPEAL, TAYLOR_HOUSE, Taylor House, TAYLOR_HOUSE, false",
        "EDIT_APPEAL_AFTER_SUBMIT, TAYLOR_HOUSE, Taylor House, TAYLOR_HOUSE, false",

        "SUBMIT_APPEAL, HATTON_CROSS, Hatton Cross, HATTON_CROSS, true",
        "EDIT_APPEAL_AFTER_SUBMIT, HATTON_CROSS, Hatton Cross, HATTON_CROSS, true",

        "SUBMIT_APPEAL, NEWCASTLE, Newcastle, , false",
        "EDIT_APPEAL_AFTER_SUBMIT, NEWCASTLE, Newcastle, , false"
    })
    void should_derive_hearing_centre_from_legal_rep_company_postcode(
        Event event, HearingCentre hearingCentre, String staffLocation, BaseLocation baseLocation, boolean isEjp) {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(event);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(HEARING_CENTRE)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_HAS_FIXED_ADDRESS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(HAS_SPONSOR, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        if (isEjp) {
            when(asylumCase.read(LEGAL_PRACTICE_ADDRESS_EJP, AddressUk.class)).thenReturn(Optional.of(
                sponsorOrCompanyAddressUk));
        } else {
            when(asylumCase.read(LEGAL_REP_COMPANY_ADDRESS, AddressUk.class)).thenReturn(Optional.of(
                sponsorOrCompanyAddressUk));
        }

        when(sponsorOrCompanyAddressUk.getPostCode()).thenReturn(Optional.of("A456 4XY"));
        when(hearingCentreFinder.find("A456 4XY")).thenReturn(hearingCentre);

        CaseManagementLocation expectedCaseManagementLocation =
            new CaseManagementLocation(Region.NATIONAL, baseLocation);
        when(caseManagementLocationService.getCaseManagementLocation(staffLocation))
            .thenReturn(expectedCaseManagementLocation);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            deriveHearingCentreHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());
        verify(hearingCentreFinder, times(1)).find("A456 4XY");
        verify(asylumCase, times(1)).write(HEARING_CENTRE, hearingCentre);

        verify(asylumCase, times(1)).write(STAFF_LOCATION, staffLocation);
        verify(asylumCase, times(1))
            .write(CASE_MANAGEMENT_LOCATION, expectedCaseManagementLocation);

        verify(asylumCase, times(1)).write(APPLICATION_CHANGE_DESIGNATED_HEARING_CENTRE, hearingCentre);
    }

    @ParameterizedTest
    @CsvSource({
        "SUBMIT_APPEAL, MANCHESTER, Manchester, MANCHESTER, Manchester Tribunal Hearing Centre - Piccadilly Exchange, true",
        "EDIT_APPEAL_AFTER_SUBMIT, MANCHESTER, Manchester, MANCHESTER, Manchester Tribunal Hearing Centre - Piccadilly Exchange,true",

        "SUBMIT_APPEAL, TAYLOR_HOUSE, Taylor House, TAYLOR_HOUSE, Taylor House Tribunal Hearing Centre, false",
        "EDIT_APPEAL_AFTER_SUBMIT, TAYLOR_HOUSE, Taylor House, TAYLOR_HOUSE, Taylor House Tribunal Hearing Centre, false",

        "SUBMIT_APPEAL, HATTON_CROSS, Hatton Cross, HATTON_CROSS, Hatton Cross Tribunal Hearing Centre, true",
        "EDIT_APPEAL_AFTER_SUBMIT, HATTON_CROSS, Hatton Cross, HATTON_CROSS, Hatton Cross Tribunal Hearing Centre, true",

        "SUBMIT_APPEAL, NEWCASTLE, Newcastle, NEWCASTLE, Newcastle Civil And Family Courts And Tribunals Centre, false",
        "EDIT_APPEAL_AFTER_SUBMIT, NEWCASTLE, Newcastle, NEWCASTLE, Newcastle Civil And Family Courts And Tribunals Centre, false"
    })
    void should_set_hearing_centre_dynamic_list_based_on_legal_rep_company_postcode(
        Event event, HearingCentre hearingCentre, String staffLocation,
        BaseLocation baseLocation, String courtName, boolean isEjp) {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(event);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(HEARING_CENTRE)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_HAS_FIXED_ADDRESS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(HAS_SPONSOR, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        if (isEjp) {
            when(asylumCase.read(LEGAL_PRACTICE_ADDRESS_EJP, AddressUk.class)).thenReturn(Optional.of(
                sponsorOrCompanyAddressUk));
        } else {
            when(asylumCase.read(LEGAL_REP_COMPANY_ADDRESS, AddressUk.class)).thenReturn(Optional.of(
                sponsorOrCompanyAddressUk));
        }

        when(sponsorOrCompanyAddressUk.getPostCode()).thenReturn(Optional.of("A456 4XY"));
        when(hearingCentreFinder.find("A456 4XY")).thenReturn(hearingCentre);


        Value courtVenue = new Value(hearingCentre.getEpimsId(), courtName);
        DynamicList hearingCentreDynamicList = new DynamicList(courtVenue, List.of(courtVenue));
        when(locationRefDataService.getCaseManagementLocationDynamicList()).thenReturn(hearingCentreDynamicList);

        CaseManagementLocation expectedCaseManagementLocation =
            new CaseManagementLocation(Region.NATIONAL, baseLocation);
        when(caseManagementLocationService.getCaseManagementLocation(staffLocation))
            .thenReturn(expectedCaseManagementLocation);
        when(asylumCase.read(IS_CASE_USING_LOCATION_REF_DATA, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            deriveHearingCentreHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());
        verify(hearingCentreFinder, times(1)).find("A456 4XY");
        verify(asylumCase, times(1)).write(HEARING_CENTRE, hearingCentre);
        verify(asylumCase, times(1)).write(HEARING_CENTRE_DYNAMIC_LIST, hearingCentreDynamicList);

        verify(asylumCase, times(1)).write(STAFF_LOCATION, staffLocation);
        verify(asylumCase, times(1))
            .write(CASE_MANAGEMENT_LOCATION, expectedCaseManagementLocation);

        verify(asylumCase, times(1)).write(APPLICATION_CHANGE_DESIGNATED_HEARING_CENTRE, hearingCentre);
    }

    @ParameterizedTest
    @CsvSource({
        "SUBMIT_APPEAL, MANCHESTER, Manchester, MANCHESTER",
        "EDIT_APPEAL_AFTER_SUBMIT, MANCHESTER, Manchester, MANCHESTER",

        "SUBMIT_APPEAL, NEWPORT, Newport, NEWPORT",
        "EDIT_APPEAL_AFTER_SUBMIT, NEWPORT, Newport, NEWPORT",

        "SUBMIT_APPEAL, HATTON_CROSS, Hatton Cross, HATTON_CROSS",
        "EDIT_APPEAL_AFTER_SUBMIT, HATTON_CROSS, Hatton Cross, HATTON_CROSS",

        "SUBMIT_APPEAL, NEWCASTLE, Newcastle,",
        "EDIT_APPEAL_AFTER_SUBMIT, NEWCASTLE, Newcastle,"
    })
    void should_use_default_hearing_centre_if_sponsor_present_and_no_valid_address(
        Event event, HearingCentre hearingCentre, String staffLocation, BaseLocation baseLocation) {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(event);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(HEARING_CENTRE)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_HAS_FIXED_ADDRESS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(HAS_SPONSOR, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(hearingCentreFinder.getDefaultHearingCentre()).thenReturn(hearingCentre);

        CaseManagementLocation expectedCaseManagementLocation =
            new CaseManagementLocation(Region.NATIONAL, baseLocation);
        when(caseManagementLocationService.getCaseManagementLocation(staffLocation))
            .thenReturn(expectedCaseManagementLocation);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            deriveHearingCentreHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());
        verify(asylumCase, times(1)).write(HEARING_CENTRE, hearingCentre);

        verify(asylumCase, times(1)).write(STAFF_LOCATION, staffLocation);
        verify(asylumCase, times(1))
            .write(CASE_MANAGEMENT_LOCATION, expectedCaseManagementLocation);

        verify(asylumCase, times(1)).write(APPLICATION_CHANGE_DESIGNATED_HEARING_CENTRE, hearingCentre);
    }

    @ParameterizedTest
    @CsvSource({
        "SUBMIT_APPEAL, MANCHESTER, Manchester, MANCHESTER, Manchester Tribunal Hearing Centre - Piccadilly Exchange,",
        "EDIT_APPEAL_AFTER_SUBMIT, MANCHESTER, Manchester, MANCHESTER, Manchester Tribunal Hearing Centre - Piccadilly Exchange,",

        "SUBMIT_APPEAL, TAYLOR_HOUSE, Taylor House, TAYLOR_HOUSE, Taylor House Tribunal Hearing Centre,",
        "EDIT_APPEAL_AFTER_SUBMIT, TAYLOR_HOUSE, Taylor House, TAYLOR_HOUSE, Taylor House Tribunal Hearing Centre,",

        "SUBMIT_APPEAL, HATTON_CROSS, Hatton Cross, HATTON_CROSS, Hatton Cross Tribunal Hearing Centre,",
        "EDIT_APPEAL_AFTER_SUBMIT, HATTON_CROSS, Hatton Cross, HATTON_CROSS, Hatton Cross Tribunal Hearing Centre,",

        "SUBMIT_APPEAL, NEWCASTLE, Newcastle, NEWCASTLE, Newcastle Civil And Family Courts And Tribunals Centre,",
        "EDIT_APPEAL_AFTER_SUBMIT, NEWCASTLE, Newcastle, NEWCASTLE, Newcastle Civil And Family Courts And Tribunals Centre,"
    })
    void should_set_hearing_centre_dynamic_list_when_sponsor_present_and_no_valid_address(
        Event event, HearingCentre hearingCentre, String staffLocation, BaseLocation baseLocation, String courtName) {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(event);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(HEARING_CENTRE)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_HAS_FIXED_ADDRESS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(HAS_SPONSOR, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(hearingCentreFinder.getDefaultHearingCentre()).thenReturn(hearingCentre);

        Value courtVenue = new Value(hearingCentre.getEpimsId(), courtName);
        DynamicList hearingCentreDynamicList = new DynamicList(courtVenue, List.of(courtVenue));
        when(locationRefDataService.getCaseManagementLocationDynamicList()).thenReturn(hearingCentreDynamicList);

        CaseManagementLocation expectedCaseManagementLocation =
            new CaseManagementLocation(Region.NATIONAL, baseLocation);
        when(caseManagementLocationService.getCaseManagementLocation(staffLocation))
            .thenReturn(expectedCaseManagementLocation);
        when(asylumCase.read(IS_CASE_USING_LOCATION_REF_DATA, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            deriveHearingCentreHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());
        verify(asylumCase, times(1)).write(HEARING_CENTRE, hearingCentre);
        verify(asylumCase, times(1)).write(HEARING_CENTRE_DYNAMIC_LIST, hearingCentreDynamicList);

        verify(asylumCase, times(1)).write(STAFF_LOCATION, staffLocation);
        verify(asylumCase, times(1))
            .write(CASE_MANAGEMENT_LOCATION, expectedCaseManagementLocation);

        verify(asylumCase, times(1)).write(APPLICATION_CHANGE_DESIGNATED_HEARING_CENTRE, hearingCentre);
    }


    @ParameterizedTest
    @CsvSource({
        "SUBMIT_APPEAL, MANCHESTER, Manchester, MANCHESTER",
        "EDIT_APPEAL_AFTER_SUBMIT, MANCHESTER, Manchester, MANCHESTER",

        "SUBMIT_APPEAL, NEWPORT, Newport, NEWPORT",
        "EDIT_APPEAL_AFTER_SUBMIT, NEWPORT, Newport, NEWPORT",

        "SUBMIT_APPEAL, HATTON_CROSS, Hatton Cross, HATTON_CROSS",
        "EDIT_APPEAL_AFTER_SUBMIT, HATTON_CROSS, Hatton Cross, HATTON_CROSS",

        "SUBMIT_APPEAL, NEWCASTLE, Newcastle,",
        "EDIT_APPEAL_AFTER_SUBMIT, NEWCASTLE, Newcastle,"
    })
    void should_use_default_hearing_centre_if_appellant_has_no_fixed_address(
        Event event, HearingCentre hearingCentre, String staffLocation, BaseLocation baseLocation) {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(event);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(HEARING_CENTRE)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_HAS_FIXED_ADDRESS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(hearingCentreFinder.getDefaultHearingCentre()).thenReturn(hearingCentre);

        CaseManagementLocation expectedCaseManagementLocation =
            new CaseManagementLocation(Region.NATIONAL, baseLocation);
        when(caseManagementLocationService.getCaseManagementLocation(staffLocation))
            .thenReturn(expectedCaseManagementLocation);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            deriveHearingCentreHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());
        verify(asylumCase, times(1)).write(HEARING_CENTRE, hearingCentre);

        verify(asylumCase, times(1)).write(STAFF_LOCATION, staffLocation);
        verify(asylumCase, times(1))
            .write(CASE_MANAGEMENT_LOCATION, expectedCaseManagementLocation);

        verify(asylumCase, times(1)).write(APPLICATION_CHANGE_DESIGNATED_HEARING_CENTRE, hearingCentre);
    }

    @ParameterizedTest
    @CsvSource({
        "SUBMIT_APPEAL, MANCHESTER, Manchester, MANCHESTER, Manchester Tribunal Hearing Centre - Piccadilly Exchange,",
        "EDIT_APPEAL_AFTER_SUBMIT, MANCHESTER, Manchester, MANCHESTER, Manchester Tribunal Hearing Centre - Piccadilly Exchange,",

        "SUBMIT_APPEAL, TAYLOR_HOUSE, Taylor House, TAYLOR_HOUSE, Taylor House Tribunal Hearing Centre,",
        "EDIT_APPEAL_AFTER_SUBMIT, TAYLOR_HOUSE, Taylor House, TAYLOR_HOUSE, Taylor House Tribunal Hearing Centre,",

        "SUBMIT_APPEAL, HATTON_CROSS, Hatton Cross, HATTON_CROSS, Hatton Cross Tribunal Hearing Centre,",
        "EDIT_APPEAL_AFTER_SUBMIT, HATTON_CROSS, Hatton Cross, HATTON_CROSS, Hatton Cross Tribunal Hearing Centre,",

        "SUBMIT_APPEAL, NEWCASTLE, Newcastle, NEWCASTLE, Newcastle Civil And Family Courts And Tribunals Centre,",
        "EDIT_APPEAL_AFTER_SUBMIT, NEWCASTLE, Newcastle, NEWCASTLE, Newcastle Civil And Family Courts And Tribunals Centre,"
    })
    void should_set_hearing_centre_dynamic_list_when_appellant_has_no_fixed_address(
        Event event, HearingCentre hearingCentre, String staffLocation, BaseLocation baseLocation, String courtName) {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(event);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(HEARING_CENTRE)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_HAS_FIXED_ADDRESS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(hearingCentreFinder.getDefaultHearingCentre()).thenReturn(hearingCentre);

        Value courtVenue = new Value(hearingCentre.getEpimsId(), courtName);
        DynamicList hearingCentreDynamicList = new DynamicList(courtVenue, List.of(courtVenue));
        when(locationRefDataService.getCaseManagementLocationDynamicList()).thenReturn(hearingCentreDynamicList);

        CaseManagementLocation expectedCaseManagementLocation =
            new CaseManagementLocation(Region.NATIONAL, baseLocation);
        when(caseManagementLocationService.getCaseManagementLocation(staffLocation))
            .thenReturn(expectedCaseManagementLocation);
        when(asylumCase.read(IS_CASE_USING_LOCATION_REF_DATA, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            deriveHearingCentreHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());
        verify(asylumCase, times(1)).write(HEARING_CENTRE, hearingCentre);
        verify(asylumCase, times(1)).write(HEARING_CENTRE_DYNAMIC_LIST, hearingCentreDynamicList);

        verify(asylumCase, times(1)).write(STAFF_LOCATION, staffLocation);
        verify(asylumCase, times(1))
            .write(CASE_MANAGEMENT_LOCATION, expectedCaseManagementLocation);

        verify(asylumCase, times(1)).write(APPLICATION_CHANGE_DESIGNATED_HEARING_CENTRE, hearingCentre);
    }

    @ParameterizedTest
    @CsvSource({
        "SUBMIT_APPEAL, TAYLOR_HOUSE, Taylor House, TAYLOR_HOUSE",
        "EDIT_APPEAL_AFTER_SUBMIT, TAYLOR_HOUSE, Taylor House, TAYLOR_HOUSE"
    })
    void should_derive_hearing_centre_from_wa_dummy_postcode_if_available(
        Event event, HearingCentre hearingCentre, String staffLocation, BaseLocation baseLocation) {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(event);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(HEARING_CENTRE)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_HAS_FIXED_ADDRESS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(WA_DUMMY_POSTCODE, String.class)).thenReturn(Optional.of("SE10 9EQ"));
        when(hearingCentreFinder.find("SE10 9EQ")).thenReturn(hearingCentre);

        CaseManagementLocation expectedCaseManagementLocation =
            new CaseManagementLocation(Region.NATIONAL, baseLocation);
        when(caseManagementLocationService.getCaseManagementLocation(staffLocation))
            .thenReturn(expectedCaseManagementLocation);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            deriveHearingCentreHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());
        verify(hearingCentreFinder, times(1)).find("SE10 9EQ");
        verify(asylumCase, times(1)).write(HEARING_CENTRE, hearingCentre);

        verify(asylumCase, times(1)).write(STAFF_LOCATION, staffLocation);
        verify(asylumCase, times(1))
            .write(CASE_MANAGEMENT_LOCATION, expectedCaseManagementLocation);

        verify(asylumCase, times(1)).write(APPLICATION_CHANGE_DESIGNATED_HEARING_CENTRE, hearingCentre);
    }

    @Test
    void should_not_set_hearing_centre_if_already_exists() {

        final HearingCentre existingHearingCentre = HearingCentre.MANCHESTER;

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(SUBMIT_APPEAL);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(DETENTION_FACILITY)).thenReturn(Optional.of(OTHER));
        when(asylumCase.read(HEARING_CENTRE)).thenReturn(Optional.of(existingHearingCentre));
        when(asylumCase.read(STAFF_LOCATION))
            .thenReturn(Optional.of(StaffLocation.getLocation(existingHearingCentre).getName()));


        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            deriveHearingCentreHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());
        verify(asylumCase, never()).write(any(), any());
        verify(asylumCase, never()).write(STAFF_LOCATION, StaffLocation.getLocation(existingHearingCentre).getName());
    }

    @Test
    void should_set_hearing_centre_for_detained_appeals_ada_or_aaa_with_detention_post_code() {
        when(callback.getEvent()).thenReturn(SUBMIT_APPEAL);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getCaseDetails().getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(DETENTION_FACILITY)).thenReturn(Optional.of(OTHER));
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(AGE_ASSESSMENT, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(DETENTION_POSTCODE, String.class)).thenReturn(Optional.of("NW7 1BJ"));
        when(hearingCentreFinder.find("NW7 1BJ")).thenReturn(HearingCentre.HATTON_CROSS);

        deriveHearingCentreHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        verify(asylumCase, times(1)).write(HEARING_CENTRE, HearingCentre.HATTON_CROSS);
    }

    @Test
    void should_set_default_hearing_centre_for_detained_appeals_ada_or_aaa_with_blank_detention_post_code() {
        when(callback.getEvent()).thenReturn(SUBMIT_APPEAL);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getCaseDetails().getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(DETENTION_FACILITY)).thenReturn(Optional.of(OTHER));
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(AGE_ASSESSMENT, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(DETENTION_POSTCODE, String.class)).thenReturn(Optional.of(""));
        when(hearingCentreFinder.find("")).thenReturn(HearingCentre.NEWPORT);

        deriveHearingCentreHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        verify(asylumCase, times(1)).write(HEARING_CENTRE, HearingCentre.NEWPORT);
    }

    @Test
    void should_set_default_hearing_centre_for_detained_appeals_ada_or_aaa_without_detention_post_code() {
        when(callback.getEvent()).thenReturn(SUBMIT_APPEAL);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getCaseDetails().getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(DETENTION_FACILITY)).thenReturn(Optional.of(OTHER));
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(AGE_ASSESSMENT, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(DETENTION_POSTCODE, String.class)).thenReturn(Optional.empty());
        when(hearingCentreFinder.getDefaultHearingCentre()).thenReturn(HearingCentre.NEWPORT);

        deriveHearingCentreHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        verify(asylumCase, times(1)).write(HEARING_CENTRE, HearingCentre.NEWPORT);
    }

    @Test
    void should_set_hearing_centre_dynamic_list_for_detained_appeals_ada_or_aaa_with_detention_post_code() {
        when(callback.getEvent()).thenReturn(SUBMIT_APPEAL);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getCaseDetails().getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(DETENTION_FACILITY)).thenReturn(Optional.of(OTHER));
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(AGE_ASSESSMENT, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(IS_CASE_USING_LOCATION_REF_DATA, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(DETENTION_POSTCODE, String.class)).thenReturn(Optional.of("NW7 1BJ"));
        when(hearingCentreFinder.find("NW7 1BJ")).thenReturn(HearingCentre.HATTON_CROSS);

        Value courtVenue = new Value(HearingCentre.HARMONDSWORTH.getEpimsId(), HARMONDSWORTH_HEARING_CENTRE);
        DynamicList hearingCentreDynamicList = new DynamicList(courtVenue, List.of(courtVenue));
        when(locationRefDataService.getCaseManagementLocationDynamicList()).thenReturn(hearingCentreDynamicList);

        deriveHearingCentreHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        verify(asylumCase, times(1)).write(HEARING_CENTRE, HearingCentre.HATTON_CROSS);
        verify(asylumCase, times(1)).write(HEARING_CENTRE_DYNAMIC_LIST, hearingCentreDynamicList);
    }

    @Test
    void should_set_hearing_centre_dynamic_list_for_detained_appeals_ada_or_aaa_with_blank_detention_post_code() {
        when(callback.getEvent()).thenReturn(SUBMIT_APPEAL);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getCaseDetails().getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(DETENTION_FACILITY)).thenReturn(Optional.of(OTHER));
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(AGE_ASSESSMENT, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(IS_CASE_USING_LOCATION_REF_DATA, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(DETENTION_POSTCODE, String.class)).thenReturn(Optional.of(""));
        when(hearingCentreFinder.find("")).thenReturn(HearingCentre.NEWPORT);

        Value courtVenue = new Value(HearingCentre.HARMONDSWORTH.getEpimsId(), HARMONDSWORTH_HEARING_CENTRE);
        DynamicList hearingCentreDynamicList = new DynamicList(courtVenue, List.of(courtVenue));
        when(locationRefDataService.getCaseManagementLocationDynamicList()).thenReturn(hearingCentreDynamicList);

        deriveHearingCentreHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        verify(asylumCase, times(1)).write(HEARING_CENTRE, HearingCentre.NEWPORT);
        verify(asylumCase, times(1)).write(HEARING_CENTRE_DYNAMIC_LIST, hearingCentreDynamicList);
    }

    @Test
    void should_set_hearing_centre_dynamic_list_for_detained_appeals_ada_or_aaa_without_detention_post_code() {
        when(callback.getEvent()).thenReturn(SUBMIT_APPEAL);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getCaseDetails().getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(DETENTION_FACILITY)).thenReturn(Optional.of(OTHER));
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(AGE_ASSESSMENT, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(IS_CASE_USING_LOCATION_REF_DATA, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(DETENTION_POSTCODE, String.class)).thenReturn(Optional.empty());
        when(hearingCentreFinder.getDefaultHearingCentre()).thenReturn(HearingCentre.NEWPORT);

        Value courtVenue = new Value(HearingCentre.HARMONDSWORTH.getEpimsId(), HARMONDSWORTH_HEARING_CENTRE);
        DynamicList hearingCentreDynamicList = new DynamicList(courtVenue, List.of(courtVenue));
        when(locationRefDataService.getCaseManagementLocationDynamicList()).thenReturn(hearingCentreDynamicList);

        deriveHearingCentreHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        verify(asylumCase, times(1)).write(HEARING_CENTRE, HearingCentre.NEWPORT);
        verify(asylumCase, times(1)).write(HEARING_CENTRE_DYNAMIC_LIST, hearingCentreDynamicList);
    }

    @ParameterizedTest
    @CsvSource({
        "Garth, MANCHESTER, Manchester, MANCHESTER", "Highpoint, TAYLOR_HOUSE, Taylor House, TAYLOR_HOUSE",
        "Swansea, NEWPORT, Newport, NEWPORT", "Perth, GLASGOW, Glasgow, GLASGOW",
        "Reading, BIRMINGHAM, Birmingham, BIRMINGHAM"
    })
    void should_derive_hearing_centre_from_detention_facility_name_from_prison(
            String prisonName, HearingCentre hearingCentre, String staffLocation, BaseLocation baseLocation
    ) {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(SUBMIT_APPEAL);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(DETENTION_FACILITY)).thenReturn(Optional.of(PRISON));
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(AGE_ASSESSMENT, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(PRISON_NAME, String.class)).thenReturn(Optional.of(prisonName));
        when(asylumCase.read(IRC_NAME, String.class)).thenReturn(Optional.empty());
        when(hearingCentreFinder.findByDetentionFacility(prisonName)).thenReturn(hearingCentre);

        CaseManagementLocation expectedCaseManagementLocation =
                new CaseManagementLocation(Region.NATIONAL, baseLocation);
        when(caseManagementLocationService.getCaseManagementLocation(staffLocation))
                .thenReturn(expectedCaseManagementLocation);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                deriveHearingCentreHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertThat(callbackResponse.getData()).isEqualTo(asylumCase);

        verify(hearingCentreFinder, times(1)).findByDetentionFacility(prisonName);
        verify(asylumCase, times(1)).write(HEARING_CENTRE, hearingCentre);
    }

    @ParameterizedTest
    @CsvSource({
        "Garth, MANCHESTER, Manchester, MANCHESTER", "Highpoint, TAYLOR_HOUSE, Taylor House, TAYLOR_HOUSE",
        "Swansea, NEWPORT, Newport, NEWPORT", "Perth, GLASGOW, Glasgow, GLASGOW",
        "Reading, BIRMINGHAM, Birmingham, BIRMINGHAM"
    })
    void should_set_hearing_centre_dynamic_list_from_detention_facility_name_from_prison(
        String prisonName, HearingCentre hearingCentre, String staffLocation, BaseLocation baseLocation
    ) {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(SUBMIT_APPEAL);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(DETENTION_FACILITY)).thenReturn(Optional.of(PRISON));
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(AGE_ASSESSMENT, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(PRISON_NAME, String.class)).thenReturn(Optional.of(prisonName));
        when(asylumCase.read(IRC_NAME, String.class)).thenReturn(Optional.empty());
        when(hearingCentreFinder.findByDetentionFacility(prisonName)).thenReturn(hearingCentre);
        when(asylumCase.read(IS_CASE_USING_LOCATION_REF_DATA, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        Value courtVenue = new Value(HearingCentre.HARMONDSWORTH.getEpimsId(), HARMONDSWORTH_HEARING_CENTRE);
        DynamicList hearingCentreDynamicList = new DynamicList(courtVenue, List.of(courtVenue));
        when(locationRefDataService.getCaseManagementLocationDynamicList()).thenReturn(hearingCentreDynamicList);

        CaseManagementLocation expectedCaseManagementLocation =
            new CaseManagementLocation(Region.NATIONAL, baseLocation);
        when(caseManagementLocationService.getCaseManagementLocation(staffLocation))
            .thenReturn(expectedCaseManagementLocation);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            deriveHearingCentreHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertThat(callbackResponse.getData()).isEqualTo(asylumCase);

        verify(hearingCentreFinder, times(1)).findByDetentionFacility(prisonName);
        verify(asylumCase, times(1)).write(HEARING_CENTRE, hearingCentre);
        verify(asylumCase, times(1)).write(HEARING_CENTRE_DYNAMIC_LIST, hearingCentreDynamicList);
    }

    @ParameterizedTest
    @CsvSource({
        "Brookhouse, TAYLOR_HOUSE, Taylor House, TAYLOR_HOUSE", "Dungavel, GLASGOW, Glasgow, GLASGOW",
        "Harmondsworth, HATTON_CROSS, Hatton Cross, HATTON_CROSS", "Derwentside, BRADFORD, Bradford, BRADFORD",
        "Yarlswood, YARLS_WOOD, Yarls Wood, YARLS_WOOD"
    })
    void should_derive_hearing_centre_from_detention_facility_name_from_irc(
            String ircName, HearingCentre hearingCentre, String staffLocation, BaseLocation baseLocation
    ) {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(SUBMIT_APPEAL);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(DETENTION_FACILITY)).thenReturn(Optional.of(IRC));
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(AGE_ASSESSMENT, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(PRISON_NAME, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(IRC_NAME, String.class)).thenReturn(Optional.of(ircName));
        when(hearingCentreFinder.findByDetentionFacility(ircName)).thenReturn(hearingCentre);

        CaseManagementLocation expectedCaseManagementLocation =
                new CaseManagementLocation(Region.NATIONAL, baseLocation);
        when(caseManagementLocationService.getCaseManagementLocation(staffLocation))
                .thenReturn(expectedCaseManagementLocation);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                deriveHearingCentreHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertThat(callbackResponse.getData()).isEqualTo(asylumCase);

        verify(hearingCentreFinder, times(1)).findByDetentionFacility(ircName);
        verify(asylumCase, times(1)).write(HEARING_CENTRE, hearingCentre);
    }

    @ParameterizedTest
    @CsvSource({
        "Brookhouse, TAYLOR_HOUSE, Taylor House, TAYLOR_HOUSE", "Dungavel, GLASGOW, Glasgow, GLASGOW",
        "Harmondsworth, HATTON_CROSS, Hatton Cross, HATTON_CROSS", "Derwentside, BRADFORD, Bradford, BRADFORD",
        "Yarlswood, YARLS_WOOD, Yarls Wood, YARLS_WOOD"
    })
    void should_set_hearing_centre_dynamic_list_from_detention_facility_name_from_irc(
        String ircName, HearingCentre hearingCentre, String staffLocation, BaseLocation baseLocation
    ) {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(SUBMIT_APPEAL);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(DETENTION_FACILITY)).thenReturn(Optional.of(IRC));
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(AGE_ASSESSMENT, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(PRISON_NAME, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(IRC_NAME, String.class)).thenReturn(Optional.of(ircName));
        when(hearingCentreFinder.findByDetentionFacility(ircName)).thenReturn(hearingCentre);
        when(asylumCase.read(IS_CASE_USING_LOCATION_REF_DATA, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        Value courtVenue = new Value(HearingCentre.HARMONDSWORTH.getEpimsId(), HARMONDSWORTH_HEARING_CENTRE);
        DynamicList hearingCentreDynamicList = new DynamicList(courtVenue, List.of(courtVenue));
        when(locationRefDataService.getCaseManagementLocationDynamicList()).thenReturn(hearingCentreDynamicList);

        CaseManagementLocation expectedCaseManagementLocation =
            new CaseManagementLocation(Region.NATIONAL, baseLocation);
        when(caseManagementLocationService.getCaseManagementLocation(staffLocation))
            .thenReturn(expectedCaseManagementLocation);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            deriveHearingCentreHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertThat(callbackResponse.getData()).isEqualTo(asylumCase);

        verify(hearingCentreFinder, times(1)).findByDetentionFacility(ircName);
        verify(asylumCase, times(1)).write(HEARING_CENTRE, hearingCentre);
        verify(asylumCase, times(1)).write(HEARING_CENTRE_DYNAMIC_LIST, hearingCentreDynamicList);
    }

    @Test
    void should_throw_for_empty_prison_and_irc() {

        when(asylumCase.read(PRISON_NAME, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(IRC_NAME, String.class)).thenReturn(Optional.empty());

        Assertions.assertThatThrownBy(
                        () -> deriveHearingCentreHandler.getHearingCentreFromDetentionFacilityName(asylumCase))
                .hasMessage("Prison name and IRC name missing")
                .isExactlyInstanceOf(RequiredFieldMissingException.class);

    }

    @Test
    void should_be_handled_at_latest_point() {
        assertEquals(DispatchPriority.LATEST, deriveHearingCentreHandler.getDispatchPriority());
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> deriveHearingCentreHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.SEND_DIRECTION);
        assertThatThrownBy(() -> deriveHearingCentreHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = deriveHearingCentreHandler.canHandle(callbackStage, callback);

                if (Arrays.asList(
                    SUBMIT_APPEAL,
                    Event.EDIT_APPEAL_AFTER_SUBMIT)
                    .contains(callback.getEvent())
                    && callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT) {

                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }

            reset(callback);
        }
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> deriveHearingCentreHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> deriveHearingCentreHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> deriveHearingCentreHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> deriveHearingCentreHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void should_set_ref_data_cml_if_ref_data_enabled_with_blank_detention_post_code() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(SUBMIT_APPEAL);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.empty());
        when(asylumCase.read(IS_CASE_USING_LOCATION_REF_DATA, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(DETENTION_POSTCODE, String.class)).thenReturn(Optional.of(""));
        when(hearingCentreFinder.find("")).thenReturn(HearingCentre.NEWPORT);

        Value courtVenue = new Value(HearingCentre.HARMONDSWORTH.getEpimsId(), HARMONDSWORTH_HEARING_CENTRE);
        DynamicList cmlDynamicList = new DynamicList(courtVenue, List.of(courtVenue));
        when(locationRefDataService.getCaseManagementLocationDynamicList()).thenReturn(cmlDynamicList);

        CaseManagementLocationRefData expectedCml = new CaseManagementLocationRefData(Region.NATIONAL, cmlDynamicList);
        when(caseManagementLocationService.getRefDataCaseManagementLocation(any()))
            .thenReturn(expectedCml);

        deriveHearingCentreHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        verify(asylumCase, times(1)).write(CASE_MANAGEMENT_LOCATION_REF_DATA, expectedCml);
        verify(asylumCase, times(1)).write(SELECTED_HEARING_CENTRE_REF_DATA, HARMONDSWORTH_HEARING_CENTRE);
    }

    @Test
    void should_set_ref_data_cml_if_ref_data_enabled_with_detention_post_code() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(SUBMIT_APPEAL);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.empty());
        when(asylumCase.read(IS_CASE_USING_LOCATION_REF_DATA, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(DETENTION_POSTCODE, String.class)).thenReturn(Optional.of("NW7 1BJ"));
        when(hearingCentreFinder.find("NW7 1BJ")).thenReturn(HearingCentre.HATTON_CROSS);

        Value courtVenue = new Value(HearingCentre.HARMONDSWORTH.getEpimsId(), HARMONDSWORTH_HEARING_CENTRE);
        DynamicList cmlDynamicList = new DynamicList(courtVenue, List.of(courtVenue));
        when(locationRefDataService.getCaseManagementLocationDynamicList()).thenReturn(cmlDynamicList);

        CaseManagementLocationRefData expectedCml = new CaseManagementLocationRefData(Region.NATIONAL, cmlDynamicList);
        when(caseManagementLocationService.getRefDataCaseManagementLocation(any()))
            .thenReturn(expectedCml);

        deriveHearingCentreHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        verify(asylumCase, times(1)).write(CASE_MANAGEMENT_LOCATION_REF_DATA, expectedCml);
        verify(asylumCase, times(1)).write(SELECTED_HEARING_CENTRE_REF_DATA, HARMONDSWORTH_HEARING_CENTRE);
    }

    @Test
    void should_set_ref_data_cml_if_ref_data_enabled_without_detention_post_code() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(SUBMIT_APPEAL);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.empty());
        when(asylumCase.read(IS_CASE_USING_LOCATION_REF_DATA, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(DETENTION_POSTCODE, String.class)).thenReturn(Optional.empty());
        when(hearingCentreFinder.getDefaultHearingCentre()).thenReturn(HearingCentre.NEWPORT);

        Value courtVenue = new Value(HearingCentre.HARMONDSWORTH.getEpimsId(), HARMONDSWORTH_HEARING_CENTRE);
        DynamicList cmlDynamicList = new DynamicList(courtVenue, List.of(courtVenue));
        when(locationRefDataService.getCaseManagementLocationDynamicList()).thenReturn(cmlDynamicList);

        CaseManagementLocationRefData expectedCml = new CaseManagementLocationRefData(Region.NATIONAL, cmlDynamicList);
        when(caseManagementLocationService.getRefDataCaseManagementLocation(any()))
            .thenReturn(expectedCml);

        deriveHearingCentreHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        verify(asylumCase, times(1)).write(CASE_MANAGEMENT_LOCATION_REF_DATA, expectedCml);
        verify(asylumCase, times(1)).write(SELECTED_HEARING_CENTRE_REF_DATA, HARMONDSWORTH_HEARING_CENTRE);
    }

    @Test
    void should_not_set_ref_data_fields_if_ref_data_not_enabled_with_detention_post_code() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(SUBMIT_APPEAL);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.empty());
        when(asylumCase.read(IS_CASE_USING_LOCATION_REF_DATA, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(DETENTION_POSTCODE, String.class)).thenReturn(Optional.of("NW7 1BJ"));
        when(hearingCentreFinder.find("NW7 1BJ")).thenReturn(HearingCentre.HATTON_CROSS);

        deriveHearingCentreHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        verify(asylumCase, never()).write(eq(CASE_MANAGEMENT_LOCATION_REF_DATA), any());
        verify(asylumCase, never()).write(eq(SELECTED_HEARING_CENTRE_REF_DATA), any());
        verify(asylumCase, never()).write(eq(HEARING_CENTRE_DYNAMIC_LIST), any());
    }

    @Test
    void should_not_set_ref_data_fields_if_ref_data_not_enabled_with_blank_detention_post_code() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(SUBMIT_APPEAL);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.empty());
        when(asylumCase.read(IS_CASE_USING_LOCATION_REF_DATA, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(DETENTION_POSTCODE, String.class)).thenReturn(Optional.of(""));
        when(hearingCentreFinder.find("")).thenReturn(HearingCentre.NEWPORT);

        deriveHearingCentreHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        verify(asylumCase, never()).write(eq(CASE_MANAGEMENT_LOCATION_REF_DATA), any());
        verify(asylumCase, never()).write(eq(SELECTED_HEARING_CENTRE_REF_DATA), any());
        verify(asylumCase, never()).write(eq(HEARING_CENTRE_DYNAMIC_LIST), any());
    }

    @Test
    void should_not_set_ref_data_fields_if_ref_data_not_enabled_without_detention_post_code() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(SUBMIT_APPEAL);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.empty());
        when(asylumCase.read(IS_CASE_USING_LOCATION_REF_DATA, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(DETENTION_POSTCODE, String.class)).thenReturn(Optional.empty());
        when(hearingCentreFinder.getDefaultHearingCentre()).thenReturn(HearingCentre.NEWPORT);

        deriveHearingCentreHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        verify(asylumCase, never()).write(eq(CASE_MANAGEMENT_LOCATION_REF_DATA), any());
        verify(asylumCase, never()).write(eq(SELECTED_HEARING_CENTRE_REF_DATA), any());
        verify(asylumCase, never()).write(eq(HEARING_CENTRE_DYNAMIC_LIST), any());
    }
}
