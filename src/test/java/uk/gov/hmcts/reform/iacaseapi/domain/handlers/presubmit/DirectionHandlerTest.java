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
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.ADA_HEARING_REQUIREMENTS_SUBMITTED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.DIRECTIONS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HAS_TRANSFERRED_OUT_OF_ADA;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.SEND_DIRECTION_DATE_DUE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.SEND_DIRECTION_EXPLANATION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.SEND_DIRECTION_PARTIES;

import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
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
        "REQUEST_CASE_EDIT", "FORCE_REQUEST_CASE_BUILDING",
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
        when(asylumCase.read(HAS_TRANSFERRED_OUT_OF_ADA, YesOrNo.class)).thenReturn(Optional.empty());
        when(asylumCase.read(ADA_HEARING_REQUIREMENTS_SUBMITTED, YesOrNo.class)).thenReturn(Optional.empty());
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

        verify(directionAppender, times(1)).append(
                asylumCase,
                existingDirections,
                expectedExplanation,
                expectedParties,
                expectedDateDue,
                expectedDirectionTag
        );
        verifyCaseAfterHandling(event, allDirections);
    }

    private void verifyCaseAfterHandling(Event event, List<IdValue<Direction>> allDirections) {
        verify(asylumCase, times(1)).read(SEND_DIRECTION_DATE_DUE, String.class);
        verify(asylumCase, times(1)).read(SEND_DIRECTION_EXPLANATION, String.class);
        verify(directionPartiesResolver, times(1)).resolve(callback);
        verify(directionTagResolver, times(1)).resolve(event);
        verify(asylumCase, times(1)).write(DIRECTIONS, allDirections);
        verify(asylumCase, times(1)).clear(SEND_DIRECTION_EXPLANATION);
        verify(asylumCase, times(1)).clear(SEND_DIRECTION_PARTIES);
        verify(asylumCase, times(1)).clear(SEND_DIRECTION_DATE_DUE);
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {
        "SEND_DIRECTION", "REQUEST_RESPONDENT_EVIDENCE", "REQUEST_RESPONDENT_REVIEW",
        "REQUEST_CASE_BUILDING", "REQUEST_REASONS_FOR_APPEAL"
    })
    void should_append_new_direction_with_direction_type_to_existing_directions_for_the_case(Event event) {

        final List<IdValue<Direction>> existingDirections = new ArrayList<>();
        final List<IdValue<Direction>> allDirections = new ArrayList<>();

        final String expectedExplanation = "Do the thing";
        final Parties expectedParties = Parties.LEGAL_REPRESENTATIVE;
        final String expectedDateDue = "2018-12-25";
        final DirectionTag expectedDirectionTag = DirectionTag.NONE;

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(event);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(HAS_TRANSFERRED_OUT_OF_ADA, YesOrNo.class)).thenReturn(Optional.empty());
        when(asylumCase.read(ADA_HEARING_REQUIREMENTS_SUBMITTED, YesOrNo.class)).thenReturn(Optional.empty());
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
            expectedDirectionTag,
            event.toString()
        )).thenReturn(allDirections);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            directionHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(directionAppender, times(1)).append(
                asylumCase,
                existingDirections,
                expectedExplanation,
                expectedParties,
                expectedDateDue,
                expectedDirectionTag,
                event.toString()
        );
        verifyCaseAfterHandling(event, allDirections);
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {
        "REQUEST_CASE_EDIT", "FORCE_REQUEST_CASE_BUILDING",
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
        when(asylumCase.read(HAS_TRANSFERRED_OUT_OF_ADA, YesOrNo.class)).thenReturn(Optional.empty());
        when(asylumCase.read(ADA_HEARING_REQUIREMENTS_SUBMITTED, YesOrNo.class)).thenReturn(Optional.empty());
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
        verifyCaseAfterHandling(event, allDirections);
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {
        "SEND_DIRECTION", "REQUEST_RESPONDENT_EVIDENCE", "REQUEST_RESPONDENT_REVIEW",
        "REQUEST_CASE_BUILDING", "REQUEST_REASONS_FOR_APPEAL"
    })
    void should_add_new_direction_with_direction_type_to_the_case_when_no_directions_exist(Event event) {

        final List<IdValue<Direction>> allDirections = new ArrayList<>();

        final String expectedExplanation = "Do the thing";
        final Parties expectedParties = Parties.RESPONDENT;
        final String expectedDateDue = "2018-12-25";
        final DirectionTag expectedDirectionTag = DirectionTag.NONE;

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(event);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(HAS_TRANSFERRED_OUT_OF_ADA, YesOrNo.class)).thenReturn(Optional.empty());
        when(asylumCase.read(ADA_HEARING_REQUIREMENTS_SUBMITTED, YesOrNo.class)).thenReturn(Optional.empty());
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
            eq(expectedDirectionTag),
            eq(event.toString())
        )).thenReturn(allDirections);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            directionHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(directionAppender, times(1)).append(
                eq(asylumCase),
                existingDirectionsCaptor.capture(),
                eq(expectedExplanation),
                eq(expectedParties),
                eq(expectedDateDue),
                eq(expectedDirectionTag),
                eq(event.toString())
        );

        List<IdValue<Direction>> actualExistingDirections =
                existingDirectionsCaptor
                        .getAllValues()
                        .get(0);
        assertEquals(0, actualExistingDirections.size());

        verifyCaseAfterHandling(event, allDirections);
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
        when(asylumCase.read(HAS_TRANSFERRED_OUT_OF_ADA, YesOrNo.class)).thenReturn(Optional.empty());
        when(asylumCase.read(ADA_HEARING_REQUIREMENTS_SUBMITTED, YesOrNo.class)).thenReturn(Optional.empty());
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

        when(asylumCase.read(HAS_TRANSFERRED_OUT_OF_ADA, YesOrNo.class)).thenReturn(Optional.empty());
        when(asylumCase.read(ADA_HEARING_REQUIREMENTS_SUBMITTED, YesOrNo.class)).thenReturn(Optional.empty());
        when(asylumCase.read(SEND_DIRECTION_EXPLANATION, String.class)).thenReturn(Optional.of("Do the thing"));
        when(asylumCase.read(SEND_DIRECTION_DATE_DUE, String.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> directionHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("sendDirectionDateDue is not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(HAS_TRANSFERRED_OUT_OF_ADA, YesOrNo.class)).thenReturn(Optional.empty());
        when(asylumCase.read(ADA_HEARING_REQUIREMENTS_SUBMITTED, YesOrNo.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> directionHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        assertThatThrownBy(() -> directionHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    static Stream<Arguments> exAdaHearingReq() {
        return Stream.of(
            Arguments.of(YesOrNo.YES, YesOrNo.YES),
            Arguments.of(YesOrNo.YES, YesOrNo.NO),
            Arguments.of(YesOrNo.NO, YesOrNo.YES),
            Arguments.of(YesOrNo.NO, YesOrNo.NO)
            );
    }

    @ParameterizedTest
    @MethodSource("exAdaHearingReq")
    void it_can_handle_callback(YesOrNo isExAda, YesOrNo adaHearingReqSubmitted) {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);
            when(callback.getCaseDetails()).thenReturn(caseDetails);
            when(caseDetails.getCaseData()).thenReturn(asylumCase);
            when(asylumCase.read(HAS_TRANSFERRED_OUT_OF_ADA, YesOrNo.class)).thenReturn(Optional.of(isExAda));
            when(asylumCase.read(ADA_HEARING_REQUIREMENTS_SUBMITTED, YesOrNo.class)).thenReturn(Optional.of(adaHearingReqSubmitted));

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = directionHandler.canHandle(callbackStage, callback);

                Set<Event> eligibleEvents = Sets.newHashSet(Event.SEND_DIRECTION,
                    Event.REQUEST_CASE_EDIT,
                    Event.REQUEST_RESPONDENT_EVIDENCE,
                    Event.REQUEST_RESPONDENT_REVIEW,
                    Event.REQUEST_CASE_BUILDING,
                    Event.FORCE_REQUEST_CASE_BUILDING,
                    Event.REQUEST_REASONS_FOR_APPEAL,
                    Event.REQUEST_RESPONSE_AMEND);

                if (isExAda.equals(YesOrNo.NO) || adaHearingReqSubmitted.equals(YesOrNo.NO)) {
                    eligibleEvents.add(Event.REQUEST_RESPONSE_REVIEW);
                }

                if (callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && eligibleEvents.contains(event)) {

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
