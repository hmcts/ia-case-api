package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.DATE_BEFORE_ADJOURN_WITHOUT_DATE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HEARING_ADJOURNMENT_WHEN;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.LIST_CASE_HEARING_DATE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.RELIST_CASE_IMMEDIATELY;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.STATE_BEFORE_ADJOURN_WITHOUT_DATE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.HearingAdjournmentDay.BEFORE_HEARING_DATE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.HearingAdjournmentDay.ON_HEARING_DATE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.RECORD_ADJOURNMENT_DETAILS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State.ADJOURNED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State.LISTING;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;

import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.HearingAdjournmentDay;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.service.IaHearingsApiService;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class RecordAdjournmentDetailsStateHandlerTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private PreSubmitCallbackResponse<AsylumCase> callbackResponse;
    @Mock
    private IaHearingsApiService iaHearingsApiService;
    private RecordAdjournmentDetailsStateHandler recordAdjournmentDetailsStateHandler;

    private final String listCaseHearingDate = "4023-12-28T09:47:22.000";

    @BeforeEach
    public void setUp() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getCaseDetails().getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(RECORD_ADJOURNMENT_DETAILS);
        when(callback.getCaseDetails().getState()).thenReturn(LISTING);

        recordAdjournmentDetailsStateHandler =
            new RecordAdjournmentDetailsStateHandler(iaHearingsApiService);
    }

    @Test
    void should_set_appeal_as_listing_on_hearing_date_and_relist() {

        when(asylumCase.read(HEARING_ADJOURNMENT_WHEN, HearingAdjournmentDay.class))
            .thenReturn(Optional.of(ON_HEARING_DATE));
        when(asylumCase.read(RELIST_CASE_IMMEDIATELY, YesOrNo.class))
            .thenReturn(Optional.of(YES));

        PreSubmitCallbackResponse<AsylumCase> response = recordAdjournmentDetailsStateHandler
            .handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback, callbackResponse);

        assertNotNull(response);
        assertThat(response.getState()).isEqualTo(LISTING);
    }

    @Test
    void should_set_appeal_as_adjourned_on_hearing_date_and_not_relist() {

        when(asylumCase.read(HEARING_ADJOURNMENT_WHEN, HearingAdjournmentDay.class))
            .thenReturn(Optional.of(ON_HEARING_DATE));
        when(asylumCase.read(RELIST_CASE_IMMEDIATELY, YesOrNo.class))
            .thenReturn(Optional.of(NO));
        when(asylumCase.read(LIST_CASE_HEARING_DATE, String.class))
            .thenReturn(Optional.of(listCaseHearingDate));

        PreSubmitCallbackResponse<AsylumCase> response = recordAdjournmentDetailsStateHandler
            .handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback, callbackResponse);

        assertNotNull(response);
        assertThat(response.getState()).isEqualTo(ADJOURNED);
        verify(asylumCase, times(1))
            .write(STATE_BEFORE_ADJOURN_WITHOUT_DATE, LISTING.toString());
        verify(asylumCase, times(1))
            .write(DATE_BEFORE_ADJOURN_WITHOUT_DATE, listCaseHearingDate);

    }

    @Test
    void should_set_appeal_as_adjourned_before_hearing_date_and_not_relist() {
        when(asylumCase.read(HEARING_ADJOURNMENT_WHEN, HearingAdjournmentDay.class))
            .thenReturn(Optional.of(HearingAdjournmentDay.BEFORE_HEARING_DATE));
        when(asylumCase.read(LIST_CASE_HEARING_DATE, String.class))
            .thenReturn(Optional.of(listCaseHearingDate));
        when(asylumCase.read(RELIST_CASE_IMMEDIATELY, YesOrNo.class))
            .thenReturn(Optional.of(NO));

        PreSubmitCallbackResponse<AsylumCase> response = recordAdjournmentDetailsStateHandler
            .handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback, callbackResponse);

        assertNotNull(response);
        assertThat(response.getState()).isEqualTo(ADJOURNED);
        verify(asylumCase, times(1))
            .write(STATE_BEFORE_ADJOURN_WITHOUT_DATE, LISTING.toString());
        verify(asylumCase, times(1))
            .write(DATE_BEFORE_ADJOURN_WITHOUT_DATE, listCaseHearingDate);
    }

    @Test
    void should_not_set_appeal_as_adjourned() {
        when(asylumCase.read(HEARING_ADJOURNMENT_WHEN, HearingAdjournmentDay.class))
            .thenReturn(Optional.of(HearingAdjournmentDay.BEFORE_HEARING_DATE));
        when(asylumCase.read(RELIST_CASE_IMMEDIATELY, YesOrNo.class))
            .thenReturn(Optional.of(YES));
        when(iaHearingsApiService.aboutToSubmit(callback)).thenReturn(asylumCase);

        PreSubmitCallbackResponse<AsylumCase> response = recordAdjournmentDetailsStateHandler
            .handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback, callbackResponse);

        assertNotNull(response);
        assertThat(response.getState()).isEqualTo(LISTING);
    }

    @Test
    void should_throw_if_hearing_adjustment_day_is_not_present() {

        assertThatThrownBy(() -> recordAdjournmentDetailsStateHandler
            .handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback, callbackResponse))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Hearing adjournment day is not present");
    }

    @Test
    void should_throw_if_relist_immediately_is_not_present() {
        when(asylumCase.read(HEARING_ADJOURNMENT_WHEN, HearingAdjournmentDay.class))
            .thenReturn(Optional.of(ON_HEARING_DATE));

        assertThatThrownBy(() -> recordAdjournmentDetailsStateHandler
            .handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback, callbackResponse))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Response to relist case immediately is not present");
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> recordAdjournmentDetailsStateHandler
            .handle(PreSubmitCallbackStage.ABOUT_TO_START, callback, callbackResponse))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = recordAdjournmentDetailsStateHandler.canHandle(callbackStage, callback);

                if (event == RECORD_ADJOURNMENT_DETAILS
                    && callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                ) {

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

        assertThatThrownBy(() -> recordAdjournmentDetailsStateHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> recordAdjournmentDetailsStateHandler
            .handle(null, callback, callbackResponse))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> recordAdjournmentDetailsStateHandler
            .handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null, callbackResponse))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @ParameterizedTest
    @MethodSource("shouldCallHearingApi")
    void should_call_hearings_api_if_adjourned_before_hearing_date_and_not_listed_immediately(
        HearingAdjournmentDay adjournmentDay,
        YesOrNo relistCaseImmediately,
        int callToHearingsApi) {
        when(asylumCase.read(HEARING_ADJOURNMENT_WHEN, HearingAdjournmentDay.class))
            .thenReturn(Optional.of(adjournmentDay));
        if (relistCaseImmediately.equals(NO)) {
            when(asylumCase.read(LIST_CASE_HEARING_DATE, String.class))
                .thenReturn(Optional.of(listCaseHearingDate));
        }

        when(asylumCase.read(RELIST_CASE_IMMEDIATELY, YesOrNo.class))
            .thenReturn(Optional.of(relistCaseImmediately));

        recordAdjournmentDetailsStateHandler
            .handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback, callbackResponse);

        verify(iaHearingsApiService, times(callToHearingsApi)).aboutToSubmit(callback);
    }

    private static Stream<Arguments> shouldCallHearingApi() {
        return Stream.of(
            Arguments.of(ON_HEARING_DATE, YES, 0),
            Arguments.of(ON_HEARING_DATE, NO, 0),
            Arguments.of(BEFORE_HEARING_DATE, YES, 1),
            Arguments.of(BEFORE_HEARING_DATE, NO, 1)
        );
    }

}
