package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CURRENT_ADJOURNMENT_DETAIL;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.NEXT_HEARING_DURATION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.PREVIOUS_ADJOURNMENT_DETAILS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.PREVIOUS_HEARINGS;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AdjournmentDetail;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.PreviousHearing;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.HoursMinutes;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class ConvertListingLengthHandlerTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;

    private final ConvertListingLengthHandler handler = new ConvertListingLengthHandler();

    @Test
    void should_have_earliest_dispatch_priority() {
        assertEquals(DispatchPriority.EARLIEST, handler.getDispatchPriority());
    }

    @Test
    void should_convert_next_hearing_duration() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        HoursMinutes duration = new HoursMinutes(6, 0);
        when(asylumCase.read(NEXT_HEARING_DURATION, HoursMinutes.class)).thenReturn(Optional.of(duration));
        when(asylumCase.read(CURRENT_ADJOURNMENT_DETAIL, AdjournmentDetail.class)).thenReturn(Optional.empty());
        when(asylumCase.read(PREVIOUS_ADJOURNMENT_DETAILS)).thenReturn(Optional.empty());
        when(asylumCase.read(PREVIOUS_HEARINGS)).thenReturn(Optional.empty());

        PreSubmitCallbackResponse<AsylumCase> response =
            handler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertNotNull(response);
        assertEquals(asylumCase, response.getData());
        verify(asylumCase).write(NEXT_HEARING_DURATION, duration);
    }

    @Test
    void should_convert_current_adjournment_detail() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        AdjournmentDetail detail = AdjournmentDetail.builder()
            .nextHearingDuration(new HoursMinutes(4, 30))
            .build();
        when(asylumCase.read(NEXT_HEARING_DURATION, HoursMinutes.class)).thenReturn(Optional.empty());
        when(asylumCase.read(CURRENT_ADJOURNMENT_DETAIL, AdjournmentDetail.class)).thenReturn(Optional.of(detail));
        when(asylumCase.read(PREVIOUS_ADJOURNMENT_DETAILS)).thenReturn(Optional.empty());
        when(asylumCase.read(PREVIOUS_HEARINGS)).thenReturn(Optional.empty());

        PreSubmitCallbackResponse<AsylumCase> response =
            handler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertNotNull(response);
        verify(asylumCase).write(CURRENT_ADJOURNMENT_DETAIL, detail);
    }

    @Test
    void should_convert_previous_adjournment_details() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        AdjournmentDetail detail = AdjournmentDetail.builder()
            .nextHearingDuration(new HoursMinutes(3, 0))
            .build();
        List<IdValue<AdjournmentDetail>> details = Collections.singletonList(new IdValue<>("1", detail));

        when(asylumCase.read(NEXT_HEARING_DURATION, HoursMinutes.class)).thenReturn(Optional.empty());
        when(asylumCase.read(CURRENT_ADJOURNMENT_DETAIL, AdjournmentDetail.class)).thenReturn(Optional.empty());
        when(asylumCase.read(PREVIOUS_ADJOURNMENT_DETAILS)).thenReturn(Optional.of(details));
        when(asylumCase.read(PREVIOUS_HEARINGS)).thenReturn(Optional.empty());

        PreSubmitCallbackResponse<AsylumCase> response =
            handler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertNotNull(response);
        verify(asylumCase).write(PREVIOUS_ADJOURNMENT_DETAILS, details);
    }

    @Test
    void should_convert_previous_hearings() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        PreviousHearing hearing = new PreviousHearing(
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            "ariaRef",
            null,
            "2024-01-01",
            Optional.of(new HoursMinutes(2, 0)),
            "allowed",
            Collections.emptyList()
        );
        List<IdValue<PreviousHearing>> hearings = Collections.singletonList(new IdValue<>("1", hearing));

        when(asylumCase.read(NEXT_HEARING_DURATION, HoursMinutes.class)).thenReturn(Optional.empty());
        when(asylumCase.read(CURRENT_ADJOURNMENT_DETAIL, AdjournmentDetail.class)).thenReturn(Optional.empty());
        when(asylumCase.read(PREVIOUS_ADJOURNMENT_DETAILS)).thenReturn(Optional.empty());
        when(asylumCase.read(PREVIOUS_HEARINGS)).thenReturn(Optional.of(hearings));

        PreSubmitCallbackResponse<AsylumCase> response =
            handler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertNotNull(response);
        verify(asylumCase).write(PREVIOUS_HEARINGS, hearings);
    }

    @Test
    void should_handle_when_no_fields_present() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(asylumCase.read(NEXT_HEARING_DURATION, HoursMinutes.class)).thenReturn(Optional.empty());
        when(asylumCase.read(CURRENT_ADJOURNMENT_DETAIL, AdjournmentDetail.class)).thenReturn(Optional.empty());
        when(asylumCase.read(PREVIOUS_ADJOURNMENT_DETAILS)).thenReturn(Optional.empty());
        when(asylumCase.read(PREVIOUS_HEARINGS)).thenReturn(Optional.empty());

        PreSubmitCallbackResponse<AsylumCase> response =
            handler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertNotNull(response);
        assertEquals(asylumCase, response.getData());
    }

    @Test
    void should_handle_about_to_submit_stage() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        HoursMinutes duration = new HoursMinutes(6, 0);
        when(asylumCase.read(NEXT_HEARING_DURATION, HoursMinutes.class)).thenReturn(Optional.of(duration));
        when(asylumCase.read(CURRENT_ADJOURNMENT_DETAIL, AdjournmentDetail.class)).thenReturn(Optional.empty());
        when(asylumCase.read(PREVIOUS_ADJOURNMENT_DETAILS)).thenReturn(Optional.empty());
        when(asylumCase.read(PREVIOUS_HEARINGS)).thenReturn(Optional.empty());

        PreSubmitCallbackResponse<AsylumCase> response =
            handler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(response);
        assertEquals(asylumCase, response.getData());
        verify(asylumCase).write(NEXT_HEARING_DURATION, duration);
    }

    @Test
    void should_handle_mid_event_stage() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        HoursMinutes duration = new HoursMinutes(6, 0);
        when(asylumCase.read(NEXT_HEARING_DURATION, HoursMinutes.class)).thenReturn(Optional.of(duration));
        when(asylumCase.read(CURRENT_ADJOURNMENT_DETAIL, AdjournmentDetail.class)).thenReturn(Optional.empty());
        when(asylumCase.read(PREVIOUS_ADJOURNMENT_DETAILS)).thenReturn(Optional.empty());
        when(asylumCase.read(PREVIOUS_HEARINGS)).thenReturn(Optional.empty());

        PreSubmitCallbackResponse<AsylumCase> response =
            handler.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertNotNull(response);
        assertEquals(asylumCase, response.getData());
        verify(asylumCase).write(NEXT_HEARING_DURATION, duration);
    }

    @Test
    void it_can_handle_callback_for_all_events_and_stages() {
        for (Event event : Event.values()) {
            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {
                boolean canHandle = handler.canHandle(callbackStage, callback);
                assertTrue(canHandle);
            }
            reset(callback);
        }
    }

    @Test
    void should_not_allow_null_arguments() {
        assertThatThrownBy(() -> handler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> handler.canHandle(PreSubmitCallbackStage.ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}