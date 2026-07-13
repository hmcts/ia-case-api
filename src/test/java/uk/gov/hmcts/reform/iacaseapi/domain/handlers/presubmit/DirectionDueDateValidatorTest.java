package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.SEND_DIRECTION_DATE_DUE;

import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;

@ExtendWith(MockitoExtension.class)
class DirectionDueDateValidatorTest {

    @Mock
    private DateProvider dateProvider;
    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;

    private DirectionDueDateValidator handler;

    @BeforeEach
    void setUp() {
        handler = new DirectionDueDateValidator(dateProvider);

    }

    @Test
    void should_handle_send_direction_mid_event() {

        when(callback.getEvent()).thenReturn(Event.SEND_DIRECTION);
        when(callback.getPageId()).thenReturn("sendDirection");

        assertTrue(handler.canHandle(
                PreSubmitCallbackStage.MID_EVENT,
                callback
        ));
    }

    @Test
    void should_not_handle_wrong_stage() {

        assertFalse(handler.canHandle(
                PreSubmitCallbackStage.ABOUT_TO_SUBMIT,
                callback
        ));
    }

    @Test
    void should_not_handle_wrong_event() {

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);

        assertFalse(handler.canHandle(
                PreSubmitCallbackStage.MID_EVENT,
                callback
        ));
    }

    @Test
    void should_add_error_when_due_date_is_in_the_past() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.SEND_DIRECTION);
        when(callback.getPageId()).thenReturn("sendDirection");
        when(dateProvider.now()).thenReturn(LocalDate.of(2025, 1, 10));
        when(asylumCase.read(SEND_DIRECTION_DATE_DUE, String.class))
                .thenReturn(Optional.of("2025-01-09"));

        PreSubmitCallbackResponse<AsylumCase> response =
                handler.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertEquals(1, response.getErrors().size());

        assertThat(response.getErrors())
                .containsExactly("The date entered is not valid - this must be today or a date in the future");
    }

    @Test
    void should_not_add_error_when_due_date_is_today() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.SEND_DIRECTION);
        when(callback.getPageId()).thenReturn("sendDirection");
        when(dateProvider.now()).thenReturn(LocalDate.of(2025, 1, 10));
        when(asylumCase.read(SEND_DIRECTION_DATE_DUE, String.class))
                .thenReturn(Optional.of("2025-01-10"));

        PreSubmitCallbackResponse<AsylumCase> response =
                handler.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertTrue(response.getErrors().isEmpty());
    }

    @Test
    void should_not_add_error_when_due_date_is_in_the_future() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.SEND_DIRECTION);
        when(callback.getPageId()).thenReturn("sendDirection");
        when(dateProvider.now()).thenReturn(LocalDate.of(2025, 1, 10));
        when(asylumCase.read(SEND_DIRECTION_DATE_DUE, String.class))
                .thenReturn(Optional.of("2025-01-11"));

        PreSubmitCallbackResponse<AsylumCase> response =
                handler.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertTrue(response.getErrors().isEmpty());
    }

    @Test
    void should_throw_if_cannot_handle() {

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);

        assertThatThrownBy(() ->
                handler.handle(PreSubmitCallbackStage.MID_EVENT, callback))
                .isExactlyInstanceOf(IllegalStateException.class)
                .hasMessage("Cannot handle callback");
    }

    @Test
    void should_not_add_error_when_due_date_not_present() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.SEND_DIRECTION);
        when(callback.getPageId()).thenReturn("sendDirection");
        when(asylumCase.read(SEND_DIRECTION_DATE_DUE, String.class))
                .thenReturn(Optional.empty());

        PreSubmitCallbackResponse<AsylumCase> response =
                handler.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertThat(response.getErrors()).isEmpty();
    }

    @Test
    void should_not_handle_wrong_page() {

        when(callback.getEvent()).thenReturn(Event.SEND_DIRECTION);
        when(callback.getPageId()).thenReturn("someOtherPage");

        assertFalse(handler.canHandle(
                PreSubmitCallbackStage.MID_EVENT,
                callback
        ));
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> handler.canHandle(null, callback))
                .isExactlyInstanceOf(NullPointerException.class)
                .hasMessage("callbackStage must not be null");

        assertThatThrownBy(() -> handler.canHandle(PreSubmitCallbackStage.MID_EVENT, null))
                .isExactlyInstanceOf(NullPointerException.class)
                .hasMessage("callback must not be null");

        assertThatThrownBy(() -> handler.handle(null, callback))
                .isExactlyInstanceOf(NullPointerException.class)
                .hasMessage("callbackStage must not be null");

        assertThatThrownBy(() -> handler.handle(PreSubmitCallbackStage.MID_EVENT, null))
                .isExactlyInstanceOf(NullPointerException.class)
                .hasMessage("callback must not be null");
    }
}