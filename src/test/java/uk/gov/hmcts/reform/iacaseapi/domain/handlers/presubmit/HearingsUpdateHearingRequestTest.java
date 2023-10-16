package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.AsylumCaseCallbackApiDelegator;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CHANGE_HEARINGS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CHANGE_HEARING_LOCATION_VALUE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.HearingCentre.BRADFORD;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.UPDATE_HEARING_REQUEST;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.MID_EVENT;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class HearingsUpdateHearingRequestTest {
    @Mock
    private Callback<AsylumCase> callback;

    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    private final String hearingsApiEndpoint = "hearings-end-point";
    private final String aboutToStartPath = "/about-to-start";
    private final String midEventPath = "/mid-event";
    @Mock
    private AsylumCaseCallbackApiDelegator asylumCaseCallbackApiDelegator;
    HearingsUpdateHearingRequest hearingsUpdateHearingRequest;

    @BeforeEach
    public void setUp() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getCaseDetails().getCaseData()).thenReturn(asylumCase);

        hearingsUpdateHearingRequest =
                new HearingsUpdateHearingRequest(
                        asylumCaseCallbackApiDelegator,
                        hearingsApiEndpoint,
                        aboutToStartPath,
                        midEventPath
                );
    }

    @Test
    public void should_delegate_to_hearings_api_start_event_when_update_hearings_is_null() {
        when(callback.getEvent()).thenReturn(UPDATE_HEARING_REQUEST);
        when(asylumCase.read(CHANGE_HEARINGS))
                .thenReturn(Optional.empty());
        when(asylumCaseCallbackApiDelegator.delegate(callback, hearingsApiEndpoint + aboutToStartPath))
                .thenReturn(asylumCase);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                hearingsUpdateHearingRequest.handle(ABOUT_TO_START, callback);

        assertNotNull(callbackResponse);
        verify(asylumCaseCallbackApiDelegator, times(1))
                .delegate(callback, hearingsApiEndpoint + aboutToStartPath);
        assertEquals(asylumCase, callbackResponse.getData());
    }

    @Test
    public void should_delegate_to_hearings_api_mid_event_when_update_hearings_is_not_null() {
        when(callback.getEvent()).thenReturn(UPDATE_HEARING_REQUEST);
        when(asylumCase.read(CHANGE_HEARINGS))
                .thenReturn(Optional.of(new DynamicList("hearing 1")));

        when(asylumCaseCallbackApiDelegator.delegate(callback, hearingsApiEndpoint + midEventPath))
                .thenReturn(asylumCase);
        when(asylumCase.read(CHANGE_HEARING_LOCATION_VALUE))
                .thenReturn(Optional.empty());

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                hearingsUpdateHearingRequest.handle(MID_EVENT, callback);

        assertNotNull(callbackResponse);
        verify(asylumCaseCallbackApiDelegator, times(1))
                .delegate(callback, hearingsApiEndpoint + midEventPath);
        assertEquals(asylumCase, callbackResponse.getData());
    }

    @Test
    public void should_set_hearing_location_when_update_hearings_is_not_null() {
        when(callback.getEvent()).thenReturn(UPDATE_HEARING_REQUEST);
        when(asylumCase.read(CHANGE_HEARINGS))
                .thenReturn(Optional.of(new DynamicList("hearing 1")));
        when(asylumCaseCallbackApiDelegator.delegate(callback, hearingsApiEndpoint + midEventPath))
                .thenReturn(asylumCase);
        when(asylumCase.read(CHANGE_HEARING_LOCATION_VALUE))
                .thenReturn(Optional.of(BRADFORD.getEpimsId()));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                hearingsUpdateHearingRequest.handle(MID_EVENT, callback);

        assertNotNull(callbackResponse);
        verify(asylumCaseCallbackApiDelegator, times(1))
                .delegate(callback, hearingsApiEndpoint + midEventPath);
        verify(asylumCase).write(CHANGE_HEARING_LOCATION_VALUE, BRADFORD.getValue());
    }
}

