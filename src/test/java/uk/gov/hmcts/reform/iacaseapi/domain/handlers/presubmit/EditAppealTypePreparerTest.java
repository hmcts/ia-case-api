package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealTypeForDisplay;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.JourneyType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.EDIT_APPEAL;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.START_APPEAL;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.DispatchPriority.EARLIEST;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
public class EditAppealTypePreparerTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;

    private EditAppealTypePreparer editAppealTypePreparer;

    @BeforeEach
    void setup() {
        editAppealTypePreparer = new EditAppealTypePreparer();
    }

    @Test
    void should_set_appealTypeForDisplay_if_ageAssessment_no_and_appealTypeForDisplay_is_empty() {
        when(callback.getEvent()).thenReturn(EDIT_APPEAL);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(AGE_ASSESSMENT, YesOrNo.class)).thenReturn(Optional.of(NO));
        when(asylumCase.read(APPEAL_TYPE_FOR_DISPLAY, AppealTypeForDisplay.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPEAL_TYPE, AppealType.class))
                .thenReturn(Optional.of(AppealType.RP));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            editAppealTypePreparer.handle(ABOUT_TO_START, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).write(APPEAL_TYPE_FOR_DISPLAY,
                AppealTypeForDisplay.from(AppealType.RP.getValue()));
    }

    @Test
    void should_not_set_appealTypeForDisplay_if_ageAssessment_no_and_appealTypeForDisplay_is_not_empty() {
        when(callback.getEvent()).thenReturn(EDIT_APPEAL);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(AGE_ASSESSMENT, YesOrNo.class)).thenReturn(Optional.of(NO));
        when(asylumCase.read(APPEAL_TYPE_FOR_DISPLAY, AppealTypeForDisplay.class))
                .thenReturn(Optional.of(AppealTypeForDisplay.RP));
        when(asylumCase.read(APPEAL_TYPE, AppealType.class))
                .thenReturn(Optional.of(AppealType.RP));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                editAppealTypePreparer.handle(ABOUT_TO_START, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(0)).write(APPEAL_TYPE_FOR_DISPLAY,
                AppealTypeForDisplay.from(AppealType.RP.getValue()));
    }

    @Test
    void should_throw_error_if_appealType_and_appealTypeForDisplay_are_both_empty() {
        when(callback.getEvent()).thenReturn(EDIT_APPEAL);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(AGE_ASSESSMENT, YesOrNo.class)).thenReturn(Optional.of(NO));
        when(asylumCase.read(APPEAL_TYPE_FOR_DISPLAY, AppealTypeForDisplay.class))
            .thenReturn(Optional.empty());
        when(asylumCase.read(APPEAL_TYPE, AppealType.class))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> editAppealTypePreparer.handle(ABOUT_TO_SUBMIT, callback))
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = editAppealTypePreparer.canHandle(callbackStage, callback);

                if (callbackStage == ABOUT_TO_START && event == EDIT_APPEAL) {
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

        assertThatThrownBy(() -> editAppealTypePreparer.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> editAppealTypePreparer.canHandle(ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> editAppealTypePreparer.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> editAppealTypePreparer.handle(ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void handler_throws_error_if_cannot_actually_handle() {

        assertThatThrownBy(() -> editAppealTypePreparer.handle(ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

    }

}
