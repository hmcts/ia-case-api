package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Direction;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DirectionTag;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Parties;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;


@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class SendDirectionConfirmationTest {

    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;

    private SendDirectionConfirmation sendDirectionConfirmation =
        new SendDirectionConfirmation();

    private IdValue originalDirection8 = new IdValue(
        "8",
        new Direction("explanation8", Parties.LEGAL_REPRESENTATIVE, "2020-01-02",
            "2020-01-01", DirectionTag.NONE, Collections.emptyList())
    );

    private IdValue originalDirection9 = new IdValue(
        "9",
        new Direction("explanation9", Parties.RESPONDENT, "2020-01-02",
            "2020-01-01", DirectionTag.NONE, Collections.emptyList())
    );

    private IdValue originalDirection10 = new IdValue(
        "10",
        new Direction("explanation10", Parties.LEGAL_REPRESENTATIVE, "2020-01-02",
            "2020-01-01", DirectionTag.RESPONDENT_REVIEW, Collections.emptyList())
    );

    private IdValue originalDirection11 = new IdValue(
        "11",
        new Direction("explanation11", Parties.RESPONDENT, "2020-01-02",
            "2020-01-01", DirectionTag.NONE, Collections.emptyList())
    );

    @Test
    void should_return_confirmation() {

        long caseId = 1234;

        when(callback.getEvent()).thenReturn(Event.SEND_DIRECTION);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getId()).thenReturn(caseId);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        PostSubmitCallbackResponse callbackResponse =
            sendDirectionConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(
            callbackResponse.getConfirmationHeader().get())
            .contains("sent a direction");

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains("What happens next");

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains(
                "[directions tab]"
                + "(/case/IA/Asylum/" + caseId + "#directions)"
            );
    }

    @Test
    void should_return_confirmation_for_legal_rep_direction() {

        long caseId = 1234;

        when(callback.getEvent()).thenReturn(Event.SEND_DIRECTION);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getId()).thenReturn(caseId);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getCaseDetails().getState()).thenReturn(State.AWAITING_RESPONDENT_EVIDENCE);

        List<IdValue<Direction>> directionList = new ArrayList<>();
        directionList.add(originalDirection8);
        when(asylumCase.read(AsylumCaseFieldDefinition.DIRECTIONS)).thenReturn(Optional.of((directionList)));

        PostSubmitCallbackResponse callbackResponse =
            sendDirectionConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(
            callbackResponse.getConfirmationHeader().get())
            .contains("sent a direction");

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains("What happens next");

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains(
                "[directions tab]"
                    + "(/case/IA/Asylum/" + caseId + "#directions)"
            );

    }

    @Test
    void should_return_right_direction_for_multiple_send_direction() {

        List<IdValue<Direction>> directionList = new ArrayList<>();
        directionList.add(originalDirection8);
        directionList.add(originalDirection9);
        directionList.add(originalDirection10);
        directionList.add(originalDirection11);
        when(asylumCase.read(AsylumCaseFieldDefinition.DIRECTIONS)).thenReturn(Optional.of((directionList)));

        Optional<Direction> selectedDirection = sendDirectionConfirmation.getLatestNonStandardRespondentDirection(asylumCase);

        assertTrue(selectedDirection.isPresent());
        assertEquals(Parties.RESPONDENT, selectedDirection.get().getParties());
        assertEquals("explanation11", selectedDirection.get().getExplanation());
    }

    @Test
    void should_return_failed_notification() {

        long caseId = 1234;

        when(callback.getEvent()).thenReturn(Event.SEND_DIRECTION);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getCaseDetails().getState()).thenReturn(State.AWAITING_RESPONDENT_EVIDENCE);
        when(asylumCase.read(AsylumCaseFieldDefinition.HOME_OFFICE_AMEND_BUNDLE_INSTRUCT_STATUS, String.class))
            .thenReturn(Optional.of("FAIL"));
        List<IdValue<Direction>> directionList = new ArrayList<>();
        directionList.add(originalDirection10);
        directionList.add(originalDirection11);
        when(asylumCase.read(AsylumCaseFieldDefinition.DIRECTIONS)).thenReturn(Optional.of((directionList)));

        PostSubmitCallbackResponse callbackResponse =
            sendDirectionConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertThat(callbackResponse.getConfirmationHeader()).isNotPresent();
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains("![Respondent notification failed confirmation]"
                           + "(https://raw.githubusercontent.com/hmcts/ia-appeal-frontend/master/app/assets/images/respondent_notification_failed.svg)"
            );

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains("#### Do this next");

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains("Contact the respondent to tell them what has changed, including any action they need to take.");
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> sendDirectionConfirmation.handle(callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            boolean canHandle = sendDirectionConfirmation.canHandle(callback);

            if (event == Event.SEND_DIRECTION) {

                assertTrue(canHandle);
            } else {
                assertFalse(canHandle);
            }

            reset(callback);
        }
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> sendDirectionConfirmation.canHandle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> sendDirectionConfirmation.handle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
