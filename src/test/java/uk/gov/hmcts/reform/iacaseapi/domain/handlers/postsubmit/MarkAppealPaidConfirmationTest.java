package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

import java.util.Optional;

@MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class MarkAppealPaidConfirmationTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;

    private MarkAppealPaidConfirmation markAppealPaidConfirmation
        = new MarkAppealPaidConfirmation();

    @BeforeEach
    void setUp() {
        when(callback.getEvent()).thenReturn(Event.MARK_APPEAL_PAID);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(AsylumCaseFieldDefinition.IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
    }

    @Test
    void should_return_confirmation() {
        PostSubmitCallbackResponse callbackResponse =
            markAppealPaidConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(
            callbackResponse.getConfirmationHeader().get())
            .contains("# Your have marked the appeal as paid");

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains("#### What happens next\n\n");

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains("The Tribunal will be notified that the fee has been paid. The appeal will progress as usual.");

    }

    @Test
    void should_return_confirmation_for_internal_journey() {
        when(asylumCase.read(AsylumCaseFieldDefinition.IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        PostSubmitCallbackResponse callbackResponse =
                markAppealPaidConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(
                callbackResponse.getConfirmationHeader().get())
                .contains("# Your have marked the appeal as paid");

        assertThat(
                callbackResponse.getConfirmationBody().get())
                .contains("#### What happens next\n\n");

        assertThat(
                callbackResponse.getConfirmationBody().get())
                .contains("The appellant has been notified that the fee has been paid. The appeal will progress as usual");

    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {
        when(callback.getEvent()).thenReturn(Event.BUILD_CASE);

        assertThatThrownBy(() -> markAppealPaidConfirmation.handle(callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            boolean canHandle = markAppealPaidConfirmation.canHandle(callback);

            if (event == Event.MARK_APPEAL_PAID) {

                assertTrue(canHandle);
            } else {
                assertFalse(canHandle);
            }

            reset(callback);
        }
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> markAppealPaidConfirmation.handle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> markAppealPaidConfirmation.canHandle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
