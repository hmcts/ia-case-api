package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
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

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class DeriveHearingCentreHandlerTest {

    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;
    @Mock private AddressUk addressUk;

    @Mock private HearingCentreFinder hearingCentreFinder;

    private DeriveHearingCentreHandler deriveHearingCentreHandler;

    @Before
    public void setUp() {
        deriveHearingCentreHandler =
            new DeriveHearingCentreHandler(hearingCentreFinder);
    }

    @Test
    public void should_derive_hearing_centre_from_appellant_postcode() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.getHearingCentre()).thenReturn(Optional.empty());
        when(asylumCase.getAppellantHasFixedAddress()).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.getAppellantAddress()).thenReturn(Optional.of(addressUk));
        when(addressUk.getPostCode()).thenReturn(Optional.of("A123 4BC"));
        when(hearingCentreFinder.find("A123 4BC")).thenReturn(HearingCentre.MANCHESTER);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            deriveHearingCentreHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());
        verify(hearingCentreFinder, times(1)).find("A123 4BC");
        verify(asylumCase, times(1)).setHearingCentre(HearingCentre.MANCHESTER);
    }

    @Test
    public void should_use_default_hearing_centre_if_appellant_has_no_fixed_address() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.getHearingCentre()).thenReturn(Optional.empty());
        when(asylumCase.getAppellantHasFixedAddress()).thenReturn(Optional.of(YesOrNo.NO));
        when(hearingCentreFinder.getDefaultHearingCentre()).thenReturn(HearingCentre.TAYLOR_HOUSE);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            deriveHearingCentreHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());
        verify(asylumCase, times(1)).setHearingCentre(HearingCentre.TAYLOR_HOUSE);
    }

    @Test
    public void should_not_set_hearing_centre_if_already_exists() {

        final HearingCentre existingHearingCentre = HearingCentre.MANCHESTER;

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.getHearingCentre()).thenReturn(Optional.of(existingHearingCentre));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            deriveHearingCentreHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());
        verify(asylumCase, never()).setHearingCentre(any());
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

                if ((event == Event.SUBMIT_APPEAL)
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
