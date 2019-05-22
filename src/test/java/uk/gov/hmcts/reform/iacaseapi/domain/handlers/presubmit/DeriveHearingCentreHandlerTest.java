package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumExtractor.*;

import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.CaseDataMap;
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

    @Mock private Callback<CaseDataMap> callback;
    @Mock private CaseDetails<CaseDataMap> caseDetails;
    @Mock private CaseDataMap caseDataMap;
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
        when(caseDetails.getCaseData()).thenReturn(caseDataMap);
        when(caseDataMap.get(HEARING_CENTRE)).thenReturn(Optional.empty());
        when(caseDataMap.get(APPELLANT_HAS_FIXED_ADDRESS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(caseDataMap.get(APPELLANT_ADDRESS)).thenReturn(Optional.of(addressUk));
        when(addressUk.getPostCode()).thenReturn(Optional.of("A123 4BC"));
        when(hearingCentreFinder.find("A123 4BC")).thenReturn(HearingCentre.MANCHESTER);

        PreSubmitCallbackResponse<CaseDataMap> callbackResponse =
            deriveHearingCentreHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(caseDataMap, callbackResponse.getData());
        verify(hearingCentreFinder, times(1)).find("A123 4BC");
        verify(caseDataMap, times(1)).write(HEARING_CENTRE, HearingCentre.MANCHESTER);
    }

    @Test
    public void should_use_default_hearing_centre_if_appellant_has_no_fixed_address() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(caseDetails.getCaseData()).thenReturn(caseDataMap);
        when(caseDataMap.get(HEARING_CENTRE)).thenReturn(Optional.empty());
        when(caseDataMap.get(APPELLANT_HAS_FIXED_ADDRESS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(hearingCentreFinder.getDefaultHearingCentre()).thenReturn(HearingCentre.TAYLOR_HOUSE);

        PreSubmitCallbackResponse<CaseDataMap> callbackResponse =
            deriveHearingCentreHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(caseDataMap, callbackResponse.getData());
        verify(caseDataMap, times(1)).write(HEARING_CENTRE, HearingCentre.TAYLOR_HOUSE);
    }

    @Test
    public void should_not_set_hearing_centre_if_already_exists() {

        final HearingCentre existingHearingCentre = HearingCentre.MANCHESTER;

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(caseDetails.getCaseData()).thenReturn(caseDataMap);
        when(caseDataMap.get(HEARING_CENTRE)).thenReturn(Optional.of(existingHearingCentre));

        PreSubmitCallbackResponse<CaseDataMap> callbackResponse =
            deriveHearingCentreHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(caseDataMap, callbackResponse.getData());
        verify(caseDataMap, never()).write(any(), any());
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
