package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.DIRECTIONS;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;


@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class RequestCaseBuildingConfirmationTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;

    private RequestCaseBuildingConfirmation requestCaseBuildingConfirmation =
        new RequestCaseBuildingConfirmation();

    private static final String explanation = "Do the thing";
    private static final Parties partiesAppelant = Parties.APPELLANT;
    private static final Parties partiesLegalRep = Parties.LEGAL_REPRESENTATIVE;
    private static final String dateDue = "2018-12-31T12:34:56";
    private static final String dateSent = "2018-12-25";
    private static DirectionTag tag = DirectionTag.REQUEST_CASE_BUILDING;
    private static List<IdValue<PreviousDates>> previousDates = Collections.emptyList();
    private static List<IdValue<ClarifyingQuestion>> clarifyingQuestions = Collections.emptyList();
    private static final String uniqueId = UUID.randomUUID().toString();
    private static final String directionType = "requestCaseBuilding";

    private static Direction directionToAppelant = new Direction(
            explanation,
            partiesAppelant,
            dateDue,
            dateSent,
            tag,
            previousDates,
            Collections.emptyList(),
            UUID.randomUUID().toString(),
            directionType
    );

    private static Direction directionToLegalRep = new Direction(
            explanation,
            partiesLegalRep,
            dateDue,
            dateSent,
            tag,
            previousDates,
            clarifyingQuestions,
            uniqueId,
            directionType
    );

    @ParameterizedTest
    @MethodSource("getDirections")
    void should_return_confirmation(Direction direction) {

        when(callback.getEvent()).thenReturn(Event.REQUEST_CASE_BUILDING);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(DIRECTIONS)).thenReturn(Optional.of(List.of(new IdValue("1", direction))));
        final String personForNotification = direction.getParties() == Parties.APPELLANT
                ? "Appellant"
                : "Legal representative";

        PostSubmitCallbackResponse callbackResponse = requestCaseBuildingConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(
                callbackResponse.getConfirmationHeader().get()).contains("You have sent a direction");

        assertThat(
                callbackResponse.getConfirmationBody().get()).contains(personForNotification + " will be notified by email.");

    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> requestCaseBuildingConfirmation.handle(callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            boolean canHandle = requestCaseBuildingConfirmation.canHandle(callback);

            if (event == Event.REQUEST_CASE_BUILDING || event == Event.FORCE_REQUEST_CASE_BUILDING) {

                assertTrue(canHandle);
            } else {
                assertFalse(canHandle);
            }

            reset(callback);
        }
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> requestCaseBuildingConfirmation.canHandle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> requestCaseBuildingConfirmation.handle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    static Stream<Direction> getDirections() {
        return Stream.of(
                directionToAppelant, directionToLegalRep
        );
    }
}
