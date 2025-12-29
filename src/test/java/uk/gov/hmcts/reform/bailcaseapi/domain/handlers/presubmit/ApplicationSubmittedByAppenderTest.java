package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ApplicationSubmittedByAppenderTest {

    ApplicationSubmittedByAppender applicationSubmittedByAppender;
    @Mock private BailCase bailCase;
    @Mock private Callback<BailCase> callback;
    @Mock private CaseDetails<BailCase> caseDetails;

    @BeforeEach
    void setUp() {
        applicationSubmittedByAppender = new ApplicationSubmittedByAppender();
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(bailCase);
        when(callback.getEvent()).thenReturn(Event.START_APPLICATION);
    }

    @Test
    void should_be_handled_at_latest_point() {
        assertEquals(DispatchPriority.LATEST, applicationSubmittedByAppender.getDispatchPriority());
    }

    @Test
    void should_return_correct_user_LR() {
        when(bailCase.read(BailCaseFieldDefinition.IS_LEGAL_REP, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(bailCase.read(BailCaseFieldDefinition.IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(bailCase.read(BailCaseFieldDefinition.IS_HOME_OFFICE, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        PreSubmitCallbackResponse<BailCase> response = applicationSubmittedByAppender.handle(ABOUT_TO_SUBMIT, callback);

        assertNotNull(response);
        assertThat(response.getData()).isNotEmpty();
        assertThat(response.getData()).isEqualTo(bailCase);
        verify(bailCase, times(1))
            .write(BailCaseFieldDefinition.APPLICATION_SUBMITTED_BY, "Legal Representative");
    }

    @Test
    void should_return_correct_user_HO() {
        when(bailCase.read(BailCaseFieldDefinition.IS_LEGAL_REP, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(bailCase.read(BailCaseFieldDefinition.IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(bailCase.read(BailCaseFieldDefinition.IS_HOME_OFFICE, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        PreSubmitCallbackResponse<BailCase> response = applicationSubmittedByAppender.handle(ABOUT_TO_SUBMIT, callback);

        assertNotNull(response);
        assertThat(response.getData()).isNotEmpty();
        assertThat(response.getData()).isEqualTo(bailCase);
        verify(bailCase, times(1))
            .write(BailCaseFieldDefinition.APPLICATION_SUBMITTED_BY, "Home Office");
    }

    @Test
    void should_return_correct_user_Applicant_via_Admin() {
        when(bailCase.read(BailCaseFieldDefinition.IS_LEGAL_REP, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(bailCase.read(BailCaseFieldDefinition.IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(bailCase.read(BailCaseFieldDefinition.APPLICATION_SENT_BY, String.class)).thenReturn(Optional.of(
            "Applicant"));
        when(bailCase.read(BailCaseFieldDefinition.IS_HOME_OFFICE, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        PreSubmitCallbackResponse<BailCase> response = applicationSubmittedByAppender.handle(ABOUT_TO_SUBMIT, callback);

        assertNotNull(response);
        assertThat(response.getData()).isNotEmpty();
        assertThat(response.getData()).isEqualTo(bailCase);
        verify(bailCase, times(1))
            .write(BailCaseFieldDefinition.APPLICATION_SUBMITTED_BY, "Applicant");
    }

    @Test
    void should_throw_exception_if_unknown_user() {
        when(bailCase.read(BailCaseFieldDefinition.IS_LEGAL_REP, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(bailCase.read(BailCaseFieldDefinition.IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(bailCase.read(BailCaseFieldDefinition.IS_HOME_OFFICE, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        assertThatThrownBy(() -> applicationSubmittedByAppender.handle(ABOUT_TO_SUBMIT, callback)).isExactlyInstanceOf(
            IllegalStateException.class).hasMessage("Unknown user");
    }

    @Test
    void should_throw_exception_if_admin_and_missing_field() {
        when(bailCase.read(BailCaseFieldDefinition.IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(bailCase.read(BailCaseFieldDefinition.APPLICATION_SENT_BY, String.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> applicationSubmittedByAppender.handle(ABOUT_TO_SUBMIT, callback)).isExactlyInstanceOf(
            IllegalStateException.class).hasMessage("Missing the field for Admin - APPLICATION_SENT_BY");
    }

    @Test
    void it_can_handle_callback() {
        for (Event event : Event.values()) {
            when(callback.getEvent()).thenReturn(event);
            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {
                boolean canHandle = applicationSubmittedByAppender.canHandle(callbackStage, callback);
                if (callbackStage == ABOUT_TO_SUBMIT
                    && (callback.getEvent() == Event.START_APPLICATION
                        || callback.getEvent() == Event.EDIT_BAIL_APPLICATION
                        || callback.getEvent() == Event.EDIT_BAIL_APPLICATION_AFTER_SUBMIT
                        || callback.getEvent() == Event.MAKE_NEW_APPLICATION)) {
                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }
        }
    }


    @Test
    void should_not_allow_null_arguments() {
        assertThatThrownBy(() -> applicationSubmittedByAppender.canHandle(null, callback)).hasMessage(
            "callbackStage must not be null").isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> applicationSubmittedByAppender.canHandle(ABOUT_TO_SUBMIT, null)).hasMessage(
            "callback must not be null").isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> applicationSubmittedByAppender.handle(null, callback)).hasMessage(
            "callbackStage must not be null").isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> applicationSubmittedByAppender.handle(ABOUT_TO_SUBMIT, null)).hasMessage(
            "callback must not be null").isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void handler_throws_error_if_cannot_actually_handle() {
        //invalid stage
        assertThatThrownBy(() -> applicationSubmittedByAppender.handle(ABOUT_TO_START, callback)).hasMessage(
            "Cannot handle callback").isExactlyInstanceOf(IllegalStateException.class);

        //invalid event
        when(callback.getEvent()).thenReturn(Event.END_APPLICATION);
        assertThatThrownBy(() -> applicationSubmittedByAppender.handle(ABOUT_TO_SUBMIT, callback)).hasMessage(
            "Cannot handle callback").isExactlyInstanceOf(IllegalStateException.class);
    }

}
