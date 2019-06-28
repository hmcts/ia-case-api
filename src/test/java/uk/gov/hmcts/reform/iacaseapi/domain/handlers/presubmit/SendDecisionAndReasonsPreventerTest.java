package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.DECISION_AND_REASONS_AVAILABLE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.DispatchPriority.EARLIEST;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.*;

import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class SendDecisionAndReasonsPreventerTest {

    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;

    private SendDecisionAndReasonsPreventer sendDecisionAndReasonsPreventer;

    @Before
    public void setUp() {
        sendDecisionAndReasonsPreventer =
            new SendDecisionAndReasonsPreventer();
    }

    @Test
    public void should_return_error_when_decision_and_reasons_not_generated() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.SEND_DECISION_AND_REASONS);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(DECISION_AND_REASONS_AVAILABLE, YesOrNo.class))
            .thenReturn(Optional.of(YesOrNo.NO));

        PreSubmitCallbackResponse<AsylumCase> preSubmitCallbackResponse =
            sendDecisionAndReasonsPreventer.handle(ABOUT_TO_START, callback);

        assertThat(preSubmitCallbackResponse.getErrors())
            .containsExactly("You must generate the Decision and reasons draft before completing the Decision and reasons");
    }

    @Test
    public void should_throw_error_when_decision_and_reasons_available_flag_not_present() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.SEND_DECISION_AND_REASONS);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(DECISION_AND_REASONS_AVAILABLE, YesOrNo.class))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> sendDecisionAndReasonsPreventer.handle(ABOUT_TO_START, callback))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("decisionAndReasonsAvailable must be present complete decision and reasons");
    }

    @Test
    public void should_return_earliest() {
        assertThat(sendDecisionAndReasonsPreventer.getDispatchPriority())
            .isEqualTo(EARLIEST);
    }

    @Test
    public void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> sendDecisionAndReasonsPreventer.handle(ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.SEND_DIRECTION);
        assertThatThrownBy(() -> sendDecisionAndReasonsPreventer.handle(ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : values()) {

                boolean canHandle = sendDecisionAndReasonsPreventer.canHandle(callbackStage, callback);

                if (event == Event.SEND_DECISION_AND_REASONS
                    && callbackStage == ABOUT_TO_START) {

                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }

            Mockito.reset(callback);
        }
    }

    @Test
    public void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> sendDecisionAndReasonsPreventer.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> sendDecisionAndReasonsPreventer.canHandle(ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> sendDecisionAndReasonsPreventer.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> sendDecisionAndReasonsPreventer.handle(ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
