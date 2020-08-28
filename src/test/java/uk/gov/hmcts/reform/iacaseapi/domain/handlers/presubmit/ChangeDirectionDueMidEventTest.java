package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class ChangeDirectionDueMidEventTest {

    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;

    @Captor ArgumentCaptor<Object> editableDirectionsCaptor;
    @Captor ArgumentCaptor<AsylumCaseFieldDefinition> asylumExtractorCaptor;

    String direction1 = "Direction 1";
    String direction2 = "Direction 2";

    ChangeDirectionDueMidEvent changeDirectionDueMidEvent;

    @BeforeEach
    void setUp() {

        changeDirectionDueMidEvent =
            new ChangeDirectionDueMidEvent();
    }

    @Test
    void should_perform_mid_event_for_editable_direction_fields() {

        List<IdValue<Direction>> existingDirections =
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
        when(asylumCase.read(DIRECTION_LIST, DynamicList.class)).thenReturn(Optional.of(new DynamicList(direction1)));

        changeDirectionDueMidEvent.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        verify(asylumCase, times(5)).write(asylumExtractorCaptor.capture(), editableDirectionsCaptor.capture());

        assertEquals(DIRECTION_EDIT_EXPLANATION, asylumExtractorCaptor.getAllValues().get(0));
        assertEquals(DIRECTION_EDIT_PARTIES, asylumExtractorCaptor.getAllValues().get(1));
        assertEquals(DIRECTION_EDIT_DATE_DUE, asylumExtractorCaptor.getAllValues().get(2));
        assertEquals(DIRECTION_EDIT_DATE_SENT, asylumExtractorCaptor.getAllValues().get(3));
        assertEquals(DIRECTION_LIST, asylumExtractorCaptor.getAllValues().get(4));

        assertEquals("explanation-2", editableDirectionsCaptor.getAllValues().get(0));
        assertEquals(Parties.RESPONDENT, editableDirectionsCaptor.getAllValues().get(1));
        assertEquals("2020-11-01", editableDirectionsCaptor.getAllValues().get(2));
        assertEquals("2019-11-01", editableDirectionsCaptor.getAllValues().get(3));

        DynamicList exactDirectionList = (DynamicList) editableDirectionsCaptor.getAllValues().get(4);
        assertEquals(direction1, exactDirectionList.getValue().getCode());
        assertEquals(direction1, exactDirectionList.getValue().getLabel());
        assertEquals(2, exactDirectionList.getListItems().size());
        assertEquals(direction2, exactDirectionList.getListItems().get(0).getCode());
        assertEquals(direction2, exactDirectionList.getListItems().get(0).getLabel());
        assertEquals(direction1, exactDirectionList.getListItems().get(1).getCode());
        assertEquals(direction1, exactDirectionList.getListItems().get(1).getLabel());
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> changeDirectionDueMidEvent.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        assertThatThrownBy(() -> changeDirectionDueMidEvent.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = changeDirectionDueMidEvent.canHandle(callbackStage, callback);

                if (event == Event.CHANGE_DIRECTION_DUE_DATE
                    && callbackStage == PreSubmitCallbackStage.MID_EVENT) {

                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }

            reset(callback);
        }
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> changeDirectionDueMidEvent.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> changeDirectionDueMidEvent.canHandle(PreSubmitCallbackStage.ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> changeDirectionDueMidEvent.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> changeDirectionDueMidEvent.handle(PreSubmitCallbackStage.ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}