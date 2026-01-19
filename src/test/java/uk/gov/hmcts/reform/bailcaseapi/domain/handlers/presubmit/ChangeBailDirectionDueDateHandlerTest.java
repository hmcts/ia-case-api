package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition.DIRECTIONS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition.BAIL_DIRECTION_LIST;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition.BAIL_DIRECTION_EDIT_DATE_DUE;

import java.time.LocalDate;
import java.util.List;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Direction;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.PreviousDates;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Value;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class ChangeBailDirectionDueDateHandlerTest {

    @Mock
    private DateProvider dateProvider;
    @Mock
    private Callback<BailCase> callback;
    @Mock
    private CaseDetails<BailCase> caseDetails;
    @Mock
    private BailCase bailCase;

    @Captor
    private ArgumentCaptor<List<IdValue<Direction>>> bailValueCaptor;
    @Captor
    private ArgumentCaptor<BailCaseFieldDefinition> bailExtractorCaptor;

    private LocalDate dateSent = LocalDate.now();

    private uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.ChangeBailDirectionDueDateHandler changeBailDirectionDueDateHandler;

    @BeforeEach
    public void setUp() {
        when(dateProvider.now()).thenReturn(dateSent);

        changeBailDirectionDueDateHandler =
            new uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.ChangeBailDirectionDueDateHandler(dateProvider);
    }

    @Test
    void should_add_previous_dates_to_changed_direction() {

        List<IdValue<Direction>> existingDirections =
            Arrays.asList(
                new IdValue<>("1", new Direction(
                    "explanation-1",
                    "Applicant",
                    "2020-12-01",
                    "2019-12-01",
                    "",
                    "",
                    Collections.emptyList()
                )),
                new IdValue<>("2", new Direction(
                    "explanation-2",
                    "Home Office",
                    "2020-11-01",
                    "2019-11-01",
                    "",
                    "",
                    Collections.emptyList()
                ))
            );

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.CHANGE_BAIL_DIRECTION_DUE_DATE);
        when(caseDetails.getCaseData()).thenReturn(bailCase);
        when(bailCase.read(DIRECTIONS)).thenReturn(Optional.of(existingDirections));

        // Direction 2 is selected to be changed (first field of DynamicList)
        DynamicList dynamicList = new DynamicList(new Value("Direction 2", "Direction 2"),
                                                  List.of(
                                                      new Value("Direction 2", "Direction 2"),
                                                      new Value("Direction 1", "Direction 1")));


        // "Direction 1" in UI is equivalent of Direction with IdValue "2" in backend
        when(bailCase.read(BAIL_DIRECTION_LIST, DynamicList.class)).thenReturn(Optional.of(dynamicList));
        when(bailCase.read(BAIL_DIRECTION_EDIT_DATE_DUE, String.class)).thenReturn(Optional.of("2022-12-01"));

        PreSubmitCallbackResponse<BailCase> callbackResponse =
            changeBailDirectionDueDateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(bailCase, callbackResponse.getData());

        verify(bailCase).clear(BAIL_DIRECTION_LIST);
        verify(bailCase, times(1)).write(eq(DIRECTIONS), bailValueCaptor.capture());

        List<IdValue<Direction>> editedDirectionsCollection = bailValueCaptor.getAllValues().get(0);
        assertEquals(existingDirections.size(), editedDirectionsCollection.size());

        // "Direction 2" in UI is equivalent of Direction with IdValue "1" in backend
        // This direction's date has NOT been edited
        assertEquals("1", editedDirectionsCollection.get(0).getId());
        assertEquals("explanation-1", editedDirectionsCollection.get(0).getValue().getSendDirectionDescription());
        assertEquals("Applicant", editedDirectionsCollection.get(0).getValue().getSendDirectionList());
        assertEquals("2020-12-01", editedDirectionsCollection.get(0).getValue().getDateOfCompliance());
        assertEquals("2019-12-01", editedDirectionsCollection.get(0).getValue().getDateSent());
        assertEquals(0, editedDirectionsCollection.get(0).getValue().getPreviousDates().size());

        // "Direction 1" in UI is equivalent of Direction with IdValue "2" in backend
        // This direction's date has been edited
        assertEquals("2", editedDirectionsCollection.get(1).getId());
        assertEquals("explanation-2", editedDirectionsCollection.get(1).getValue().getSendDirectionDescription());
        assertEquals("Home Office", editedDirectionsCollection.get(1).getValue().getSendDirectionList());
        assertEquals("2022-12-01", editedDirectionsCollection.get(1).getValue().getDateOfCompliance()); // edited
        assertEquals(dateSent.toString(), editedDirectionsCollection.get(1).getValue().getDateSent());
        assertEquals(1, editedDirectionsCollection.get(1).getValue().getPreviousDates().size());
        assertEquals("2020-11-01", editedDirectionsCollection.get(1).getValue().getPreviousDates().get(0)
            .getValue().getDateDue());
    }

    @Test
    void should_add_to_previous_dates_if_direction_already_been_edited() {

        List<IdValue<Direction>> existingDirections =
            Arrays.asList(
                new IdValue<>("1", new Direction(
                    "explanation-1",
                    "Applicant",
                    "2020-12-01",
                    "2019-12-01",
                    "",
                    "",
                    Collections.emptyList()
                )),
                new IdValue<>("2", new Direction(
                    "explanation-2",
                    "Home Office",
                    "2022-11-01",
                    "2019-11-01",
                    "",
                    "",
                    List.of(new IdValue("1", new PreviousDates("2020-11-01", "2019-11-01")))
                ))
            );

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.CHANGE_BAIL_DIRECTION_DUE_DATE);
        when(caseDetails.getCaseData()).thenReturn(bailCase);
        when(bailCase.read(DIRECTIONS)).thenReturn(Optional.of(existingDirections));

        // Direction 2 is selected to be changed (first field of DynamicList)
        DynamicList dynamicList = new DynamicList(new Value("Direction 2", "Direction 2"),
                                                  List.of(
                                                      new Value("Direction 2", "Direction 2"),
                                                      new Value("Direction 1", "Direction 1")));


        // "Direction 1" in UI is equivalent of Direction with IdValue "2" in backend
        when(bailCase.read(BAIL_DIRECTION_LIST, DynamicList.class)).thenReturn(Optional.of(dynamicList));
        when(bailCase.read(BAIL_DIRECTION_EDIT_DATE_DUE, String.class)).thenReturn(Optional.of("2023-12-01"));

        PreSubmitCallbackResponse<BailCase> callbackResponse =
            changeBailDirectionDueDateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(bailCase, callbackResponse.getData());

        verify(bailCase).clear(BAIL_DIRECTION_LIST);
        verify(bailCase, times(1)).write(eq(DIRECTIONS), bailValueCaptor.capture());

        List<IdValue<Direction>> editedDirectionsCollection = bailValueCaptor.getAllValues().get(0);
        assertEquals(existingDirections.size(), editedDirectionsCollection.size());

        // "Direction 2" in UI is equivalent of Direction with IdValue "1" in backend
        // This direction's date has NOT been edited
        assertEquals("1", editedDirectionsCollection.get(0).getId());
        assertEquals("explanation-1", editedDirectionsCollection.get(0).getValue().getSendDirectionDescription());
        assertEquals("Applicant", editedDirectionsCollection.get(0).getValue().getSendDirectionList());
        assertEquals("2020-12-01", editedDirectionsCollection.get(0).getValue().getDateOfCompliance());
        assertEquals("2019-12-01", editedDirectionsCollection.get(0).getValue().getDateSent());
        assertEquals(0, editedDirectionsCollection.get(0).getValue().getPreviousDates().size());

        // "Direction 1" in UI is equivalent of Direction with IdValue "2" in backend
        // This direction's date has been edited
        assertEquals("2", editedDirectionsCollection.get(1).getId());
        assertEquals("explanation-2", editedDirectionsCollection.get(1).getValue().getSendDirectionDescription());
        assertEquals("Home Office", editedDirectionsCollection.get(1).getValue().getSendDirectionList());
        assertEquals("2023-12-01", editedDirectionsCollection.get(1).getValue().getDateOfCompliance()); // edited
        assertEquals(dateSent.toString(), editedDirectionsCollection.get(1).getValue().getDateSent());
        assertEquals(2, editedDirectionsCollection.get(1).getValue().getPreviousDates().size());
        assertEquals("2022-11-01", editedDirectionsCollection.get(1).getValue().getPreviousDates().get(0)
            .getValue().getDateDue());
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> changeBailDirectionDueDateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.SEND_BAIL_DIRECTION);
        assertThatThrownBy(() -> changeBailDirectionDueDateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = changeBailDirectionDueDateHandler.canHandle(callbackStage, callback);

                if (event == Event.CHANGE_BAIL_DIRECTION_DUE_DATE
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
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> changeBailDirectionDueDateHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> changeBailDirectionDueDateHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> changeBailDirectionDueDateHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> changeBailDirectionDueDateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
