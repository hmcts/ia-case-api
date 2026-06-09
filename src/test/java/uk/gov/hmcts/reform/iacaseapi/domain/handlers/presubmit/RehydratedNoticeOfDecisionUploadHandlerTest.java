package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_REHYDRATED_APPEAL;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.UPLOAD_THE_NOTICE_OF_DECISION_DOCS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.UPLOAD_THE_NOTICE_OF_DECISION_DOCS_REHYDRATED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentWithDescription;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class RehydratedNoticeOfDecisionUploadHandlerTest {

    @Mock private AsylumCase asylumCase;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private Callback<AsylumCase> callback;

    @Mock
    private DocumentWithDescription noticeOfDecision;

    private RehydratedNoticeOfDecisionUploadHandler rehydratedNoticeOfDecisionUploadHandler;

    @BeforeEach
    void setUp() {
        rehydratedNoticeOfDecisionUploadHandler = new RehydratedNoticeOfDecisionUploadHandler();

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getCaseDetails().getCaseData()).thenReturn(asylumCase);
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {"START_APPEAL", "EDIT_APPEAL", "EDIT_APPEAL_AFTER_SUBMIT"})
    void transfer_value_if_rehydrated_case_and_rehydrated_value_present(Event event) {
        when(callback.getEvent()).thenReturn(event);

        when(asylumCase.read(IS_REHYDRATED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        List<IdValue<DocumentWithDescription>> noticeOfDecisionDocument =
                Arrays.asList(
                        new IdValue<>("1", noticeOfDecision)
                );
        when(asylumCase.read(UPLOAD_THE_NOTICE_OF_DECISION_DOCS)).thenReturn(Optional.empty());
        when(asylumCase.read(UPLOAD_THE_NOTICE_OF_DECISION_DOCS_REHYDRATED)).thenReturn(Optional.of(noticeOfDecisionDocument));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse = rehydratedNoticeOfDecisionUploadHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertThat(callbackResponse).isNotNull();

        verify(asylumCase, times(1)).write(
                UPLOAD_THE_NOTICE_OF_DECISION_DOCS,
                noticeOfDecisionDocument
        );

        verify(asylumCase, times(1)).clear(UPLOAD_THE_NOTICE_OF_DECISION_DOCS_REHYDRATED);
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {"START_APPEAL", "EDIT_APPEAL", "EDIT_APPEAL_AFTER_SUBMIT"})
    void do_not_transfer_value_if_rehydrated_case_but_rehydrated_value_empty(Event event) {
        when(callback.getEvent()).thenReturn(event);

        when(asylumCase.read(IS_REHYDRATED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        List<IdValue<DocumentWithDescription>> noticeOfDecisionDocument =
                Arrays.asList(
                        new IdValue<>("1", noticeOfDecision)
                );

        when(asylumCase.read(UPLOAD_THE_NOTICE_OF_DECISION_DOCS)).thenReturn(Optional.empty());
        when(asylumCase.read(UPLOAD_THE_NOTICE_OF_DECISION_DOCS_REHYDRATED)).thenReturn(Optional.empty());

        PreSubmitCallbackResponse<AsylumCase> callbackResponse = rehydratedNoticeOfDecisionUploadHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertThat(callbackResponse).isNotNull();

        verify(asylumCase, times(0)).write(UPLOAD_THE_NOTICE_OF_DECISION_DOCS, noticeOfDecisionDocument);
        verify(asylumCase, times(0)).clear(UPLOAD_THE_NOTICE_OF_DECISION_DOCS_REHYDRATED);
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {"START_APPEAL", "EDIT_APPEAL", "EDIT_APPEAL_AFTER_SUBMIT"})
    void do_not_transfer_value_if_rehydrated_case_but_original_value_present(Event event) {
        when(callback.getEvent()).thenReturn(event);

        when(asylumCase.read(IS_REHYDRATED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        List<IdValue<DocumentWithDescription>> noticeOfDecisionDocument =
                Arrays.asList(
                        new IdValue<>("1", noticeOfDecision)
                );

        when(asylumCase.read(UPLOAD_THE_NOTICE_OF_DECISION_DOCS)).thenReturn(Optional.empty());
        when(asylumCase.read(UPLOAD_THE_NOTICE_OF_DECISION_DOCS_REHYDRATED)).thenReturn(Optional.empty());

        PreSubmitCallbackResponse<AsylumCase> callbackResponse = rehydratedNoticeOfDecisionUploadHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertThat(callbackResponse).isNotNull();

        verify(asylumCase, times(0)).write(UPLOAD_THE_NOTICE_OF_DECISION_DOCS, noticeOfDecisionDocument);
        verify(asylumCase, times(0)).clear(UPLOAD_THE_NOTICE_OF_DECISION_DOCS_REHYDRATED);
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {"START_APPEAL", "EDIT_APPEAL", "EDIT_APPEAL_AFTER_SUBMIT"})
    void do_not_handle_for_non_rehydrated_case(Event event) {
        when(callback.getEvent()).thenReturn(event);
        when(asylumCase.read(IS_REHYDRATED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        assertThatThrownBy(() ->
                rehydratedNoticeOfDecisionUploadHandler.handle(ABOUT_TO_SUBMIT, callback)
        ).isInstanceOf(IllegalStateException.class)
                .hasMessage("Cannot handle callback");
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {
        assertThatThrownBy(() -> rehydratedNoticeOfDecisionUploadHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
                .hasMessage("Cannot handle callback")
                .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> rehydratedNoticeOfDecisionUploadHandler.canHandle(null, callback))
                .hasMessage("callbackStage must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> rehydratedNoticeOfDecisionUploadHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_START, null))
                .hasMessage("callback must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> rehydratedNoticeOfDecisionUploadHandler.handle(null, callback))
                .hasMessage("callbackStage must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> rehydratedNoticeOfDecisionUploadHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, null))
                .hasMessage("callback must not be null")
                .isExactlyInstanceOf(NullPointerException.class);
    }
}