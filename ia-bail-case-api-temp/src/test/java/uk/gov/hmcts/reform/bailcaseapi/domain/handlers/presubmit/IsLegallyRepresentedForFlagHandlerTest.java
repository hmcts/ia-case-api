package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.YesOrNo;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
public class IsLegallyRepresentedForFlagHandlerTest {

    @Mock
    private Callback<BailCase> callback;

    @Mock
    private BailCase bailCase;

    @Mock
    private CaseDetails<BailCase> caseDetails;

    private IsLegallyRepresentedForFlagHandler isLegallyRepresentedForFlagHandler;

    @BeforeEach
    public void setUp() {
        isLegallyRepresentedForFlagHandler = new IsLegallyRepresentedForFlagHandler();
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(bailCase);
        when(callback.getEvent()).thenReturn(Event.START_APPLICATION);
    }

    @Test
    void should_set_legally_represented_to_yes_is_legal_rep_true() {
        when(bailCase.read(BailCaseFieldDefinition.IS_LEGAL_REP, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(bailCase.read(BailCaseFieldDefinition.HAS_LEGAL_REP, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        PreSubmitCallbackResponse<BailCase> response = isLegallyRepresentedForFlagHandler
            .handle(ABOUT_TO_SUBMIT, callback);

        assertNotNull(response);
        assertThat(response.getData()).isNotEmpty();
        assertThat(response.getData()).isEqualTo(bailCase);
        verify(bailCase, times(1))
            .write(BailCaseFieldDefinition.IS_LEGALLY_REPRESENTED_FOR_FLAG, YesOrNo.YES);
    }

    @Test
    void should_set_legally_represented_to_yes_has_legal_rep_true() {
        when(bailCase.read(BailCaseFieldDefinition.IS_LEGAL_REP, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(bailCase.read(BailCaseFieldDefinition.HAS_LEGAL_REP, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        PreSubmitCallbackResponse<BailCase> response = isLegallyRepresentedForFlagHandler
            .handle(ABOUT_TO_SUBMIT, callback);

        assertNotNull(response);
        assertThat(response.getData()).isNotEmpty();
        assertThat(response.getData()).isEqualTo(bailCase);
        verify(bailCase, times(1))
            .write(BailCaseFieldDefinition.IS_LEGALLY_REPRESENTED_FOR_FLAG, YesOrNo.YES);
    }

    @Test
    void should_set_legally_represented_to_yes_both_true() {
        when(bailCase.read(BailCaseFieldDefinition.IS_LEGAL_REP, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(bailCase.read(BailCaseFieldDefinition.HAS_LEGAL_REP, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        PreSubmitCallbackResponse<BailCase> response = isLegallyRepresentedForFlagHandler
            .handle(ABOUT_TO_SUBMIT, callback);

        assertNotNull(response);
        assertThat(response.getData()).isNotEmpty();
        assertThat(response.getData()).isEqualTo(bailCase);
        verify(bailCase, times(1))
            .write(BailCaseFieldDefinition.IS_LEGALLY_REPRESENTED_FOR_FLAG, YesOrNo.YES);
    }

    @Test
    void should_set_legally_represented_to_no_both_false() {
        when(bailCase.read(BailCaseFieldDefinition.IS_LEGAL_REP, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(bailCase.read(BailCaseFieldDefinition.HAS_LEGAL_REP, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        PreSubmitCallbackResponse<BailCase> response = isLegallyRepresentedForFlagHandler
            .handle(ABOUT_TO_SUBMIT, callback);

        assertNotNull(response);
        assertThat(response.getData()).isNotEmpty();
        assertThat(response.getData()).isEqualTo(bailCase);
        verify(bailCase, times(1))
            .write(BailCaseFieldDefinition.IS_LEGALLY_REPRESENTED_FOR_FLAG, YesOrNo.NO);
    }

    @Test
    void it_can_handle_callback() {
        for (Event event : Event.values()) {
            when(callback.getEvent()).thenReturn(event);
            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {
                boolean canHandle = isLegallyRepresentedForFlagHandler.canHandle(callbackStage, callback);
                if (callbackStage == ABOUT_TO_SUBMIT
                        && (callback.getEvent() == Event.START_APPLICATION
                        || callback.getEvent() == Event.EDIT_BAIL_APPLICATION
                        || callback.getEvent() == Event.MAKE_NEW_APPLICATION
                        || callback.getEvent() == Event.EDIT_BAIL_APPLICATION_AFTER_SUBMIT)) {
                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }
        }
    }

    @Test
    void handler_throws_error_if_cannot_actually_handle() {
        //invalid stage
        assertThatThrownBy(() -> isLegallyRepresentedForFlagHandler.handle(ABOUT_TO_START, callback)).hasMessage(
            "Cannot handle callback").isExactlyInstanceOf(IllegalStateException.class);

        //invalid event
        when(callback.getEvent()).thenReturn(Event.END_APPLICATION);
        assertThatThrownBy(() -> isLegallyRepresentedForFlagHandler.handle(ABOUT_TO_SUBMIT, callback)).hasMessage(
            "Cannot handle callback").isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_not_allow_null_arguments() {
        assertThatThrownBy(() -> isLegallyRepresentedForFlagHandler.canHandle(null, callback)).hasMessage(
            "callbackStage must not be null").isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> isLegallyRepresentedForFlagHandler.canHandle(ABOUT_TO_SUBMIT, null)).hasMessage(
            "callback must not be null").isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> isLegallyRepresentedForFlagHandler.handle(null, callback)).hasMessage(
            "callbackStage must not be null").isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> isLegallyRepresentedForFlagHandler.handle(ABOUT_TO_SUBMIT, null)).hasMessage(
            "callback must not be null").isExactlyInstanceOf(NullPointerException.class);
    }
}
