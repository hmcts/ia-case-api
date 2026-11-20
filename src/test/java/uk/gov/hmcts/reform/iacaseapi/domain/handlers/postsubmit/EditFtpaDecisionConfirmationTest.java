package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;

@ExtendWith(MockitoExtension.class)
class EditFtpaDecisionConfirmationTest {

    @Mock
    private Callback<AsylumCase> callback;

    private final EditFtpaDecisionConfirmation confirmation = new EditFtpaDecisionConfirmation();

    @Test
    void should_return_confirmation() {
        when(callback.getEvent()).thenReturn(Event.UPDATE_FTPA_DECISION);

        PostSubmitCallbackResponse response = confirmation.handle(callback);

        assertThat(response.getConfirmationHeader()).contains("# You have updated the FTPA decision");
        assertThat(response.getConfirmationBody().orElseThrow()).contains("What happens next?");
    }

    @Test
    void should_only_handle_update_ftpa_decision() {
        for (Event event : Event.values()) {
            when(callback.getEvent()).thenReturn(event);
            assertThat(confirmation.canHandle(callback)).isEqualTo(event == Event.UPDATE_FTPA_DECISION);
        }
    }

    @Test
    void should_throw_if_cannot_handle() {
        when(callback.getEvent()).thenReturn(Event.START_APPEAL);

        assertThatThrownBy(() -> confirmation.handle(callback))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("Cannot handle callback");
    }

    @Test
    void should_not_allow_null_arguments() {
        assertThatThrownBy(() -> confirmation.canHandle(null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("callback must not be null");
    }
}
