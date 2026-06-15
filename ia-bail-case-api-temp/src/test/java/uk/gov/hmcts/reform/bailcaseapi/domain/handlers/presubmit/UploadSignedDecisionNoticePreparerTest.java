package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.RECORD_DECISION_TYPE;
import static uk.gov.hmcts.reform.bailcaseapi.domain.handlers.presubmit.UploadSignedDecisionNoticePreparer.INVALID_EVENT_ERROR_MESSAGE;
import static uk.gov.hmcts.reform.bailcaseapi.domain.handlers.presubmit.UploadSignedDecisionNoticePreparer.MISSING_DECISION_ERROR_MESSAGE;

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
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.DecisionType;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
public class UploadSignedDecisionNoticePreparerTest {

    @Mock
    private Callback<BailCase> callback;
    @Mock
    private CaseDetails<BailCase> caseDetails;
    @Mock
    private BailCase bailCase;

    private UploadSignedDecisionNoticePreparer uploadSignedDecisionNoticePreparer;

    @BeforeEach
    public void setUp() {
        uploadSignedDecisionNoticePreparer = new UploadSignedDecisionNoticePreparer();
        when(callback.getEvent()).thenReturn(Event.UPLOAD_SIGNED_DECISION_NOTICE);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(bailCase);
    }

    @Test
    void should_not_allow_null_arguments() {
        assertThatThrownBy(() -> uploadSignedDecisionNoticePreparer.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(
            () -> uploadSignedDecisionNoticePreparer.canHandle(PreSubmitCallbackStage.ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @ParameterizedTest
    @EnumSource(value = PreSubmitCallbackStage.class, mode = EnumSource.Mode.EXCLUDE, names = {"ABOUT_TO_START"})
    void canHandle_false_if_wrong_callback_stage(PreSubmitCallbackStage callbackStage) {
        assertFalse(uploadSignedDecisionNoticePreparer.canHandle(callbackStage, callback));
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, mode = EnumSource.Mode.EXCLUDE, names = {"UPLOAD_SIGNED_DECISION_NOTICE", "UPLOAD_SIGNED_DECISION_NOTICE_CONDITIONAL_GRANT"})
    void canHandle_false_if_invalid_event(Event event) {
        when(callback.getEvent()).thenReturn(event);
        assertFalse(uploadSignedDecisionNoticePreparer.canHandle(PreSubmitCallbackStage.ABOUT_TO_START, callback));
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {"UPLOAD_SIGNED_DECISION_NOTICE", "UPLOAD_SIGNED_DECISION_NOTICE_CONDITIONAL_GRANT"})
    void canHandle_true_if_valid_event_and_callback_stage(Event event) {
        when(callback.getEvent()).thenReturn(event);
        assertTrue(uploadSignedDecisionNoticePreparer.canHandle(PreSubmitCallbackStage.ABOUT_TO_START, callback));
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {
        assertThatThrownBy(
            () -> uploadSignedDecisionNoticePreparer.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_add_error_if_decision_type_is_missing() {
        PreSubmitCallbackResponse<BailCase> response = uploadSignedDecisionNoticePreparer.handle(
            PreSubmitCallbackStage.ABOUT_TO_START,
            callback
        );
        assertNotNull(response);
        assertEquals(1, response.getErrors().size());
        assertTrue(response.getErrors().contains(MISSING_DECISION_ERROR_MESSAGE));
    }

    @Test
    void should_add_error_if_decision_type_is_invalid_for_event() {
        when(bailCase.read(
            RECORD_DECISION_TYPE,
            String.class
        )).thenReturn(Optional.of(DecisionType.CONDITIONAL_GRANT.toString()));
        PreSubmitCallbackResponse<BailCase> response = uploadSignedDecisionNoticePreparer.handle(
            PreSubmitCallbackStage.ABOUT_TO_START,
            callback
        );
        assertNotNull(response);
        assertEquals(1, response.getErrors().size());
        assertTrue(response.getErrors().contains(INVALID_EVENT_ERROR_MESSAGE));
    }

    @Test
    void should_not_add_error_if_decision_type_is_valid_for_event() {
        when(bailCase.read(RECORD_DECISION_TYPE, String.class)).thenReturn(Optional.of(DecisionType.GRANTED.toString()));
        PreSubmitCallbackResponse<BailCase> response = uploadSignedDecisionNoticePreparer.handle(
            PreSubmitCallbackStage.ABOUT_TO_START,
            callback
        );
        assertNotNull(response);
        assertTrue(response.getErrors().isEmpty());
    }
}
