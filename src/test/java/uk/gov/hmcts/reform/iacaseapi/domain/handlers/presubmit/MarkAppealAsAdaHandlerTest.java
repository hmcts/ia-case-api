package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DetentionStatus;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class MarkAppealAsAdaHandlerTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private DateProvider dateProvider;
    private MarkAppealAsAdaHandler markAppealAsAdaHandler;
    private LocalDate date = LocalDate.now();
    private static final String MARK_APPEAL_AS_ADA_REASON = "mark appeal as ada reason";
    private static final String ADA_SUFFIX_VALUE = "_ada";
    private static final String DECISION_DATE = "25 Feb 2023";

    @BeforeEach
    public void setup() {

        when(dateProvider.now()).thenReturn(date);
        markAppealAsAdaHandler = new MarkAppealAsAdaHandler(dateProvider);
    }

    @Test
    void should_mark_appeal_as_ada_and_update_case_data() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.PA));
        when(asylumCase.read(MARK_APPEAL_AS_ADA_EXPLANATION, String.class)).thenReturn(Optional.of(MARK_APPEAL_AS_ADA_REASON));
        when(callback.getEvent()).thenReturn(Event.MARK_APPEAL_AS_ADA);
        when(asylumCase.read(HOME_OFFICE_DECISION_DATE, String.class)).thenReturn(Optional.of(DECISION_DATE));
        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            markAppealAsAdaHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase).write(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.YES);
        verify(asylumCase).write(DETENTION_STATUS, DetentionStatus.ACCELERATED);
        verify(asylumCase).write(DATE_MARKED_AS_ADA, dateProvider.now().toString());
        verify(asylumCase).write(REASON_APPEAL_MARKED_AS_ADA, MARK_APPEAL_AS_ADA_REASON);
        verify(asylumCase).write(ADA_SUFFIX, ADA_SUFFIX_VALUE);
        verify(asylumCase).write(DECISION_LETTER_RECEIVED_DATE, DECISION_DATE);
        verify(asylumCase).clear(MARK_APPEAL_AS_ADA_EXPLANATION);
        verify(asylumCase).clear(HOME_OFFICE_DECISION_DATE);
    }

    @Test
    void should_not_mark_appeal_as_ada_when_aaa_appeal() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.AG));
        when(asylumCase.read(MARK_APPEAL_AS_ADA_EXPLANATION, String.class)).thenReturn(Optional.of(MARK_APPEAL_AS_ADA_REASON));
        when(callback.getEvent()).thenReturn(Event.MARK_APPEAL_AS_ADA);
        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            markAppealAsAdaHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(0)).write(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.YES);
        verify(asylumCase, times(0)).write(DETENTION_STATUS, DetentionStatus.ACCELERATED);
        verify(asylumCase, times(0)).write(DATE_MARKED_AS_ADA, dateProvider.now().toString());
        verify(asylumCase, times(0)).write(REASON_APPEAL_MARKED_AS_ADA, MARK_APPEAL_AS_ADA_REASON);
        verify(asylumCase, times(0)).write(ADA_SUFFIX, ADA_SUFFIX_VALUE);
    }

    @Test
    void should_not_mark_appeal_as_ada_when_ada_appeal() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.PA));
        when(asylumCase.read(MARK_APPEAL_AS_ADA_EXPLANATION, String.class)).thenReturn(Optional.of(MARK_APPEAL_AS_ADA_REASON));
        when(callback.getEvent()).thenReturn(Event.MARK_APPEAL_AS_ADA);
        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            markAppealAsAdaHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(0)).write(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.YES);
        verify(asylumCase, times(0)).write(DETENTION_STATUS, DetentionStatus.ACCELERATED);
        verify(asylumCase, times(0)).write(DATE_MARKED_AS_ADA, dateProvider.now().toString());
        verify(asylumCase, times(0)).write(REASON_APPEAL_MARKED_AS_ADA, MARK_APPEAL_AS_ADA_REASON);
        verify(asylumCase, times(0)).write(ADA_SUFFIX, ADA_SUFFIX_VALUE);
    }

    @Test
    void should_not_mark_appeal_as_ada_when_non_detained_appeal() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.PA));
        when(asylumCase.read(MARK_APPEAL_AS_ADA_EXPLANATION, String.class)).thenReturn(Optional.of(MARK_APPEAL_AS_ADA_REASON));
        when(callback.getEvent()).thenReturn(Event.MARK_APPEAL_AS_ADA);
        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            markAppealAsAdaHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(0)).write(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.YES);
        verify(asylumCase, times(0)).write(DETENTION_STATUS, DetentionStatus.ACCELERATED);
        verify(asylumCase, times(0)).write(DATE_MARKED_AS_ADA, dateProvider.now().toString());
        verify(asylumCase, times(0)).write(REASON_APPEAL_MARKED_AS_ADA, MARK_APPEAL_AS_ADA_REASON);
        verify(asylumCase, times(0)).write(ADA_SUFFIX, ADA_SUFFIX_VALUE);
    }

    @Test
    void handling_should_throw_if_mark_appeal_as_ada_explanation_not_set() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.PA));
        when(asylumCase.read(MARK_APPEAL_AS_ADA_EXPLANATION, String.class)).thenReturn(Optional.empty());
        when(callback.getEvent()).thenReturn(Event.MARK_APPEAL_AS_ADA);

        assertThatThrownBy(
            () -> markAppealAsAdaHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Explain why this appeal is being marked an accelerated detained appeal is required")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        assertThatThrownBy(
                () -> markAppealAsAdaHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
                .hasMessage("Cannot handle callback")
                .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {
        for (Event event : Event.values()) {
            when(callback.getEvent()).thenReturn(event);
            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {
                boolean canHandle = markAppealAsAdaHandler.canHandle(callbackStage, callback);
                if (callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                        && ((callback.getEvent() == Event.MARK_APPEAL_AS_ADA))) {
                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }
        }
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> markAppealAsAdaHandler.canHandle(null, callback))
                .hasMessage("callbackStage must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(
                () -> markAppealAsAdaHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
                .hasMessage("callback must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> markAppealAsAdaHandler.handle(null, callback))
                .hasMessage("callbackStage must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> markAppealAsAdaHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
                .hasMessage("callback must not be null")
                .isExactlyInstanceOf(NullPointerException.class);
    }

}
