package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_ADDRESS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_HAS_FIXED_ADDRESS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPLICATION_CHANGE_DESIGNATED_HEARING_CENTRE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CASE_MANAGEMENT_LOCATION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HAS_SPONSOR;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HEARING_CENTRE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.LEGAL_REP_COMPANY_ADDRESS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.SPONSOR_ADDRESS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.STAFF_LOCATION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WA_DUMMY_POSTCODE;

import java.util.Arrays;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.BaseLocation;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.CaseManagementLocation;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Region;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.AddressUk;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.service.HearingCentreFinder;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.utils.StaffLocation;

@SuppressWarnings("unchecked")
@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class DeriveHearingCentreHandlerTest {

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

    private DeriveHearingCentreHandler deriveHearingCentreHandler;

    @BeforeEach
    public void setUp() {
        deriveHearingCentreHandler =
            new DeriveHearingCentreHandler(hearingCentreFinder, caseManagementLocationService);
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
        when(asylumCase.read(APPELLANT_HAS_FIXED_ADDRESS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
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
        "SUBMIT_APPEAL, MANCHESTER, Manchester, MANCHESTER",
        "EDIT_APPEAL_AFTER_SUBMIT, MANCHESTER, Manchester, MANCHESTER",

        "SUBMIT_APPEAL, TAYLOR_HOUSE, Taylor House, TAYLOR_HOUSE",
        "EDIT_APPEAL_AFTER_SUBMIT, TAYLOR_HOUSE, Taylor House, TAYLOR_HOUSE",

        "SUBMIT_APPEAL, HATTON_CROSS, Hatton Cross, HATTON_CROSS",
        "EDIT_APPEAL_AFTER_SUBMIT, HATTON_CROSS, Hatton Cross, HATTON_CROSS",

        "SUBMIT_APPEAL, NEWCASTLE, Newcastle,",
        "EDIT_APPEAL_AFTER_SUBMIT, NEWCASTLE, Newcastle,"
    })
    void should_derive_hearing_centre_from_legal_rep_company_postcode(
        Event event, HearingCentre hearingCentre, String staffLocation, BaseLocation baseLocation) {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(event);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(HEARING_CENTRE)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_HAS_FIXED_ADDRESS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(HAS_SPONSOR, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(LEGAL_REP_COMPANY_ADDRESS, AddressUk.class)).thenReturn(Optional.of(
            sponsorOrCompanyAddressUk));
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
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
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
                    Event.SUBMIT_APPEAL,
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
}
