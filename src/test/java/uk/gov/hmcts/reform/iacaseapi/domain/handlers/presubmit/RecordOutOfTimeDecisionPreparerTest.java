package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.OutOfTimeDecisionType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.service.OutOfTimeDecisionDetailsAppender;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class RecordOutOfTimeDecisionPreparerTest {

    @Mock private AsylumCase asylumCase;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private Callback<AsylumCase> callback;

    @Mock private Document document;

    private OutOfTimeDecisionDetailsAppender outOfTimeDecisionDetailsAppender;

    private RecordOutOfTimeDecisionPreparer recordOutOfTimeDecisionPreparer;

    private static final String TRIBUNAL_CASE_WORKER = "Tribunal Caseworker";

    @BeforeEach
    void setUp() {

        outOfTimeDecisionDetailsAppender = new OutOfTimeDecisionDetailsAppender();

        recordOutOfTimeDecisionPreparer =
            new RecordOutOfTimeDecisionPreparer(outOfTimeDecisionDetailsAppender);
    }

    @Test
    void should_not_append_if_not_previous_out_of_time_decision_exists() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.RECORD_OUT_OF_TIME_DECISION);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            recordOutOfTimeDecisionPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertNotNull(callbackResponse);

        verify(asylumCase, times(0)).clear(OUT_OF_TIME_DECISION_TYPE);
        verify(asylumCase, times(0)).clear(OUT_OF_TIME_DECISION_MAKER);
        verify(asylumCase, times(0)).clear(OUT_OF_TIME_DECISION_DOCUMENT);
    }

    @Test
    void should_throw_if_no_type_in_previous_out_of_time_decision_details() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.RECORD_OUT_OF_TIME_DECISION);

        when(asylumCase.read(RECORDED_OUT_OF_TIME_DECISION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(PREVIOUS_OUT_OF_TIME_DECISION_DETAILS)).thenReturn(Optional.of(Collections.emptyList()));

        assertThatThrownBy(() -> recordOutOfTimeDecisionPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("Out of time decision type is not present");
    }

    @ParameterizedTest
    @EnumSource(value = OutOfTimeDecisionType.class, names = { "IN_TIME", "APPROVED", "REJECTED" })
    void should_throw_if_no_decision_maker_in_previous_out_of_time_decision_details(OutOfTimeDecisionType outOfTimeDecisionType) {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.RECORD_OUT_OF_TIME_DECISION);

        when(asylumCase.read(RECORDED_OUT_OF_TIME_DECISION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(PREVIOUS_OUT_OF_TIME_DECISION_DETAILS)).thenReturn(Optional.of(Collections.emptyList()));
        when(asylumCase.read(OUT_OF_TIME_DECISION_TYPE, OutOfTimeDecisionType.class))
            .thenReturn(Optional.of(outOfTimeDecisionType));

        assertThatThrownBy(() -> recordOutOfTimeDecisionPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("Out of time decision maker is not present");
    }

    @ParameterizedTest
    @EnumSource(value = OutOfTimeDecisionType.class, names = { "IN_TIME", "APPROVED", "REJECTED" })
    void should_append_if_previous_out_of_time_decisions_exists(OutOfTimeDecisionType outOfTimeDecisionType) {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.RECORD_OUT_OF_TIME_DECISION);

        when(asylumCase.read(RECORDED_OUT_OF_TIME_DECISION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(OUT_OF_TIME_DECISION_TYPE, OutOfTimeDecisionType.class))
            .thenReturn(Optional.of(outOfTimeDecisionType));
        when(asylumCase.read(OUT_OF_TIME_DECISION_MAKER, String.class))
            .thenReturn(Optional.of(TRIBUNAL_CASE_WORKER));
        when(asylumCase.read(OUT_OF_TIME_DECISION_DOCUMENT)).thenReturn(Optional.of(document));
        when(asylumCase.read(PREVIOUS_OUT_OF_TIME_DECISION_DETAILS)).thenReturn(Optional.of(Collections.emptyList()));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            recordOutOfTimeDecisionPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertNotNull(callbackResponse);
        assertEquals(callbackResponse.getData(), asylumCase);
        assertEquals(1, outOfTimeDecisionDetailsAppender.getAllOutOfTimeDecisionDetails().size());

        verify(asylumCase, times(1)).clear(OUT_OF_TIME_DECISION_TYPE);
        verify(asylumCase, times(1)).clear(OUT_OF_TIME_DECISION_MAKER);
        verify(asylumCase, times(1)).clear(OUT_OF_TIME_DECISION_DOCUMENT);
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> recordOutOfTimeDecisionPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = recordOutOfTimeDecisionPreparer.canHandle(callbackStage, callback);

                if ((event == Event.RECORD_OUT_OF_TIME_DECISION)
                    && callbackStage == PreSubmitCallbackStage.ABOUT_TO_START) {

                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }
        }
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> recordOutOfTimeDecisionPreparer.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> recordOutOfTimeDecisionPreparer.canHandle(PreSubmitCallbackStage.ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> recordOutOfTimeDecisionPreparer.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> recordOutOfTimeDecisionPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

}
