package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.AddressUk;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.service.HearingCentreFinder;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class DeriveHearingCentreHandlerTest {

    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;
    @Mock private AddressUk addressUk;

    @Mock private HearingCentreFinder hearingCentreFinder;

    DeriveHearingCentreHandler deriveHearingCentreHandler;

    @BeforeEach
    void setUp() {

        deriveHearingCentreHandler =
            new DeriveHearingCentreHandler(hearingCentreFinder);
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {
        "SUBMIT_APPEAL",
        "EDIT_APPEAL_AFTER_SUBMIT"
    })
    void should_derive_hearing_centre_from_appellant_postcode(Event event) {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(event);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(HEARING_CENTRE)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_HAS_FIXED_ADDRESS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(APPELLANT_ADDRESS)).thenReturn(Optional.of(addressUk));
        when(addressUk.getPostCode()).thenReturn(Optional.of("A123 4BC"));
        when(hearingCentreFinder.find("A123 4BC")).thenReturn(HearingCentre.MANCHESTER);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            deriveHearingCentreHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());
        verify(hearingCentreFinder, times(1)).find("A123 4BC");
        verify(asylumCase, times(1)).write(HEARING_CENTRE, HearingCentre.MANCHESTER);
        verify(asylumCase, times(1)).write(APPLICATION_CHANGE_DESIGNATED_HEARING_CENTRE, HearingCentre.MANCHESTER);
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {
        "SUBMIT_APPEAL",
        "EDIT_APPEAL_AFTER_SUBMIT"
    })
    void should_use_default_hearing_centre_if_appellant_has_no_fixed_address(Event event) {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(event);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(HEARING_CENTRE)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_HAS_FIXED_ADDRESS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(hearingCentreFinder.getDefaultHearingCentre()).thenReturn(HearingCentre.TAYLOR_HOUSE);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            deriveHearingCentreHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());
        verify(asylumCase, times(1)).write(HEARING_CENTRE, HearingCentre.TAYLOR_HOUSE);
        verify(asylumCase, times(1)).write(APPLICATION_CHANGE_DESIGNATED_HEARING_CENTRE, HearingCentre.TAYLOR_HOUSE);
    }

    @Test
    void should_not_set_hearing_centre_if_already_exists() {

        final HearingCentre existingHearingCentre = HearingCentre.MANCHESTER;

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(HEARING_CENTRE)).thenReturn(Optional.of(existingHearingCentre));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            deriveHearingCentreHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());
        verify(asylumCase, never()).write(any(), any());
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

                if ((event == Event.SUBMIT_APPEAL || event == Event.EDIT_APPEAL_AFTER_SUBMIT)
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
