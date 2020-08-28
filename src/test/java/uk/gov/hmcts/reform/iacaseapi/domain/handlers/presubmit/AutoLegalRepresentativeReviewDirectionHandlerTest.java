package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.DIRECTIONS;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
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

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class AutoLegalRepresentativeReviewDirectionHandlerTest {

    private static final int REVIEW_DUE_IN_DAYS = 5;

    @Mock private DateProvider dateProvider;
    @Mock private DirectionAppender directionAppender;
    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;

    @Captor private ArgumentCaptor<List<IdValue<Direction>>> existingDirectionsCaptor;

    private AutoLegalRepresentativeReviewDirectionHandler autoLegalRepresentativeReviewDirectionHandler;

    @BeforeEach
    void setUp() {

        autoLegalRepresentativeReviewDirectionHandler =
            new AutoLegalRepresentativeReviewDirectionHandler(
                REVIEW_DUE_IN_DAYS,
                dateProvider,
                directionAppender
            );
    }

    @Test
    void should_append_new_direction_to_existing_directions_for_the_case() {

        final List<IdValue<Direction>> existingDirections = new ArrayList<>();
        final List<IdValue<Direction>> allDirections = new ArrayList<>();

        final String expectedExplanationPart = "The Home Office has replied to your Appeal Skeleton Argument and evidence";
        final Parties expectedParties = Parties.LEGAL_REPRESENTATIVE;
        final String expectedDateDue = "2018-12-25";
        final DirectionTag expectedTag = DirectionTag.LEGAL_REPRESENTATIVE_REVIEW;

        when(dateProvider.now()).thenReturn(LocalDate.parse("2018-12-20"));
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.ADD_APPEAL_RESPONSE);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(DIRECTIONS)).thenReturn(Optional.of(existingDirections));
        when(directionAppender.append(
            eq(existingDirections),
            contains(expectedExplanationPart),
            eq(expectedParties),
            eq(expectedDateDue),
            eq(expectedTag)
        )).thenReturn(allDirections);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            autoLegalRepresentativeReviewDirectionHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(directionAppender, times(1)).append(
            eq(existingDirections),
            contains(expectedExplanationPart),
            eq(expectedParties),
            eq(expectedDateDue),
            eq(expectedTag)
        );

        verify(asylumCase, times(1)).write(DIRECTIONS, allDirections);
    }

    @Test
    void should_add_new_direction_to_the_case_when_no_directions_exist() {

        final List<IdValue<Direction>> allDirections = new ArrayList<>();

        final String expectedExplanationPart = "The Home Office has replied to your Appeal Skeleton Argument and evidence";
        final Parties expectedParties = Parties.LEGAL_REPRESENTATIVE;
        final String expectedDateDue = "2018-12-25";
        final DirectionTag expectedTag = DirectionTag.LEGAL_REPRESENTATIVE_REVIEW;

        when(dateProvider.now()).thenReturn(LocalDate.parse("2018-12-20"));
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.ADD_APPEAL_RESPONSE);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(DIRECTIONS)).thenReturn(Optional.empty());
        when(directionAppender.append(
            any(List.class),
            contains(expectedExplanationPart),
            eq(expectedParties),
            eq(expectedDateDue),
            eq(expectedTag)
        )).thenReturn(allDirections);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            autoLegalRepresentativeReviewDirectionHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(directionAppender, times(1)).append(
            existingDirectionsCaptor.capture(),
            contains(expectedExplanationPart),
            eq(expectedParties),
            eq(expectedDateDue),
            eq(expectedTag)
        );

        List<IdValue<Direction>> actualExistingDirections =
            existingDirectionsCaptor
                .getAllValues()
                .get(0);

        assertEquals(0, actualExistingDirections.size());

        verify(asylumCase, times(1)).write(DIRECTIONS, allDirections);
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> autoLegalRepresentativeReviewDirectionHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.SEND_DIRECTION);
        assertThatThrownBy(() -> autoLegalRepresentativeReviewDirectionHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = autoLegalRepresentativeReviewDirectionHandler.canHandle(callbackStage, callback);

                if (event == Event.ADD_APPEAL_RESPONSE
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

        assertThatThrownBy(() -> autoLegalRepresentativeReviewDirectionHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> autoLegalRepresentativeReviewDirectionHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> autoLegalRepresentativeReviewDirectionHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> autoLegalRepresentativeReviewDirectionHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
