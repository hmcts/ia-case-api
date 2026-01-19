package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition.DIRECTIONS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition.BAIL_DIRECTION_LIST;

import java.util.List;
import java.util.Optional;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Direction;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Value;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;


@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class ChangeBailDirectionDueDatePreparerTest {

    @Mock
    private Callback<BailCase> callback;
    @Mock
    private CaseDetails<BailCase> caseDetails;
    @Mock
    private BailCase bailCase;

    @Captor
    private ArgumentCaptor<Object> directionsCaptor;
    @Captor
    private ArgumentCaptor<BailCaseFieldDefinition> bailExtractorCaptor;

    private String direction1 = "Direction 1";
    private String direction2 = "Direction 2";

    private uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.ChangeBailDirectionDueDatePreparer changeBailDirectionDueDatePreparer;

    @BeforeEach
    public void setUp() {
        changeBailDirectionDueDatePreparer =
            new uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.ChangeBailDirectionDueDatePreparer();
    }

    @Test
    void should_create_dynamic_list() {
        final List<IdValue<Direction>> existingDirections =
            Arrays.asList(
                new IdValue<>("1", new Direction(
                    "explanation-1",
                    "Legal representative",
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

        PreSubmitCallbackResponse<BailCase> callbackResponse =
            changeBailDirectionDueDatePreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertNotNull(callbackResponse);
        assertEquals(bailCase, callbackResponse.getData());

        verify(bailCase, times(1)).write(bailExtractorCaptor.capture(), directionsCaptor.capture());

        assertEquals(
            BAIL_DIRECTION_LIST,
            bailExtractorCaptor.getAllValues().get(0)
        );

        DynamicList expectedDynamicList = new DynamicList(new Value("Direction 2", "Direction 2"),
                                                          List.of(
                                                              new Value("Direction 2", "Direction 2"),
                                                              new Value("Direction 1", "Direction 1")));

        assertEquals(expectedDynamicList, directionsCaptor.getValue());
    }

    @Test
    void handling_should_return_error_when_direction_is_empty_list() {

        final List<IdValue<Direction>> existingDirections = emptyList();

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.CHANGE_BAIL_DIRECTION_DUE_DATE);
        when(caseDetails.getCaseData()).thenReturn(bailCase);
        when(bailCase.read(DIRECTIONS)).thenReturn(Optional.of(existingDirections));

        PreSubmitCallbackResponse<BailCase> callbackResponse =
            changeBailDirectionDueDatePreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertNotNull(callbackResponse);
        assertEquals(bailCase, callbackResponse.getData());
        assertEquals(1, callbackResponse.getErrors().size());
        assertTrue(callbackResponse.getErrors().contains("There is no direction to edit"));
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(
            () -> changeBailDirectionDueDatePreparer.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.SEND_BAIL_DIRECTION);
        assertThatThrownBy(() -> changeBailDirectionDueDatePreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = changeBailDirectionDueDatePreparer.canHandle(callbackStage, callback);

                if (event == Event.CHANGE_BAIL_DIRECTION_DUE_DATE
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
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> changeBailDirectionDueDatePreparer.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> changeBailDirectionDueDatePreparer.canHandle(PreSubmitCallbackStage.ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> changeBailDirectionDueDatePreparer.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> changeBailDirectionDueDatePreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
