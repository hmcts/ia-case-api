package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.DIRECTIONS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.DIRECTION_LIST;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.EDITABLE_DIRECTIONS;

import java.util.Arrays;
import java.util.Collections;
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
public class ChangeDirectionDueDatePreparerTest {

    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;

    @Captor private ArgumentCaptor<Object> editableDirectionsCaptor;
    @Captor private ArgumentCaptor<AsylumCaseFieldDefinition> asylumExtractorCaptor;

    private String direction1 = "Direction 1";
    private String direction2 = "Direction 2";

    private ChangeDirectionDueDatePreparer changeDirectionDueDatePreparer;

    @Before
    public void setUp() {
        changeDirectionDueDatePreparer =
            new ChangeDirectionDueDatePreparer();
    }

    @Test
    public void should_prepare_editable_direction_fields() {

        final List<IdValue<Direction>> existingDirections =
            Arrays.asList(
                new IdValue<>("1", new Direction(
                    "explanation-1",
                    Parties.LEGAL_REPRESENTATIVE,
                    "2020-12-01",
                    "2019-12-01",
                    DirectionTag.LEGAL_REPRESENTATIVE_REVIEW,
                    Collections.emptyList()
                )),
                new IdValue<>("2", new Direction(
                    "explanation-2",
                    Parties.RESPONDENT,
                    "2020-11-01",
                    "2019-11-01",
                    DirectionTag.RESPONDENT_REVIEW,
                    Collections.emptyList()
                ))
            );

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.CHANGE_DIRECTION_DUE_DATE);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(DIRECTIONS)).thenReturn(Optional.of(existingDirections));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            changeDirectionDueDatePreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(2)).write(asylumExtractorCaptor.capture(), editableDirectionsCaptor.capture());

        DynamicList dynamicList = (DynamicList) editableDirectionsCaptor.getAllValues().get(0);

        assertEquals(
            DIRECTION_LIST,
            asylumExtractorCaptor.getAllValues().get(0)
        );

        assertEquals(direction2, dynamicList.getValue().getCode());
        assertEquals(direction2, dynamicList.getValue().getLabel());

        assertEquals(2, dynamicList.getListItems().size());
        assertEquals(direction2, dynamicList.getListItems().get(0).getCode());
        assertEquals(direction2, dynamicList.getListItems().get(0).getLabel());
        assertEquals(direction1, dynamicList.getListItems().get(1).getCode());
        assertEquals(direction1, dynamicList.getListItems().get(1).getLabel());

        List<IdValue<EditableDirection>> actualEditableDirections = (List<IdValue<EditableDirection>>) editableDirectionsCaptor.getAllValues().get(1);

        assertEquals(
            EDITABLE_DIRECTIONS,
            asylumExtractorCaptor.getAllValues().get(1)
        );

        assertEquals(
            existingDirections.size(),
            actualEditableDirections.size()
        );

        assertEquals(existingDirections.get(0).getId(), actualEditableDirections.get(0).getId());
        assertEquals(existingDirections.get(0).getValue().getExplanation(), actualEditableDirections.get(0).getValue().getExplanation());
        assertEquals(existingDirections.get(0).getValue().getParties(), actualEditableDirections.get(0).getValue().getParties());
        assertEquals(existingDirections.get(0).getValue().getDateDue(), actualEditableDirections.get(0).getValue().getDateDue());

        assertEquals(existingDirections.get(1).getId(), actualEditableDirections.get(1).getId());
        assertEquals(existingDirections.get(1).getValue().getExplanation(), actualEditableDirections.get(1).getValue().getExplanation());
        assertEquals(existingDirections.get(1).getValue().getParties(), actualEditableDirections.get(1).getValue().getParties());
        assertEquals(existingDirections.get(1).getValue().getDateDue(), actualEditableDirections.get(1).getValue().getDateDue());
    }

    @Test
    public void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> changeDirectionDueDatePreparer.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.SEND_DIRECTION);
        assertThatThrownBy(() -> changeDirectionDueDatePreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = changeDirectionDueDatePreparer.canHandle(callbackStage, callback);

                if (event == Event.CHANGE_DIRECTION_DUE_DATE
                    && callbackStage == PreSubmitCallbackStage.ABOUT_TO_START) {

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

        assertThatThrownBy(() -> changeDirectionDueDatePreparer.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> changeDirectionDueDatePreparer.canHandle(PreSubmitCallbackStage.ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> changeDirectionDueDatePreparer.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> changeDirectionDueDatePreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
