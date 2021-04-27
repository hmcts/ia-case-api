package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.DIRECTIONS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.SEND_DIRECTION_DATE_DUE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.SEND_DIRECTION_EXPLANATION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.SEND_DIRECTION_PARTIES;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
import uk.gov.hmcts.reform.iacaseapi.domain.service.DirectionPartiesResolver;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DirectionTagResolver;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class DirectionHandlerTest {

    @Mock
    private DirectionAppender directionAppender;
    @Mock
    private DirectionPartiesResolver directionPartiesResolver;
    @Mock
    private DirectionTagResolver directionTagResolver;
    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;

    @Captor
    private ArgumentCaptor<List<IdValue<Direction>>> existingDirectionsCaptor;

    private DirectionHandler directionHandler;

    @BeforeEach
    public void setUp() {
        directionHandler =
            new DirectionHandler(
                directionAppender,
                directionPartiesResolver,
                directionTagResolver
            );
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {
        "SEND_DIRECTION", "REQUEST_CASE_EDIT", "REQUEST_RESPONDENT_EVIDENCE", "REQUEST_RESPONDENT_REVIEW",
        "REQUEST_CASE_BUILDING", "FORCE_REQUEST_CASE_BUILDING", "REQUEST_REASONS_FOR_APPEAL",
        "REQUEST_RESPONSE_REVIEW", "REQUEST_RESPONSE_AMEND"
    })
    void should_append_new_direction_to_existing_directions_for_the_case(Event event) {

        final List<IdValue<Direction>> existingDirections = new ArrayList<>();
        final List<IdValue<Direction>> allDirections = new ArrayList<>();

        final String expectedExplanation = "Do the thing";
        final Parties expectedParties = Parties.LEGAL_REPRESENTATIVE;
        final String expectedDateDue = "2018-12-25";
        final DirectionTag expectedDirectionTag = DirectionTag.NONE;

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(event);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(DIRECTIONS)).thenReturn(Optional.of(existingDirections));
        when(asylumCase.read(SEND_DIRECTION_EXPLANATION, String.class)).thenReturn(Optional.of(expectedExplanation));
        when(asylumCase.read(SEND_DIRECTION_DATE_DUE, String.class)).thenReturn(Optional.of(expectedDateDue));

        when(directionPartiesResolver.resolve(callback)).thenReturn(expectedParties);
        when(directionTagResolver.resolve(event)).thenReturn(expectedDirectionTag);
        when(directionAppender.append(
            asylumCase,
            existingDirections,
            expectedExplanation,
            expectedParties,
            expectedDateDue,
            expectedDirectionTag
        )).thenReturn(allDirections);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            directionHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).read(SEND_DIRECTION_EXPLANATION, String.class);
        verify(asylumCase, times(1)).read(SEND_DIRECTION_DATE_DUE, String.class);

        verify(directionPartiesResolver, times(1)).resolve(callback);
        verify(directionTagResolver, times(1)).resolve(event);
        verify(directionAppender, times(1)).append(
            asylumCase,
            existingDirections,
            expectedExplanation,
            expectedParties,
            expectedDateDue,
            expectedDirectionTag
        );

        verify(asylumCase, times(1)).write(DIRECTIONS, allDirections);

        verify(asylumCase, times(1)).clear(SEND_DIRECTION_EXPLANATION);
        verify(asylumCase, times(1)).clear(SEND_DIRECTION_PARTIES);
        verify(asylumCase, times(1)).clear(SEND_DIRECTION_DATE_DUE);
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {
        "SEND_DIRECTION", "REQUEST_CASE_EDIT", "REQUEST_RESPONDENT_EVIDENCE", "REQUEST_RESPONDENT_REVIEW",
        "REQUEST_CASE_BUILDING", "FORCE_REQUEST_CASE_BUILDING", "REQUEST_REASONS_FOR_APPEAL",
        "REQUEST_RESPONSE_REVIEW", "REQUEST_RESPONSE_AMEND"
    })
    void should_add_new_direction_to_the_case_when_no_directions_exist(Event event) {

        final List<IdValue<Direction>> allDirections = new ArrayList<>();

        final String expectedExplanation = "Do the thing";
        final Parties expectedParties = Parties.RESPONDENT;
        final String expectedDateDue = "2018-12-25";
        final DirectionTag expectedDirectionTag = DirectionTag.NONE;

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(event);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(DIRECTIONS)).thenReturn(Optional.empty());
        when(asylumCase.read(SEND_DIRECTION_EXPLANATION, String.class)).thenReturn(Optional.of(expectedExplanation));
        when(asylumCase.read(SEND_DIRECTION_DATE_DUE, String.class)).thenReturn(Optional.of(expectedDateDue));

        when(directionPartiesResolver.resolve(callback)).thenReturn(expectedParties);
        when(directionTagResolver.resolve(event)).thenReturn(expectedDirectionTag);
        when(directionAppender.append(
            eq(asylumCase),
            any(List.class),
            eq(expectedExplanation),
            eq(expectedParties),
            eq(expectedDateDue),
            eq(expectedDirectionTag)
        )).thenReturn(allDirections);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            directionHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).read(SEND_DIRECTION_DATE_DUE, String.class);
        verify(asylumCase, times(1)).read(SEND_DIRECTION_EXPLANATION, String.class);

        verify(directionPartiesResolver, times(1)).resolve(callback);
        verify(directionTagResolver, times(1)).resolve(event);
        verify(directionAppender, times(1)).append(
            eq(asylumCase),
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

        verify(asylumCase, times(1)).write(DIRECTIONS, allDirections);

        verify(asylumCase, times(1)).clear(SEND_DIRECTION_EXPLANATION);
        verify(asylumCase, times(1)).clear(SEND_DIRECTION_PARTIES);
        verify(asylumCase, times(1)).clear(SEND_DIRECTION_DATE_DUE);
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {
        "SEND_DIRECTION", "REQUEST_CASE_EDIT", "REQUEST_RESPONDENT_EVIDENCE", "REQUEST_RESPONDENT_REVIEW",
        "REQUEST_CASE_BUILDING", "FORCE_REQUEST_CASE_BUILDING", "REQUEST_REASONS_FOR_APPEAL",
        "REQUEST_RESPONSE_REVIEW", "REQUEST_RESPONSE_AMEND"
    })
    void should_throw_when_send_direction_explanation_is_not_present(Event event) {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(event);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(asylumCase.read(SEND_DIRECTION_EXPLANATION, String.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> directionHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("sendDirectionExplanation is not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {
        "SEND_DIRECTION", "REQUEST_CASE_EDIT", "REQUEST_RESPONDENT_EVIDENCE", "REQUEST_RESPONDENT_REVIEW",
        "REQUEST_CASE_BUILDING", "FORCE_REQUEST_CASE_BUILDING", "REQUEST_REASONS_FOR_APPEAL",
        "REQUEST_RESPONSE_REVIEW", "REQUEST_RESPONSE_AMEND"
    })
    void should_throw_when_send_direction_date_due_is_not_present(Event event) {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(event);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(asylumCase.read(SEND_DIRECTION_EXPLANATION, String.class)).thenReturn(Optional.of("Do the thing"));
        when(asylumCase.read(SEND_DIRECTION_DATE_DUE, String.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> directionHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("sendDirectionDateDue is not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> directionHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        assertThatThrownBy(() -> directionHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = directionHandler.canHandle(callbackStage, callback);

                if (callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    &&
                    Arrays.asList(
                        Event.SEND_DIRECTION,
                        Event.REQUEST_CASE_EDIT,
                        Event.REQUEST_RESPONDENT_EVIDENCE,
                        Event.REQUEST_RESPONDENT_REVIEW,
                        Event.REQUEST_CASE_BUILDING,
                        Event.FORCE_REQUEST_CASE_BUILDING,
                        Event.REQUEST_REASONS_FOR_APPEAL,
                        Event.REQUEST_RESPONSE_REVIEW,
                        Event.REQUEST_RESPONSE_AMEND
                    ).contains(event)) {

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

        assertThatThrownBy(() -> directionHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> directionHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> directionHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> directionHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
