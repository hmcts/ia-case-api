package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Optional;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.SEND_DIRECTION_WITH_QUESTIONS;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class SendDirectionWithQuestionsMidEventHandlerTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private DateProvider dateProvider;
    @Mock
    private SendDirectionWithQuestionsMidEventHandler handler;

    @BeforeEach
    public void setup() {
        handler = new SendDirectionWithQuestionsMidEventHandler(dateProvider);
    }

    @Test
    void error_if_direction_due_date_is_today() {
        setupCallbackAndCaseDetails("2020-02-02");
        when(dateProvider.now()).thenReturn(LocalDate.parse("2020-02-02"));

        PreSubmitCallbackResponse<AsylumCase> response = handler.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertThat(response.getErrors()).containsExactlyInAnyOrderElementsOf(
                new HashSet<>(singletonList("Direction due date must be in the future")));
    }

    @Test
    void error_if_direction_due_date_is_in_past() {
        setupCallbackAndCaseDetails("2020-02-01");
        when(dateProvider.now()).thenReturn(LocalDate.parse("2020-02-02"));

        PreSubmitCallbackResponse<AsylumCase> response = handler.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertThat(response.getErrors()).containsExactlyInAnyOrderElementsOf(
                new HashSet<>(singletonList("Direction due date must be in the future")));
    }

    @Test
    void return_normally_if_direction_due_date_in_future() {
        setupCallbackAndCaseDetails("2020-03-02");
        when(dateProvider.now()).thenReturn(LocalDate.parse("2020-02-02"));

        PreSubmitCallbackResponse<AsylumCase> response = handler.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertEquals(0, response.getErrors().size());
    }

    private void setupCallbackAndCaseDetails(String directionDueDate) {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(SEND_DIRECTION_WITH_QUESTIONS);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(AsylumCaseFieldDefinition.SEND_DIRECTION_DATE_DUE, String.class))
                .thenReturn(Optional.of(directionDueDate));
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(
                () -> handler.handle(PreSubmitCallbackStage.MID_EVENT, callback))
                .hasMessage("Cannot handle callback")
                .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.SEND_DIRECTION);
        assertThatThrownBy(
                () -> handler.handle(PreSubmitCallbackStage.MID_EVENT, callback))
                .hasMessage("Cannot handle callback")
                .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = handler.canHandle(callbackStage, callback);

                if (event == SEND_DIRECTION_WITH_QUESTIONS
                        && callbackStage == PreSubmitCallbackStage.MID_EVENT) {

                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }

            reset(callback);
        }
    }
}