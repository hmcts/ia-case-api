package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DirectionAppender;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class SendDirectionWithQuestionsHandlerTest {

    @Mock private
    Callback<AsylumCase> callback;
    @Mock private
    CaseDetails<AsylumCase> caseDetails;
    @Mock private
    AsylumCase asylumCase;
    @Mock private
    DateProvider dateProvider;
    @Mock private
    DirectionAppender directionAppender;

    SendDirectionWithQuestionsHandler sendDirectionWithQuestionsHandler;

    @BeforeEach
    void setUp() {

        sendDirectionWithQuestionsHandler = new SendDirectionWithQuestionsHandler(dateProvider, directionAppender);
    }

    @Test
    void error_if_direction_due_date_is_today() {
        setupInvalidDirectionDueDate("2020-02-02");
    }

    @Test
    void error_if_direction_due_date_is_in_past() {
        setupInvalidDirectionDueDate("2020-02-01");
    }

    void setupInvalidDirectionDueDate(String directionDueDate) {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.SEND_DIRECTION_WITH_QUESTIONS);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(AsylumCaseFieldDefinition.SEND_DIRECTION_DATE_DUE, String.class)).thenReturn(Optional.of(directionDueDate));

        when(dateProvider.now()).thenReturn(LocalDate.parse("2020-02-02"));

        PreSubmitCallbackResponse<AsylumCase> response = sendDirectionWithQuestionsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertThat(response.getErrors(), is(new HashSet<>(singletonList("Direction due date must be in the future"))));
    }

    @Test
    void adds_direction_with_questions() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.SEND_DIRECTION_WITH_QUESTIONS);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(AsylumCaseFieldDefinition.SEND_DIRECTION_DATE_DUE, String.class)).thenReturn(Optional.of("2020-02-16"));
        IdValue originalDirection = new IdValue(
                "1",
                new Direction("explanation", Parties.LEGAL_REPRESENTATIVE, "2020-01-02", "2020-01-01", DirectionTag.BUILD_CASE, Collections.emptyList())
        );
        when(asylumCase.read(AsylumCaseFieldDefinition.DIRECTIONS)).thenReturn(Optional.of(singletonList(originalDirection)));

        when(dateProvider.now()).thenReturn(LocalDate.parse("2020-02-02"));

        List<IdValue<ClarifyingQuestion>> clarifyingQuestions = Arrays.asList(
                new IdValue<>("1", new ClarifyingQuestion("question 1")),
                new IdValue<>("2", new ClarifyingQuestion("question 2"))
        );
        IdValue directionWithQuestions = new IdValue(
                "2",
                new Direction(
                        "You need to answer some questions about your appeal.",
                        Parties.APPELLANT,
                        "2020-02-16",
                        "2020-02-02",
                        DirectionTag.REQUEST_CLARIFYING_QUESTIONS,
                        Collections.emptyList(),
                        clarifyingQuestions
                )
        );
        when(directionAppender.append(
                singletonList(originalDirection),
                "You need to answer some questions about your appeal.",
                Parties.APPELLANT,
                "2020-02-16",
                DirectionTag.REQUEST_CLARIFYING_QUESTIONS,
                clarifyingQuestions
        ))
                .thenReturn(Arrays.asList(directionWithQuestions, originalDirection));

        when(asylumCase.read(AsylumCaseFieldDefinition.SEND_DIRECTION_QUESTIONS)).thenReturn(Optional.of(clarifyingQuestions));

        PreSubmitCallbackResponse<AsylumCase> response = sendDirectionWithQuestionsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertThat(response.getErrors(), is(new HashSet<>()));

        verify(asylumCase).write(AsylumCaseFieldDefinition.DIRECTIONS, Arrays.asList(directionWithQuestions, originalDirection));
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> sendDirectionWithQuestionsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
                .hasMessage("Cannot handle callback")
                .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.SEND_DIRECTION);
        assertThatThrownBy(() -> sendDirectionWithQuestionsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
                .hasMessage("Cannot handle callback")
                .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = sendDirectionWithQuestionsHandler.canHandle(callbackStage, callback);

                if (event == Event.SEND_DIRECTION_WITH_QUESTIONS
                        && callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT) {

                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }

            reset(callback);
        }
    }
}
