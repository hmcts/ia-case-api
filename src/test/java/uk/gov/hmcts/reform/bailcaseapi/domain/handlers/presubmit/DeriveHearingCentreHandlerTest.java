package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.HEARING_CENTRE;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.IRC_NAME;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.PRISON_NAME;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.DispatchPriority.LATEST;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;

import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.bailcaseapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.bailcaseapi.domain.service.HearingCentreFinder;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class DeriveHearingCentreHandlerTest {

    @Mock
    private Callback<BailCase> callback;
    @Mock
    private CaseDetails<BailCase> caseDetails;
    @Mock
    private BailCase bailCase;

    @Mock
    private HearingCentreFinder hearingCentreFinder;

    private DeriveHearingCentreHandler deriveHearingCentreHandler;

    @BeforeEach
    public void setUp() {
        deriveHearingCentreHandler = new DeriveHearingCentreHandler(hearingCentreFinder);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.START_APPLICATION);
        when(caseDetails.getCaseData()).thenReturn(bailCase);
    }

    @Test
    void set_to_latest() {
        assertThat(deriveHearingCentreHandler.getDispatchPriority()).isEqualTo(LATEST);
    }

    @Test
    void should_derive_hearing_centre_from_detention_facility_name_from_prison() {

        when(bailCase.read(PRISON_NAME, String.class)).thenReturn(Optional.of("Garth"));
        when(bailCase.read(IRC_NAME, String.class)).thenReturn(Optional.empty());
        when(hearingCentreFinder.find("Garth")).thenReturn(HearingCentre.MANCHESTER);

        PreSubmitCallbackResponse<BailCase> callbackResponse =
            deriveHearingCentreHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertThat(callbackResponse.getData()).isNotEmpty();
        assertThat(callbackResponse.getData()).isEqualTo(bailCase);

        verify(hearingCentreFinder, times(1)).find("Garth");
        verify(bailCase, times(1)).write(HEARING_CENTRE, HearingCentre.MANCHESTER);
    }

    @Test
    void should_derive_hearing_centre_from_detention_facility_name_from_irc() {

        when(bailCase.read(PRISON_NAME, String.class)).thenReturn(Optional.empty());
        when(bailCase.read(IRC_NAME, String.class)).thenReturn(Optional.of("Harmondsworth"));
        when(hearingCentreFinder.find("Harmondsworth")).thenReturn(HearingCentre.HATTON_CROSS);

        PreSubmitCallbackResponse<BailCase> callbackResponse =
            deriveHearingCentreHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertThat(callbackResponse.getData()).isNotEmpty();
        assertThat(callbackResponse.getData()).isEqualTo(bailCase);

        verify(hearingCentreFinder, times(1)).find("Harmondsworth");
        verify(bailCase, times(1)).write(HEARING_CENTRE, HearingCentre.HATTON_CROSS);
    }

    @Test
    void should_set_hearing_centre_if_already_assigned_value() {
        final HearingCentre existingHearingCentre = HearingCentre.TAYLOR_HOUSE;

        when(bailCase.read(PRISON_NAME, String.class)).thenReturn(Optional.empty());
        when(bailCase.read(IRC_NAME, String.class)).thenReturn(Optional.of("Harmondsworth"));
        when(bailCase.read(HEARING_CENTRE)).thenReturn(Optional.of(existingHearingCentre));
        when(hearingCentreFinder.find("Harmondsworth")).thenReturn(HearingCentre.HATTON_CROSS);

        PreSubmitCallbackResponse<BailCase> callbackResponse =
            deriveHearingCentreHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertThat(callbackResponse.getData()).isNotEmpty();
        assertThat(callbackResponse.getData()).isEqualTo(bailCase);
        verify(bailCase, times(1)).write(HEARING_CENTRE, HearingCentre.HATTON_CROSS);
    }

    @Test
    void should_throw_for_empty_prison_and_irc() {

        when(bailCase.read(PRISON_NAME, String.class)).thenReturn(Optional.empty());
        when(bailCase.read(IRC_NAME, String.class)).thenReturn(Optional.empty());

        Assertions.assertThatThrownBy(
            () -> deriveHearingCentreHandler.setHearingCentreFromDetentionFacilityName(bailCase))
            .hasMessage("Prison name and IRC name missing")
            .isExactlyInstanceOf(RequiredFieldMissingException.class);

    }

    @Test
    void handler_can_handle_callback() {
        for (Event event : Event.values()) {
            when(callback.getEvent()).thenReturn(event);
            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {
                boolean canHandle = deriveHearingCentreHandler.canHandle(callbackStage, callback);
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
        Assertions.assertThatThrownBy(() -> deriveHearingCentreHandler.handle(ABOUT_TO_START, callback)).hasMessage(
            "Cannot handle callback").isExactlyInstanceOf(IllegalStateException.class);

        //invalid event
        when(callback.getEvent()).thenReturn(Event.END_APPLICATION);
        Assertions.assertThatThrownBy(() -> deriveHearingCentreHandler.handle(ABOUT_TO_SUBMIT, callback)).hasMessage(
            "Cannot handle callback").isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_not_allow_null_args() {
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
