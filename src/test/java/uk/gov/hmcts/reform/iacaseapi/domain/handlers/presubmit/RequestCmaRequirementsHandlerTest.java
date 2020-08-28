package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
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
class RequestCmaRequirementsHandlerTest {

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

    RequestCmaRequirementsHandler requestCmaRequirementsHandler;

    @BeforeEach
    void setUp() {

        requestCmaRequirementsHandler = new RequestCmaRequirementsHandler(dateProvider, directionAppender);
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
        when(callback.getEvent()).thenReturn(Event.REQUEST_CMA_REQUIREMENTS);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(AsylumCaseFieldDefinition.SEND_DIRECTION_DATE_DUE)).thenReturn(Optional.of(directionDueDate));

        when(dateProvider.now()).thenReturn(LocalDate.parse("2020-02-02"));

        PreSubmitCallbackResponse<AsylumCase> response = requestCmaRequirementsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertThat(response.getErrors(), is(new HashSet<>(singletonList("Direction due date must be in the future"))));
    }

    @Test
    void adds_direction_with_questions() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.REQUEST_CMA_REQUIREMENTS);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(AsylumCaseFieldDefinition.SEND_DIRECTION_DATE_DUE)).thenReturn(Optional.of("2020-02-16"));
        String cmaRequirementsReasons = "CMA requirements reasons";
        when(asylumCase.read(AsylumCaseFieldDefinition.REQUEST_CMA_REQUIREMENTS_REASONS)).thenReturn(Optional.of(cmaRequirementsReasons));
        IdValue originalDirection = new IdValue(
                "1",
                new Direction("explanation", Parties.APPELLANT, "2020-01-02", "2020-01-01", DirectionTag.BUILD_CASE, Collections.emptyList())
        );
        when(asylumCase.read(AsylumCaseFieldDefinition.DIRECTIONS)).thenReturn(Optional.of(singletonList(originalDirection)));

        when(dateProvider.now()).thenReturn(LocalDate.parse("2020-02-02"));


        IdValue requestCmaRequirements = new IdValue(
                "2",
                new Direction(
                        cmaRequirementsReasons,
                        Parties.APPELLANT,
                        "2020-02-16",
                        "2020-02-02",
                        DirectionTag.REQUEST_CMA_REQUIREMENTS,
                        Collections.emptyList()
                )
        );
        when(directionAppender.append(
                singletonList(originalDirection),
                "You need to attend a case management appointment. This is a meeting with a Tribunal "
                        + "Caseworker to talk about your appeal. A Home Office representative may also be at the meeting.\n\n"
                        + cmaRequirementsReasons,
                Parties.APPELLANT,
                "2020-02-16",
                DirectionTag.REQUEST_CMA_REQUIREMENTS
        ))
                .thenReturn(Arrays.asList(requestCmaRequirements, originalDirection));

        PreSubmitCallbackResponse<AsylumCase> response = requestCmaRequirementsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertThat(response.getErrors(), is(new HashSet<>()));

        verify(asylumCase).write(AsylumCaseFieldDefinition.DIRECTIONS, Arrays.asList(requestCmaRequirements, originalDirection));
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> requestCmaRequirementsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
                .hasMessage("Cannot handle callback")
                .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.SEND_DIRECTION);
        assertThatThrownBy(() -> requestCmaRequirementsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
                .hasMessage("Cannot handle callback")
                .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = requestCmaRequirementsHandler.canHandle(callbackStage, callback);

                if (event == Event.REQUEST_CMA_REQUIREMENTS
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
