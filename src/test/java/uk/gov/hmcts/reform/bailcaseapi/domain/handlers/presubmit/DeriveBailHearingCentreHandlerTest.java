package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.DispatchPriority.LATEST;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;

import java.util.List;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Value;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggleService;
import uk.gov.hmcts.reform.iacaseapi.domain.service.HearingCentreFinder;
import uk.gov.hmcts.reform.iacaseapi.domain.service.LocationRefDataService;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class DeriveBailHearingCentreHandlerTest {

    @Mock
    private Callback<BailCase> callback;
    @Mock
    private CaseDetails<BailCase> caseDetails;
    @Mock
    private BailCase bailCase;
    @Mock
    private HearingCentreFinder hearingCentreFinder;
    @Mock
    private LocationRefDataService locationRefDataService;
    @Mock
    private FeatureToggleService featureToggleService;
    @Captor
    private ArgumentCaptor<DynamicList> dynamicListArgumentCaptor;

    private Value hattonCross = new Value("386417", "Hatton Cross Tribunal Hearing Centre");
    private Value newCastle = new Value("366796", "Newcastle");
    private uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.DeriveBailHearingCentreHandler deriveBailHearingCentreHandler;

    @BeforeEach
    public void setUp() {
        deriveBailHearingCentreHandler = new uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.DeriveBailHearingCentreHandler(
            hearingCentreFinder, locationRefDataService, featureToggleService);

        DynamicList locationRefDataDynamicList = new DynamicList(
            new Value("", ""), List.of(hattonCross, newCastle));

        when(locationRefDataService.getCaseManagementLocationsDynamicList())
            .thenReturn(locationRefDataDynamicList);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.START_APPLICATION);
        when(caseDetails.getCaseData()).thenReturn(bailCase);
        when(featureToggleService.locationRefDataEnabled()).thenReturn(false);
    }

    @Test
    void set_to_latest() {
        assertThat(deriveBailHearingCentreHandler.getDispatchPriority()).isEqualTo(LATEST);
    }

    @Test
    void should_derive_hearing_centre_from_detention_facility_name_from_prison() {

        when(bailCase.read(PRISON_NAME, String.class)).thenReturn(Optional.of("Garth"));
        when(bailCase.read(IRC_NAME, String.class)).thenReturn(Optional.empty());
        when(hearingCentreFinder.find("Garth")).thenReturn(HearingCentre.MANCHESTER);

        PreSubmitCallbackResponse<BailCase> callbackResponse =
            deriveBailHearingCentreHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertThat(callbackResponse.getData()).isNotEmpty();
        assertThat(callbackResponse.getData()).isEqualTo(bailCase);

        verify(hearingCentreFinder, times(1)).find("Garth");
        verify(bailCase, times(1)).write(HEARING_CENTRE, HearingCentre.MANCHESTER);
        verify(bailCase, times(1)).write(DESIGNATED_TRIBUNAL_CENTRE, HearingCentre.MANCHESTER);
    }

    @Test
    void should_derive_hearing_centre_from_detention_facility_name_from_irc() {

        when(bailCase.read(PRISON_NAME, String.class)).thenReturn(Optional.empty());
        when(bailCase.read(IRC_NAME, String.class)).thenReturn(Optional.of("Harmondsworth"));
        when(hearingCentreFinder.find("Harmondsworth")).thenReturn(HearingCentre.HATTON_CROSS);

        PreSubmitCallbackResponse<BailCase> callbackResponse =
            deriveBailHearingCentreHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertThat(callbackResponse.getData()).isNotEmpty();
        assertThat(callbackResponse.getData()).isEqualTo(bailCase);

        verify(hearingCentreFinder, times(1)).find("Harmondsworth");
        verify(bailCase, times(1)).write(HEARING_CENTRE, HearingCentre.HATTON_CROSS);
        verify(bailCase, times(1)).write(DESIGNATED_TRIBUNAL_CENTRE, HearingCentre.HATTON_CROSS);
    }

    @Test
    void should_set_hearing_centre_if_already_assigned_value() {
        final HearingCentre existingHearingCentre = HearingCentre.TAYLOR_HOUSE;

        when(bailCase.read(PRISON_NAME, String.class)).thenReturn(Optional.empty());
        when(bailCase.read(IRC_NAME, String.class)).thenReturn(Optional.of("Harmondsworth"));
        when(bailCase.read(HEARING_CENTRE)).thenReturn(Optional.of(existingHearingCentre));
        when(hearingCentreFinder.find("Harmondsworth")).thenReturn(HearingCentre.HATTON_CROSS);

        PreSubmitCallbackResponse<BailCase> callbackResponse =
            deriveBailHearingCentreHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertThat(callbackResponse.getData()).isNotEmpty();
        verify(bailCase, times(1)).write(HEARING_CENTRE, HearingCentre.HATTON_CROSS);
        verify(bailCase, times(1)).write(DESIGNATED_TRIBUNAL_CENTRE, HearingCentre.HATTON_CROSS);
    }

    @Test
    void should_set_hearing_centre_ref_data() {

        when(bailCase.read(PRISON_NAME, String.class)).thenReturn(Optional.empty());
        when(bailCase.read(IRC_NAME, String.class)).thenReturn(Optional.of("Harmondsworth"));
        when(hearingCentreFinder.find("Harmondsworth")).thenReturn(HearingCentre.HATTON_CROSS);
        when(featureToggleService.locationRefDataEnabled()).thenReturn(true);

        PreSubmitCallbackResponse<BailCase> callbackResponse =
            deriveBailHearingCentreHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);

        verify(bailCase, times(1)).write(IS_BAILS_LOCATION_REFERENCE_DATA_ENABLED, YES);
        verify(bailCase, times(1))
            .write(eq(HEARING_CENTRE_REF_DATA), dynamicListArgumentCaptor.capture());
        verify(bailCase, times(1)).write(SELECTED_HEARING_CENTRE_REF_DATA, hattonCross.getLabel());

        assertEquals(hattonCross, dynamicListArgumentCaptor.getValue().getValue());
    }

    @Test
    void should_not_set_hearing_centre_ref_data_when_location_ref_data_not_enabled() {

        when(bailCase.read(PRISON_NAME, String.class)).thenReturn(Optional.empty());
        when(bailCase.read(IRC_NAME, String.class)).thenReturn(Optional.of("Harmondsworth"));
        when(hearingCentreFinder.find("Harmondsworth")).thenReturn(HearingCentre.HATTON_CROSS);

        PreSubmitCallbackResponse<BailCase> callbackResponse =
            deriveBailHearingCentreHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);

        verify(bailCase, never())
            .write(eq(HEARING_CENTRE_REF_DATA), any());
        verify(bailCase, never())
            .write(eq(SELECTED_HEARING_CENTRE_REF_DATA), any());
    }

    @Test
    void should_not_set_hearing_centre_ref_data_when_selected_hearing_centre_not_in_location_reference_data() {

        when(bailCase.read(PRISON_NAME, String.class)).thenReturn(Optional.empty());
        when(bailCase.read(IRC_NAME, String.class)).thenReturn(Optional.of("Harmondsworth"));
        when(hearingCentreFinder.find("Harmondsworth")).thenReturn(HearingCentre.HATTON_CROSS);
        DynamicList locationRefDataDynamicList = new DynamicList(
            new Value("", ""), List.of(newCastle));

        PreSubmitCallbackResponse<BailCase> callbackResponse =
            deriveBailHearingCentreHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);

        verify(bailCase, never())
            .write(eq(HEARING_CENTRE_REF_DATA), any());
    }

    @Test
    void should_set_location_reference_data_flag_to_no() {

        when(bailCase.read(PRISON_NAME, String.class)).thenReturn(Optional.empty());
        when(bailCase.read(IRC_NAME, String.class)).thenReturn(Optional.of("Harmondsworth"));
        when(hearingCentreFinder.find("Harmondsworth")).thenReturn(HearingCentre.HATTON_CROSS);
        when(featureToggleService.locationRefDataEnabled()).thenReturn(false);

        PreSubmitCallbackResponse<BailCase> callbackResponse =
            deriveBailHearingCentreHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);

        verify(bailCase, times(1)).write(IS_BAILS_LOCATION_REFERENCE_DATA_ENABLED, NO);
    }

    @Test
    void should_throw_for_empty_prison_and_irc() {

        when(bailCase.read(PRISON_NAME, String.class)).thenReturn(Optional.empty());
        when(bailCase.read(IRC_NAME, String.class)).thenReturn(Optional.empty());

        Assertions.assertThatThrownBy(
            () -> deriveBailHearingCentreHandler.setHearingCentreFromDetentionFacilityName(bailCase))
            .hasMessage("Prison name and IRC name missing")
            .isExactlyInstanceOf(RequiredFieldMissingException.class);

    }

    @Test
    void handler_can_handle_callback() {
        for (Event event : Event.values()) {
            when(callback.getEvent()).thenReturn(event);
            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {
                boolean canHandle = deriveBailHearingCentreHandler.canHandle(callbackStage, callback);
                if (callbackStage == ABOUT_TO_SUBMIT
                    && (callback.getEvent() == Event.START_APPLICATION
                        || callback.getEvent() == Event.EDIT_BAIL_APPLICATION
                        || callback.getEvent() == Event.MAKE_NEW_APPLICATION
                        || callback.getEvent() == Event.EDIT_BAIL_APPLICATION_AFTER_SUBMIT)) {
                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }
        }
    }

    @Test
    void handler_throws_error_if_cannot_actually_handle() {
        //invalid stage
        Assertions.assertThatThrownBy(() -> deriveBailHearingCentreHandler.handle(ABOUT_TO_START, callback)).hasMessage(
            "Cannot handle callback").isExactlyInstanceOf(IllegalStateException.class);

        //invalid event
        when(callback.getEvent()).thenReturn(Event.END_APPLICATION);
        Assertions.assertThatThrownBy(() -> deriveBailHearingCentreHandler.handle(ABOUT_TO_SUBMIT, callback)).hasMessage(
            "Cannot handle callback").isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_not_allow_null_args() {
        assertThatThrownBy(() -> deriveBailHearingCentreHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> deriveBailHearingCentreHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> deriveBailHearingCentreHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> deriveBailHearingCentreHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
