package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.OutOfTimeDecisionType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;

@ExtendWith(MockitoExtension.class)
class RecordOutOfTimeDecisionConfirmationTest {

    @Mock private AsylumCase asylumCase;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private Callback<AsylumCase> callback;

    private RecordOutOfTimeDecisionConfirmation recordOutOfTimeDecisionConfirmation;

    @BeforeEach
    void setUp() {

        recordOutOfTimeDecisionConfirmation = new RecordOutOfTimeDecisionConfirmation();
    }

    @Test
    void should_throw_if_no_out_of_time_decision_type() {

        when(callback.getEvent()).thenReturn(Event.RECORD_OUT_OF_TIME_DECISION);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        assertThatThrownBy(() -> recordOutOfTimeDecisionConfirmation.handle(callback))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("Out of time decision is not present");
    }

    @ParameterizedTest
    @EnumSource(value = OutOfTimeDecisionType.class, names = { "IN_TIME", "APPROVED", "REJECTED" })
    void should_return_confirmation_message_for_out_of_time_decision_type(OutOfTimeDecisionType outOfTimeDecisionType) {

        when(callback.getEvent()).thenReturn(Event.RECORD_OUT_OF_TIME_DECISION);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(asylumCase.read(OUT_OF_TIME_DECISION_TYPE, OutOfTimeDecisionType.class))
            .thenReturn(Optional.of(outOfTimeDecisionType));

        PostSubmitCallbackResponse callbackResponse =
            recordOutOfTimeDecisionConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        switch (outOfTimeDecisionType) {
            case IN_TIME:
            case APPROVED:
                assertThat(callbackResponse.getConfirmationHeader().get())
                    .contains("You have recorded an out of time decision");
                assertThat(callbackResponse.getConfirmationBody().get())
                    .contains("This appeal will proceed as usual.");
                break;

            case REJECTED:
                assertThat(callbackResponse.getConfirmationHeader().get())
                    .contains("You have recorded that the appeal is out of time and cannot proceed");
                assertThat(callbackResponse.getConfirmationBody().get())
                    .contains("This appeal is out time and cannot proceed. "
                              + "You must [end the appeal](/case/IA/Asylum/"
                              + callback.getCaseDetails().getId() + "/trigger/endAppeal).");
                break;

            default:
                break;
        }
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> recordOutOfTimeDecisionConfirmation.handle(callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> recordOutOfTimeDecisionConfirmation.canHandle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> recordOutOfTimeDecisionConfirmation.handle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
