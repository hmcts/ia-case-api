package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_INTEGRATED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.LIST_CASE_HEARING_DATE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.NEXT_HEARING_DETAILS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.CMR_LISTING;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.CMR_RE_LISTING;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.EDIT_CASE_LISTING;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.LIST_CASE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.UPDATE_NEXT_HEARING_INFO;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.NextHearingDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.service.NextHearingDateService;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class NextHearingDateHandlerTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private NextHearingDateService nextHearingDateSerice;
    @Captor
    private ArgumentCaptor<NextHearingDetails> captor;

    private NextHearingDateHandler handler;
    private final String listCaseHearingDate = LocalDateTime.now().plusDays(1).toString();


    @BeforeEach
    public void setUp() {

        handler = new NextHearingDateHandler(nextHearingDateSerice);

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {"EDIT_CASE_LISTING", "LIST_CASE", "UPDATE_NEXT_HEARING_INFO", "CMR_LISTING", "CMR_RE_LISTING"})
    public void should_not_set_next_hearing_date_if_feature_not_enabled(Event event) {
        when(callback.getEvent()).thenReturn(event);
        when(nextHearingDateSerice.enabled()).thenReturn(false);
        when(asylumCase.read(IS_INTEGRATED, YesOrNo.class)).thenReturn(Optional.of(YES));

        if (event == UPDATE_NEXT_HEARING_INFO) {
            handler.handle(ABOUT_TO_START, callback);
        } else {
            handler.handle(ABOUT_TO_SUBMIT, callback);
        }

        verify(nextHearingDateSerice, never()).calculateNextHearingDateFromHearings(
                        callback, event == UPDATE_NEXT_HEARING_INFO ? ABOUT_TO_START : ABOUT_TO_SUBMIT);
        verify(nextHearingDateSerice, never()).calculateNextHearingDateFromCaseData(callback);
        verify(asylumCase, never()).write(eq(NEXT_HEARING_DETAILS), any());
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {"EDIT_CASE_LISTING", "LIST_CASE", "UPDATE_NEXT_HEARING_INFO", "CMR_LISTING", "CMR_RE_LISTING"})
    public void should_set_next_hearing_date_from_hearings(Event event) {
        when(callback.getEvent()).thenReturn(event);
        when(nextHearingDateSerice.enabled()).thenReturn(true);
        when(asylumCase.read(IS_INTEGRATED, YesOrNo.class)).thenReturn(Optional.of(YES));

        NextHearingDetails nextHearingDetails = NextHearingDetails.builder()
            .hearingId("hearingId").hearingDateTime("hearingDateTime").build();
        when(asylumCase.read(NEXT_HEARING_DETAILS, NextHearingDetails.class))
            .thenReturn(Optional.of(nextHearingDetails));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            event == UPDATE_NEXT_HEARING_INFO ? handler.handle(ABOUT_TO_START, callback)
                    : handler.handle(ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        verify(nextHearingDateSerice, times(1))
                .calculateNextHearingDateFromHearings(
                        callback, event == UPDATE_NEXT_HEARING_INFO ? ABOUT_TO_START : ABOUT_TO_SUBMIT);
        verify(nextHearingDateSerice, never()).calculateNextHearingDateFromCaseData(callback);
        verify(asylumCase).write(eq(NEXT_HEARING_DETAILS), any());
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {"EDIT_CASE_LISTING", "LIST_CASE", "CMR_LISTING", "CMR_RE_LISTING"})
    public void should_set_next_hearing_date_from_case_data(Event event) {
        when(callback.getEvent()).thenReturn(event);
        when(nextHearingDateSerice.enabled()).thenReturn(true);
        when(asylumCase.read(IS_INTEGRATED, YesOrNo.class)).thenReturn(Optional.of(NO));
        when(asylumCase.read(LIST_CASE_HEARING_DATE, String.class)).thenReturn(Optional.of(listCaseHearingDate));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            handler.handle(ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        verify(nextHearingDateSerice, never()).calculateNextHearingDateFromHearings(callback, ABOUT_TO_SUBMIT);
        verify(nextHearingDateSerice).calculateNextHearingDateFromCaseData(callback);
        verify(asylumCase).write(eq(NEXT_HEARING_DETAILS), any());
    }

    @Test
    public void should_reset_next_hearing_date() {
        when(callback.getEvent()).thenReturn(UPDATE_NEXT_HEARING_INFO);
        when(nextHearingDateSerice.enabled()).thenReturn(true);
        when(asylumCase.read(IS_INTEGRATED, YesOrNo.class)).thenReturn(Optional.of(NO));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            handler.handle(ABOUT_TO_START, callback);

        assertNotNull(callbackResponse);
        verify(nextHearingDateSerice, never()).calculateNextHearingDateFromHearings(callback, ABOUT_TO_START);
        verify(nextHearingDateSerice, never()).calculateNextHearingDateFromCaseData(callback);
        verify(asylumCase).write(eq(NEXT_HEARING_DETAILS), captor.capture());

        NextHearingDetails expected = NextHearingDetails.builder()
            .hearingId(null).hearingDateTime(null).build();
        assertEquals(expected, captor.getValue());
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = handler.canHandle(callbackStage, callback);

                if (event == UPDATE_NEXT_HEARING_INFO
                    || (List.of(LIST_CASE, EDIT_CASE_LISTING, CMR_LISTING, CMR_RE_LISTING).contains(event)
                    && callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT)) {
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

        assertThatThrownBy(() -> handler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> handler.canHandle(ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> handler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> handler.handle(ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
