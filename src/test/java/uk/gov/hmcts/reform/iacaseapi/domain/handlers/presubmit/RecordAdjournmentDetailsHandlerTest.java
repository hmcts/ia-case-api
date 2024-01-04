package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.ADDITIONAL_ADJOURNMENT_INFO;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.ADJOURNMENT_DETAILS_HEARING;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.ANY_ADDITIONAL_ADJOURNMENT_INFO;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CURRENT_ADJOURNMENT_DETAIL;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HEARING_ADJOURNMENT_WHEN;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_APPEAL_SUITABLE_TO_FLOAT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.PREVIOUS_ADJOURNMENT_DETAILS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.RESERVE_OR_EXCLUDE_JUDGE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.SHOULD_RESERVE_OR_EXCLUDE_JUDGE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.HearingAdjournmentDay.BEFORE_HEARING_DATE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.HearingAdjournmentDay.ON_HEARING_DATE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.RECORD_ADJOURNMENT_DETAILS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.swing.text.html.Option;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AdjournmentDetail;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.HearingAdjournmentDay;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Value;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class RecordAdjournmentDetailsHandlerTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Captor
    private ArgumentCaptor<List<IdValue<AdjournmentDetail>>> previousDetailsCaptor;
    @Captor
    private ArgumentCaptor<AdjournmentDetail> currentDetailCaptor;

    private RecordAdjournmentDetailsHandler recordAdjournmentDetailsHandler;

    private AdjournmentDetail currentAdjournmentDetail;

    @BeforeEach
    public void setUp() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(RECORD_ADJOURNMENT_DETAILS);

        DynamicList adjournmentDetailsHearing =
            new DynamicList(new Value("code", "adjournmentDetailsHearing"), Collections.emptyList());
        when(asylumCase.read(ADJOURNMENT_DETAILS_HEARING, DynamicList.class))
            .thenReturn(Optional.of(adjournmentDetailsHearing));

        currentAdjournmentDetail = AdjournmentDetail.builder()
            .adjournmentDetailsHearing("adjournmentDetailsHearing")
            .hearingAdjournmentWhen("")
            .hearingAdjournmentDecisionParty("")
            .hearingAdjournmentDecisionPartyName("")
            .hearingAdjournmentRequestingParty("")
            .anyAdditionalAdjournmentInfo("")
            .additionalAdjournmentInfo("")
            .relistCaseImmediately("")
            .nextHearingFormat("")
            .nextHearingLocation("")
            .nextHearingDuration("")
            .nextHearingDate("")
            .nextHearingDateFixed("")
            .nextHearingDateRangeEarliest("")
            .nextHearingDateRangeLatest("")
            .shouldReserveOrExcludeJudge("")
            .reserveOrExcludeJudge("")
            .build();

        recordAdjournmentDetailsHandler = new RecordAdjournmentDetailsHandler();
    }

    @Test
    void should_build_current_adjournment_detail() {

        recordAdjournmentDetailsHandler.handle(ABOUT_TO_SUBMIT, callback);

        verify(asylumCase).write(eq(CURRENT_ADJOURNMENT_DETAIL), currentDetailCaptor.capture());
        assertEquals(currentAdjournmentDetail, currentDetailCaptor.getValue());
    }

    @Test
    void should_build_current_adjournment_detail_and_preserve_previous_adjournment_history() {

        when(asylumCase.read(CURRENT_ADJOURNMENT_DETAIL, AdjournmentDetail.class))
            .thenReturn(Optional.of(currentAdjournmentDetail));

        recordAdjournmentDetailsHandler.handle(ABOUT_TO_SUBMIT, callback);

        verify(asylumCase).write(eq(PREVIOUS_ADJOURNMENT_DETAILS), previousDetailsCaptor.capture());
        assertEquals(1, previousDetailsCaptor.getValue().size());
    }

    @Test
    void should_not_include_optional_stale_info() {

        when(asylumCase.read(ANY_ADDITIONAL_ADJOURNMENT_INFO, YesOrNo.class)).thenReturn(Optional.of(NO));
        when(asylumCase.read(SHOULD_RESERVE_OR_EXCLUDE_JUDGE, YesOrNo.class)).thenReturn(Optional.of(NO));

        recordAdjournmentDetailsHandler.handle(ABOUT_TO_SUBMIT, callback);

        verify(asylumCase).clear(ADDITIONAL_ADJOURNMENT_INFO);
        verify(asylumCase).clear(RESERVE_OR_EXCLUDE_JUDGE);
        verify(asylumCase).write(eq(CURRENT_ADJOURNMENT_DETAIL), currentDetailCaptor.capture());
        assertEquals("", currentDetailCaptor.getValue().getAdditionalAdjournmentInfo());
        assertEquals("", currentDetailCaptor.getValue().getReserveOrExcludeJudge());
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {
            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = recordAdjournmentDetailsHandler.canHandle(callbackStage, callback);

                if (Objects.equals(callbackStage, ABOUT_TO_SUBMIT)
                    && Objects.equals(RECORD_ADJOURNMENT_DETAILS, callback.getEvent())) {

                    assertTrue(canHandle, "Can handle event " + event);
                } else {
                    assertFalse(canHandle, "Cannot handle event " + event);
                }
            }
        }
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> recordAdjournmentDetailsHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> recordAdjournmentDetailsHandler.canHandle(ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> recordAdjournmentDetailsHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> recordAdjournmentDetailsHandler.handle(ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void handler_throws_error_if_cannot_actually_handle() {

        assertThatThrownBy(() -> recordAdjournmentDetailsHandler.handle(ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        assertThatThrownBy(() -> recordAdjournmentDetailsHandler.handle(ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_set_float_suitability_to_no_when_on_hearing_date_and_yes_for_float_suitability() {
        when(asylumCase.read(HEARING_ADJOURNMENT_WHEN, HearingAdjournmentDay.class))
                .thenReturn(Optional.of(ON_HEARING_DATE));

        when(asylumCase.read(IS_APPEAL_SUITABLE_TO_FLOAT, YesOrNo.class))
                .thenReturn(Optional.of(YES));

        recordAdjournmentDetailsHandler.handle(ABOUT_TO_SUBMIT, callback);

        verify(asylumCase, times(1)).write(eq(IS_APPEAL_SUITABLE_TO_FLOAT), eq(NO));
    }

    @Test
    void should_not_set_float_suitability_when_before_hearing_date_and_no_for_float_suitability() {
        when(asylumCase.read(HEARING_ADJOURNMENT_WHEN, HearingAdjournmentDay.class))
                .thenReturn(Optional.of(ON_HEARING_DATE));

        when(asylumCase.read(IS_APPEAL_SUITABLE_TO_FLOAT, YesOrNo.class))
                .thenReturn(Optional.of(NO));

        recordAdjournmentDetailsHandler.handle(ABOUT_TO_SUBMIT, callback);

        verify(asylumCase, times(0)).write(IS_APPEAL_SUITABLE_TO_FLOAT, NO);
    }

    @Test
    void should_not_set_float_suitability_when_before_hearing_date_and_yes_for_float_suitability() {
        when(asylumCase.read(HEARING_ADJOURNMENT_WHEN, HearingAdjournmentDay.class))
                .thenReturn(Optional.of(BEFORE_HEARING_DATE));

        when(asylumCase.read(IS_APPEAL_SUITABLE_TO_FLOAT, YesOrNo.class))
                .thenReturn(Optional.of(YES));

        recordAdjournmentDetailsHandler.handle(ABOUT_TO_SUBMIT, callback);

        verify(asylumCase, times(0)).write(IS_APPEAL_SUITABLE_TO_FLOAT, NO);
    }

    @Test
    void should_not_set_float_suitability_when_null_value() {
        when(asylumCase.read(HEARING_ADJOURNMENT_WHEN, HearingAdjournmentDay.class))
                .thenReturn(Optional.empty());

        when(asylumCase.read(IS_APPEAL_SUITABLE_TO_FLOAT, YesOrNo.class))
                .thenReturn(Optional.empty());

        recordAdjournmentDetailsHandler.handle(ABOUT_TO_SUBMIT, callback);

        verify(asylumCase, times(0)).write(IS_APPEAL_SUITABLE_TO_FLOAT, NO);
    }
}
