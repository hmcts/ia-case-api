package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.util.Arrays;
import java.util.Optional;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import junitparams.converters.Nullable;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.AddressUk;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.service.HearingCentreFinder;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.utils.StaffLocation;

@SuppressWarnings("unchecked")
@RunWith(JUnitParamsRunner.class)
public class DeriveHearingCentreHandlerTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule().strictness(Strictness.LENIENT);

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private AddressUk addressUk;

    @Mock
    private HearingCentreFinder hearingCentreFinder;
    @Mock
    private CaseManagementLocationService caseManagementLocationService;

    private DeriveHearingCentreHandler deriveHearingCentreHandler;

    @Before
    public void setUp() {
        deriveHearingCentreHandler =
            new DeriveHearingCentreHandler(hearingCentreFinder, caseManagementLocationService);
    }

    @Test
    @Parameters({
        "SUBMIT_APPEAL, MANCHESTER, Manchester, MANCHESTER",
        "EDIT_APPEAL_AFTER_SUBMIT, MANCHESTER, Manchester, MANCHESTER",
        "PAY_AND_SUBMIT_APPEAL, MANCHESTER, Manchester, MANCHESTER",

        "SUBMIT_APPEAL, TAYLOR_HOUSE, Taylor House, TAYLOR_HOUSE",
        "EDIT_APPEAL_AFTER_SUBMIT, TAYLOR_HOUSE, Taylor House, TAYLOR_HOUSE",
        "PAY_AND_SUBMIT_APPEAL, TAYLOR_HOUSE, Taylor House, TAYLOR_HOUSE",

        "SUBMIT_APPEAL, HATTON_CROSS, Hatton Cross, HATTON_CROSS",
        "EDIT_APPEAL_AFTER_SUBMIT, HATTON_CROSS, Hatton Cross, HATTON_CROSS",
        "PAY_AND_SUBMIT_APPEAL, HATTON_CROSS, Hatton Cross, HATTON_CROSS",

        "SUBMIT_APPEAL, NEWCASTLE, Newcastle, null",
        "EDIT_APPEAL_AFTER_SUBMIT, NEWCASTLE, Newcastle, null",
        "PAY_AND_SUBMIT_APPEAL, NEWCASTLE, Newcastle, null"
    })
    public void should_derive_hearing_centre_from_appellant_postcode(
        Event event, HearingCentre hearingCentre, String staffLocation, @Nullable BaseLocation baseLocation) {

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

    @Test
    @Parameters({
        "SUBMIT_APPEAL, MANCHESTER, Manchester, MANCHESTER",
        "EDIT_APPEAL_AFTER_SUBMIT, MANCHESTER, Manchester, MANCHESTER",
        "PAY_AND_SUBMIT_APPEAL, MANCHESTER, Manchester, MANCHESTER",

        "SUBMIT_APPEAL, TAYLOR_HOUSE, Taylor House, TAYLOR_HOUSE",
        "EDIT_APPEAL_AFTER_SUBMIT, TAYLOR_HOUSE, Taylor House, TAYLOR_HOUSE",
        "PAY_AND_SUBMIT_APPEAL, TAYLOR_HOUSE, Taylor House, TAYLOR_HOUSE",

        "SUBMIT_APPEAL, HATTON_CROSS, Hatton Cross, HATTON_CROSS",
        "EDIT_APPEAL_AFTER_SUBMIT, HATTON_CROSS, Hatton Cross, HATTON_CROSS",
        "PAY_AND_SUBMIT_APPEAL, HATTON_CROSS, Hatton Cross, HATTON_CROSS"
    })
    public void should_use_default_hearing_centre_if_appellant_has_no_fixed_address(
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

    @Test
    public void should_be_handled_at_earliest_point() {
        assertEquals(DispatchPriority.EARLIEST, deriveHearingCentreHandler.getDispatchPriority());
    }

    @Test
    public void should_not_set_hearing_centre_if_already_exists() {

        final HearingCentre existingHearingCentre = HearingCentre.MANCHESTER;

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(HEARING_CENTRE)).thenReturn(Optional.of(existingHearingCentre));
        when(asylumCase.read(STAFF_LOCATION)).thenReturn(Optional.of(StaffLocation.getLocation(existingHearingCentre).getName()));


        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            deriveHearingCentreHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());
        verify(asylumCase, never()).write(any(), any());
        verify(asylumCase, never()).write(STAFF_LOCATION, StaffLocation.getLocation(existingHearingCentre).getName());
    }

    @Test
    public void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> deriveHearingCentreHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.SEND_DIRECTION);
        assertThatThrownBy(() -> deriveHearingCentreHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = deriveHearingCentreHandler.canHandle(callbackStage, callback);

                if (Arrays.asList(
                    Event.SUBMIT_APPEAL,
                    Event.EDIT_APPEAL_AFTER_SUBMIT,
                    Event.PAY_AND_SUBMIT_APPEAL)
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
    public void should_not_allow_null_arguments() {

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
