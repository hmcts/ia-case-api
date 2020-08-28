package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
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
class RequestHearingRequirementsDirectionTest {

    private static final int HEARING_REQUIREMENTS_DUE_IN_DAYS = 5;

    @Mock private DateProvider dateProvider;
    @Mock private DirectionAppender directionAppender;
    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;

    @Captor private ArgumentCaptor<List<IdValue<Direction>>> existingDirectionsCaptor;

    private RequestHearingRequirementsDirectionHandler requestHearingRequirementsDirectionHandler;

    @BeforeEach
    void setUp() {

        requestHearingRequirementsDirectionHandler =
            new RequestHearingRequirementsDirectionHandler(
                HEARING_REQUIREMENTS_DUE_IN_DAYS,
                dateProvider,
                directionAppender
            );
    }

    @Test
    void can_handle_request_hearing_requirements_feature() {

        when(callback.getEvent()).thenReturn(Event.REQUEST_HEARING_REQUIREMENTS_FEATURE);

        requestHearingRequirementsDirectionHandler =
            new RequestHearingRequirementsDirectionHandler(
                HEARING_REQUIREMENTS_DUE_IN_DAYS,
                dateProvider,
                directionAppender
            );

        boolean canHandle = requestHearingRequirementsDirectionHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertTrue(canHandle);
    }

    @Test
    void should_append_new_direction_to_existing_directions_for_the_case() {

        final List<IdValue<Direction>> existingDirections = new ArrayList<>();
        final List<IdValue<Direction>> allDirections = new ArrayList<>();

        final String expectedExplanationPart = "Visit the online service and use the HMCTS reference to find the case. You'll be able to submit the hearing requirements using the Overview tab.";
        final Parties expectedParties = Parties.LEGAL_REPRESENTATIVE;
        final String expectedDateDue = "2018-12-25";
        final DirectionTag expectedTag = DirectionTag.LEGAL_REPRESENTATIVE_HEARING_REQUIREMENTS;

        when(dateProvider.now()).thenReturn(LocalDate.parse("2018-12-20"));
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.REQUEST_HEARING_REQUIREMENTS_FEATURE);
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
            requestHearingRequirementsDirectionHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

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

        final String expectedExplanationPart = "Visit the online service and use the HMCTS reference to find the case. You'll be able to submit the hearing requirements using the Overview tab.";
        final Parties expectedParties = Parties.LEGAL_REPRESENTATIVE;
        final String expectedDateDue = "2018-12-25";
        final DirectionTag expectedTag = DirectionTag.LEGAL_REPRESENTATIVE_HEARING_REQUIREMENTS;

        when(dateProvider.now()).thenReturn(LocalDate.parse("2018-12-20"));
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.REQUEST_HEARING_REQUIREMENTS_FEATURE);
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
            requestHearingRequirementsDirectionHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

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

        assertThatThrownBy(() -> requestHearingRequirementsDirectionHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.SEND_DIRECTION);
        assertThatThrownBy(() -> requestHearingRequirementsDirectionHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = requestHearingRequirementsDirectionHandler.canHandle(callbackStage, callback);

                if (event == Event.REQUEST_HEARING_REQUIREMENTS_FEATURE
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

        assertThatThrownBy(() -> requestHearingRequirementsDirectionHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> requestHearingRequirementsDirectionHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> requestHearingRequirementsDirectionHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> requestHearingRequirementsDirectionHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
