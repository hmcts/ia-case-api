package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Direction;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DirectionTag;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Parties;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DirectionAppender;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class SendDirectionHandlerTest {

    @Mock private DirectionAppender directionAppender;
    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;

    @Captor private ArgumentCaptor<List<IdValue<Direction>>> existingDirectionsCaptor;

    private SendDirectionHandler sendDirectionHandler;

    @Before
    public void setUp() {
        sendDirectionHandler =
            new SendDirectionHandler(
                directionAppender
            );
    }

    @Test
    public void should_append_new_direction_to_existing_directions_for_the_case() {

        final List<IdValue<Direction>> existingDirections = new ArrayList<>();
        final List<IdValue<Direction>> allDirections = new ArrayList<>();

        final String expectedExplanation = "Do the thing";
        final Parties expectedParties = Parties.LEGAL_REPRESENTATIVE;
        final String expectedDateDue = "2018-12-25";
        final DirectionTag expectedDirectionTag = DirectionTag.NONE;

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.SEND_DIRECTION);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.getDirections()).thenReturn(Optional.of(existingDirections));
        when(asylumCase.getSendDirectionExplanation()).thenReturn(Optional.of(expectedExplanation));
        when(asylumCase.getSendDirectionParties()).thenReturn(Optional.of(expectedParties));
        when(asylumCase.getSendDirectionDateDue()).thenReturn(Optional.of(expectedDateDue));
        when(directionAppender.append(
            existingDirections,
            expectedExplanation,
            expectedParties,
            expectedDateDue,
            expectedDirectionTag
        )).thenReturn(allDirections);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            sendDirectionHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).getSendDirectionExplanation();
        verify(asylumCase, times(1)).getSendDirectionParties();
        verify(asylumCase, times(1)).getSendDirectionDateDue();

        verify(directionAppender, times(1)).append(
            existingDirections,
            expectedExplanation,
            expectedParties,
            expectedDateDue,
            expectedDirectionTag
        );

        verify(asylumCase, times(1)).setDirections(allDirections);

        verify(asylumCase, times(1)).clearSendDirectionExplanation();
        verify(asylumCase, times(1)).clearSendDirectionParties();
        verify(asylumCase, times(1)).clearSendDirectionDateDue();
    }

    @Test
    public void should_add_new_direction_to_the_case_when_no_directions_exist() {

        final List<IdValue<Direction>> allDirections = new ArrayList<>();

        final String expectedExplanation = "Do the thing";
        final Parties expectedParties = Parties.RESPONDENT;
        final String expectedDateDue = "2018-12-25";
        final DirectionTag expectedDirectionTag = DirectionTag.NONE;

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.SEND_DIRECTION);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.getDirections()).thenReturn(Optional.empty());
        when(asylumCase.getSendDirectionExplanation()).thenReturn(Optional.of(expectedExplanation));
        when(asylumCase.getSendDirectionParties()).thenReturn(Optional.of(expectedParties));
        when(asylumCase.getSendDirectionDateDue()).thenReturn(Optional.of(expectedDateDue));
        when(directionAppender.append(
            any(List.class),
            eq(expectedExplanation),
            eq(expectedParties),
            eq(expectedDateDue),
            eq(expectedDirectionTag)
        )).thenReturn(allDirections);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            sendDirectionHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).getSendDirectionExplanation();
        verify(asylumCase, times(1)).getSendDirectionParties();
        verify(asylumCase, times(1)).getSendDirectionDateDue();

        verify(directionAppender, times(1)).append(
            existingDirectionsCaptor.capture(),
            eq(expectedExplanation),
            eq(expectedParties),
            eq(expectedDateDue),
            eq(expectedDirectionTag)
        );

        List<IdValue<Direction>> actualExistingDirections =
            existingDirectionsCaptor
                .getAllValues()
                .get(0);

        assertEquals(0, actualExistingDirections.size());

        verify(asylumCase, times(1)).setDirections(allDirections);

        verify(asylumCase, times(1)).clearSendDirectionExplanation();
        verify(asylumCase, times(1)).clearSendDirectionParties();
        verify(asylumCase, times(1)).clearSendDirectionDateDue();
    }

    @Test
    public void should_throw_when_send_direction_explanation_is_not_present() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.SEND_DIRECTION);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(asylumCase.getSendDirectionExplanation()).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sendDirectionHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("sendDirectionExplanation is not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void should_throw_when_send_direction_parties_is_not_present() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.SEND_DIRECTION);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(asylumCase.getSendDirectionExplanation()).thenReturn(Optional.of("Do the thing"));
        when(asylumCase.getSendDirectionParties()).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sendDirectionHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("sendDirectionParties is not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void should_throw_when_send_direction_date_due_is_not_present() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.SEND_DIRECTION);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(asylumCase.getSendDirectionExplanation()).thenReturn(Optional.of("Do the thing"));
        when(asylumCase.getSendDirectionParties()).thenReturn(Optional.of(Parties.BOTH));
        when(asylumCase.getSendDirectionDateDue()).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sendDirectionHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("sendDirectionDateDue is not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> sendDirectionHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        assertThatThrownBy(() -> sendDirectionHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = sendDirectionHandler.canHandle(callbackStage, callback);

                if (event == Event.SEND_DIRECTION
                    && callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT) {

                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }

            reset(callback);
        }
    }

    @Test
    public void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> sendDirectionHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> sendDirectionHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> sendDirectionHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> sendDirectionHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
