package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CHANGE_HEARINGS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CHANGE_HEARING_DATE_RANGE_EARLIEST;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CHANGE_HEARING_DATE_RANGE_LATEST;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CHANGE_HEARING_DATE_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CHANGE_HEARING_DATE_YES_NO;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CHANGE_HEARING_VENUE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.UPDATE_HEARING_REQUEST;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.MID_EVENT;

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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.service.IaHearingsApiService;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class HearingsUpdateHearingRequestMidEventHandlerTest {

    private static final String COMPLIANT_DATE_RANGE_NEEDED = "Earliest hearing date or Latest hearing date required";
    private static final String UPDATE_HEARING_DATE_PAGE_ID = "updateHearingDate";
    private static final String UPDATE_HEARING_LIST_PAGE_ID = "updateHearingList";

    @Mock
    private Callback<AsylumCase> callback;

    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private IaHearingsApiService iaHearingsApiService;
    HearingsUpdateHearingRequestMidEventHandler handler;

    @BeforeEach
    public void setUp() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getCaseDetails().getCaseData()).thenReturn(asylumCase);

        handler = new HearingsUpdateHearingRequestMidEventHandler(iaHearingsApiService);
    }

    @Test
    public void should_delegate_to_hearings_api_mid_event_when_update_hearings_is_not_null() {
        when(callback.getEvent()).thenReturn(UPDATE_HEARING_REQUEST);
        when(callback.getPageId()).thenReturn(UPDATE_HEARING_LIST_PAGE_ID);
        when(asylumCase.read(CHANGE_HEARINGS))
            .thenReturn(Optional.of(new DynamicList("hearing 1")));
        when(iaHearingsApiService.midEvent(callback)).thenReturn(asylumCase);
        when(asylumCase.read(CHANGE_HEARING_VENUE))
                .thenReturn(Optional.empty());

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                handler.handle(MID_EVENT, callback);

        assertNotNull(callbackResponse);
        verify(iaHearingsApiService, times(1)).midEvent(callback);
        assertEquals(asylumCase, callbackResponse.getData());
        assertTrue(callbackResponse.getErrors().isEmpty());
    }

    @Test
    public void should_add_error_when_date_range_needed_and_not_compliant() {
        when(callback.getEvent()).thenReturn(UPDATE_HEARING_REQUEST);
        when(callback.getPageId()).thenReturn(UPDATE_HEARING_DATE_PAGE_ID);

        when(asylumCase.read(CHANGE_HEARING_DATE_YES_NO, String.class)).thenReturn(Optional.of("yes"));
        when(asylumCase.read(CHANGE_HEARING_DATE_TYPE, String.class)).thenReturn(Optional.of("ChooseADateRange"));
        when(asylumCase.read(CHANGE_HEARING_DATE_RANGE_EARLIEST, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(CHANGE_HEARING_DATE_RANGE_LATEST, String.class)).thenReturn(Optional.empty());

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            handler.handle(MID_EVENT, callback);

        assertNotNull(callbackResponse);
        assertEquals(1, callbackResponse.getErrors().size());
        assertTrue(callbackResponse.getErrors().contains(COMPLIANT_DATE_RANGE_NEEDED));
    }

    @Test
    public void should_not_add_error_when_date_range_needed_and_compliant() {
        when(callback.getEvent()).thenReturn(UPDATE_HEARING_REQUEST);
        when(callback.getPageId()).thenReturn(UPDATE_HEARING_DATE_PAGE_ID);

        when(asylumCase.read(CHANGE_HEARING_DATE_YES_NO, String.class)).thenReturn(Optional.of("yes"));
        when(asylumCase.read(CHANGE_HEARING_DATE_TYPE, String.class)).thenReturn(Optional.of("ChooseADateRange"));
        when(asylumCase.read(CHANGE_HEARING_DATE_RANGE_EARLIEST, String.class)).thenReturn(Optional.of("2024-02-23"));
        when(asylumCase.read(CHANGE_HEARING_DATE_RANGE_LATEST, String.class)).thenReturn(Optional.empty());

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            handler.handle(MID_EVENT, callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getErrors().isEmpty());
    }

    @Test
    public void should_not_add_error_when_date_range_not_needed() {
        when(callback.getEvent()).thenReturn(UPDATE_HEARING_REQUEST);
        when(callback.getPageId()).thenReturn(UPDATE_HEARING_DATE_PAGE_ID);

        when(asylumCase.read(CHANGE_HEARING_DATE_YES_NO, String.class)).thenReturn(Optional.of("no"));
        when(asylumCase.read(CHANGE_HEARING_DATE_TYPE, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(CHANGE_HEARING_DATE_RANGE_EARLIEST, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(CHANGE_HEARING_DATE_RANGE_LATEST, String.class)).thenReturn(Optional.empty());

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            handler.handle(MID_EVENT, callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getErrors().isEmpty());
    }
}

