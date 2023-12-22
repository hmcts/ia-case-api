package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CHANGE_HEARINGS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CHANGE_HEARING_LOCATION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.MANUAL_UPDATE_HEARING_REQUIRED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.SHOULD_TRIGGER_REVIEW_INTERPRETER_TASK;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.HearingCentre.BRADFORD;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.UPDATE_HEARING_REQUEST;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.MID_EVENT;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Value;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.service.IaHearingsApiService;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class HearingsUpdateHearingRequestTest {

    public static final String NO_HEARINGS_ERROR_MESSAGE =
        "You've made an invalid request. You must request a substantive hearing before you can update a hearing.";

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
    private IaHearingsApiService iaHearingsApiService;
    HearingsUpdateHearingRequest hearingsUpdateHearingRequest;

    @BeforeEach
    public void setUp() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getCaseDetails().getCaseData()).thenReturn(asylumCase);

        hearingsUpdateHearingRequest = new HearingsUpdateHearingRequest(iaHearingsApiService);
    }

    @Test
    public void should_delegate_to_hearings_api_start_event_when_update_hearings_is_null() {
        when(callback.getEvent()).thenReturn(UPDATE_HEARING_REQUEST);
        when(iaHearingsApiService.aboutToStart(callback)).thenReturn(asylumCase);

        DynamicList adjournmentDetailsHearing =
            new DynamicList(
                new Value("code", "adjournmentDetailsHearing"),
                Arrays.asList(new Value("code", "hearing1")));
        when(asylumCase.read(CHANGE_HEARINGS, DynamicList.class))
            .thenReturn(Optional.of(adjournmentDetailsHearing));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                hearingsUpdateHearingRequest.handle(ABOUT_TO_START, callback);

        assertNotNull(callbackResponse);
        verify(iaHearingsApiService, times(1)).aboutToStart(callback);
        assertEquals(asylumCase, callbackResponse.getData());
    }

    @Test
    public void should_delegate_to_hearings_api_mid_event_when_update_hearings_is_not_null() {
        when(callback.getEvent()).thenReturn(UPDATE_HEARING_REQUEST);
        when(asylumCase.read(CHANGE_HEARINGS))
                .thenReturn(Optional.of(new DynamicList("hearing 1")));

        when(iaHearingsApiService.midEvent(callback)).thenReturn(asylumCase);

        when(asylumCase.read(CHANGE_HEARING_LOCATION))
                .thenReturn(Optional.empty());

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                hearingsUpdateHearingRequest.handle(MID_EVENT, callback);

        assertNotNull(callbackResponse);
        verify(iaHearingsApiService, times(1)).midEvent(callback);

        assertEquals(asylumCase, callbackResponse.getData());
    }

    @Test
    public void should_set_hearing_location_when_update_hearings_is_not_null() {
        when(callback.getEvent()).thenReturn(UPDATE_HEARING_REQUEST);
        when(asylumCase.read(CHANGE_HEARINGS))
                .thenReturn(Optional.of(new DynamicList("hearing 1")));
        when(iaHearingsApiService.midEvent(callback)).thenReturn(asylumCase);

        when(asylumCase.read(CHANGE_HEARING_LOCATION))
                .thenReturn(Optional.of(BRADFORD.getEpimsId()));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                hearingsUpdateHearingRequest.handle(MID_EVENT, callback);

        assertNotNull(callbackResponse);
        verify(iaHearingsApiService, times(1)).midEvent(callback);

        verify(asylumCase).write(CHANGE_HEARING_LOCATION, BRADFORD.getValue());
        verify(asylumCase).clear(MANUAL_UPDATE_HEARING_REQUIRED);
        verify(asylumCase).clear(SHOULD_TRIGGER_REVIEW_INTERPRETER_TASK);
    }

    @Test
    void should_throw_error_if_no_hearings() {
        when(callback.getEvent()).thenReturn(UPDATE_HEARING_REQUEST);
        when(iaHearingsApiService.aboutToStart(callback)).thenReturn(asylumCase);

        DynamicList adjournmentDetailsHearing =
            new DynamicList(new Value("code", "adjournmentDetailsHearing"), Collections.emptyList());
        when(asylumCase.read(CHANGE_HEARINGS, DynamicList.class))
            .thenReturn(Optional.of(adjournmentDetailsHearing));

        assertEquals(NO_HEARINGS_ERROR_MESSAGE,
            hearingsUpdateHearingRequest.handle(ABOUT_TO_START, callback).getErrors().iterator().next());
    }
}

