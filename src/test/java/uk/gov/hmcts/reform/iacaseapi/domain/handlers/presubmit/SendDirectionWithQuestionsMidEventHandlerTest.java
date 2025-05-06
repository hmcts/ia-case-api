package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ClarifyingQuestion;
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

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.SEND_DIRECTION_WITH_QUESTIONS;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class SendDirectionWithQuestionsMidEventHandlerTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private DateProvider dateProvider;
    @Mock
    private DirectionAppender directionAppender;

    private SendDirectionWithQuestionsMidEventHandler handler;

    @BeforeEach
    public void setup() {
        handler = new SendDirectionWithQuestionsMidEventHandler(dateProvider, directionAppender);
    }

    @Test
    void error_if_direction_due_date_is_today() {
        setupInvalidDirectionDueDate("2020-02-02");
    }

    @Test
    void error_if_direction_due_date_is_in_past() {
        setupInvalidDirectionDueDate("2020-02-01");
    }

    private void setupInvalidDirectionDueDate(String directionDueDate) {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(SEND_DIRECTION_WITH_QUESTIONS);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(AsylumCaseFieldDefinition.SEND_DIRECTION_DATE_DUE, String.class))
                .thenReturn(Optional.of(directionDueDate));

        when(dateProvider.now()).thenReturn(LocalDate.parse("2020-02-02"));

        PreSubmitCallbackResponse<AsylumCase> response =
                handler.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertThat(response.getErrors()).containsExactlyInAnyOrderElementsOf(
                new HashSet<>(singletonList("Direction due date must be in the future")));
    }

    @Test
    void adds_direction_with_questions() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(SEND_DIRECTION_WITH_QUESTIONS);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(AsylumCaseFieldDefinition.SEND_DIRECTION_DATE_DUE, String.class))
                .thenReturn(Optional.of("2020-02-16"));
        IdValue originalDirection = new IdValue(
                "1",
                new Direction("explanation", Parties.LEGAL_REPRESENTATIVE, "2020-01-02", "2020-01-01",
                        DirectionTag.BUILD_CASE, Collections.emptyList(),
                        Collections.emptyList(),
                        UUID.randomUUID().toString(),
                        "directionType1"
                )
        );
        when(asylumCase.read(AsylumCaseFieldDefinition.DIRECTIONS))
                .thenReturn(Optional.of(singletonList(originalDirection)));

        when(dateProvider.now()).thenReturn(LocalDate.parse("2020-02-02"));

        List<IdValue<ClarifyingQuestion>> clarifyingQuestions = Arrays.asList(
                new IdValue<>("1", new ClarifyingQuestion("question 1")),
                new IdValue<>("2", new ClarifyingQuestion("question 2"))
        );
        String uniqueId = UUID.randomUUID().toString();
        String eventDirectionType = SEND_DIRECTION_WITH_QUESTIONS.toString();
        IdValue directionWithQuestions = new IdValue(
                "2",
                new Direction(
                        "You need to answer some questions about your appeal.",
                        Parties.APPELLANT,
                        "2020-02-16",
                        "2020-02-02",
                        DirectionTag.REQUEST_CLARIFYING_QUESTIONS,
                        Collections.emptyList(),
                        clarifyingQuestions,
                        uniqueId,
                        eventDirectionType
                )
        );
        when(directionAppender.append(
                asylumCase,
                singletonList(originalDirection),
                "You need to answer some questions about your appeal.",
                Parties.APPELLANT,
                "2020-02-16",
                DirectionTag.REQUEST_CLARIFYING_QUESTIONS,
                clarifyingQuestions,
                eventDirectionType
        ))
                .thenReturn(Arrays.asList(directionWithQuestions, originalDirection));

        when(asylumCase.read(AsylumCaseFieldDefinition.SEND_DIRECTION_QUESTIONS))
                .thenReturn(Optional.of(clarifyingQuestions));

        PreSubmitCallbackResponse<AsylumCase> response =
                handler.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertThat(response.getErrors().equals(new HashSet<>()));

        verify(asylumCase)
                .write(AsylumCaseFieldDefinition.DIRECTIONS, Arrays.asList(directionWithQuestions, originalDirection));
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(
                () -> handler.handle(PreSubmitCallbackStage.MID_EVENT, callback))
                .hasMessage("Cannot handle callback")
                .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.SEND_DIRECTION);
        assertThatThrownBy(
                () -> handler.handle(PreSubmitCallbackStage.MID_EVENT, callback))
                .hasMessage("Cannot handle callback")
                .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = handler.canHandle(callbackStage, callback);

                if (event == SEND_DIRECTION_WITH_QUESTIONS
                        && callbackStage == PreSubmitCallbackStage.MID_EVENT) {

                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }

            reset(callback);
        }
    }
}