package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CHANGE_HEARINGS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.MANUAL_UPDATE_HEARING_REQUIRED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.UPDATE_HEARING_REQUEST;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;

import java.util.List;
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
public class HearingsUpdateHearingRequestPreparerTest {

    public static final String NO_HEARINGS_ERROR_MESSAGE =
        "You've made an invalid request. You must request a substantive hearing before you can update a hearing.";

    @Mock
    private Callback<AsylumCase> callback;

    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private IaHearingsApiService iaHearingsApiService;

    private HearingsUpdateHearingRequestPreparer handler;

    @BeforeEach
    public void setUp() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getCaseDetails().getCaseData()).thenReturn(asylumCase);

        handler = new HearingsUpdateHearingRequestPreparer(iaHearingsApiService);
    }

    @Test
    public void should_delegate_to_hearings_api_start_event_when_change_hearings_is_not_set() {
        when(callback.getEvent()).thenReturn(UPDATE_HEARING_REQUEST);
        when(iaHearingsApiService.aboutToStart(callback)).thenReturn(asylumCase);
        when(asylumCase.read(CHANGE_HEARINGS, DynamicList.class))
            .thenReturn(Optional.empty());

        Value value = new Value("code", "hearings1");
        DynamicList changeHearings =
            new DynamicList(value, List.of(value));
        when(asylumCase.read(CHANGE_HEARINGS))
            .thenReturn(Optional.of(changeHearings));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                handler.handle(ABOUT_TO_START, callback);

        assertNotNull(callbackResponse);
        verify(iaHearingsApiService, times(1)).aboutToStart(callback);
        assertEquals(asylumCase, callbackResponse.getData());
        verify(asylumCase).clear(MANUAL_UPDATE_HEARING_REQUIRED);
    }

    @Test
    public void should_delegate_to_hearings_api_start_event_when_change_hearings_is_set() {
        when(callback.getEvent()).thenReturn(UPDATE_HEARING_REQUEST);
        when(iaHearingsApiService.aboutToStart(callback)).thenReturn(asylumCase);

        Value value = new Value("code", "hearings1");
        DynamicList changeHearings =
            new DynamicList(value, List.of(value));
        when(asylumCase.read(CHANGE_HEARINGS, DynamicList.class))
            .thenReturn(Optional.of(changeHearings));
        when(asylumCase.read(CHANGE_HEARINGS))
            .thenReturn(Optional.of(changeHearings));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            handler.handle(ABOUT_TO_START, callback);

        assertNotNull(callbackResponse);
        verify(iaHearingsApiService, times(1)).aboutToStart(callback);
        assertEquals(asylumCase, callbackResponse.getData());
        verify(asylumCase).clear(MANUAL_UPDATE_HEARING_REQUIRED);
    }

    @Test
    void should_throw_error_if_no_hearings() {
        when(callback.getEvent()).thenReturn(UPDATE_HEARING_REQUEST);

        when(iaHearingsApiService.aboutToStart(callback)).thenReturn(asylumCase);
        when(asylumCase.read(CHANGE_HEARINGS, DynamicList.class))
            .thenReturn(Optional.empty());
        when(asylumCase.read(CHANGE_HEARINGS))
            .thenReturn(Optional.empty());

        assertEquals(NO_HEARINGS_ERROR_MESSAGE,
            handler.handle(ABOUT_TO_START, callback).getErrors().iterator().next());
    }
}

