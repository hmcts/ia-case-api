package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class ChangeDirectionDueDateHandlerTest {

    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;

    @Captor private ArgumentCaptor<List<IdValue<Direction>>> directionsCaptor;

    private ChangeDirectionDueDateHandler changeDirectionDueDateHandler;

    @Before
    public void setUp() {
        changeDirectionDueDateHandler =
            new ChangeDirectionDueDateHandler();
    }

    @Test
    public void should_copy_due_date_back_into_main_direction_fields_ignoring_other_changes() {

        final List<IdValue<Direction>> existingDirections =
            Arrays.asList(
                new IdValue<>("1", new Direction(
                    "explanation-1",
                    Parties.LEGAL_REPRESENTATIVE,
                    "2020-12-01",
                    "2019-12-01",
                    DirectionTag.LEGAL_REPRESENTATIVE_REVIEW
                )),
                new IdValue<>("2", new Direction(
                    "explanation-2",
                    Parties.RESPONDENT,
                    "2020-11-01",
                    "2019-11-01",
                    DirectionTag.RESPONDENT_REVIEW
                ))
            );

        final List<IdValue<EditableDirection>> editableDirections =
            Arrays.asList(
                new IdValue<>("1", new EditableDirection(
                    "some-other-explanation-1-that-should-be-ignored",
                    Parties.BOTH,
                    "2222-12-01"
                )),
                new IdValue<>("2", new EditableDirection(
                    "some-other-explanation-1-that-should-be-ignored",
                    Parties.BOTH,
                    "3333-11-01"
                ))
            );

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.CHANGE_DIRECTION_DUE_DATE);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.getDirections()).thenReturn(Optional.of(existingDirections));
        when(asylumCase.getEditableDirections()).thenReturn(Optional.of(editableDirections));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            changeDirectionDueDateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).setDirections(directionsCaptor.capture());

        List<IdValue<Direction>> actualDirections = directionsCaptor.getAllValues().get(0);

        assertEquals(
            existingDirections.size(),
            actualDirections.size()
        );

        assertEquals("1", actualDirections.get(0).getId());
        assertEquals("explanation-1", actualDirections.get(0).getValue().getExplanation());
        assertEquals(Parties.LEGAL_REPRESENTATIVE, actualDirections.get(0).getValue().getParties());
        assertEquals("2222-12-01", actualDirections.get(0).getValue().getDateDue());
        assertEquals("2019-12-01", actualDirections.get(0).getValue().getDateSent());
        assertEquals(DirectionTag.LEGAL_REPRESENTATIVE_REVIEW, actualDirections.get(0).getValue().getTag());

        assertEquals("2", actualDirections.get(1).getId());
        assertEquals("explanation-2", actualDirections.get(1).getValue().getExplanation());
        assertEquals(Parties.RESPONDENT, actualDirections.get(1).getValue().getParties());
        assertEquals("3333-11-01", actualDirections.get(1).getValue().getDateDue());
        assertEquals("2019-11-01", actualDirections.get(1).getValue().getDateSent());
        assertEquals(DirectionTag.RESPONDENT_REVIEW, actualDirections.get(1).getValue().getTag());
    }

    @Test
    public void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> changeDirectionDueDateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.SEND_DIRECTION);
        assertThatThrownBy(() -> changeDirectionDueDateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = changeDirectionDueDateHandler.canHandle(callbackStage, callback);

                if (event == Event.CHANGE_DIRECTION_DUE_DATE
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

        assertThatThrownBy(() -> changeDirectionDueDateHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> changeDirectionDueDateHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> changeDirectionDueDateHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> changeDirectionDueDateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
