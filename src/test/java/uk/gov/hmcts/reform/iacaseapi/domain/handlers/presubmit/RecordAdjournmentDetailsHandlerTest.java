package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.ADJOURNMENT_DETAILS_HEARING;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CURRENT_ADJOURNMENT_DETAIL;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.PREVIOUS_ADJOURNMENT_DETAILS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.RECORD_ADJOURNMENT_DETAILS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Value;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;

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
            .shouldReserveOrExcludedJudge("")
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
}
