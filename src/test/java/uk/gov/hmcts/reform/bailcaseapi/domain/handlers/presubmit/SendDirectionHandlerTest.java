package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.presubmit;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.DATE_OF_COMPLIANCE;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.DIRECTIONS;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.LISTING_EVENT;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SEND_DIRECTION_DESCRIPTION;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SEND_DIRECTION_LIST;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.ListingEvent.INITIAL_LISTING;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.bailcaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.Direction;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ListingEvent;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.bailcaseapi.domain.service.Appender;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
public class SendDirectionHandlerTest {

    @Mock
    private Appender<Direction> directionAppender;
    @Mock private Callback<BailCase> callback;
    @Mock private CaseDetails<BailCase> caseDetails;
    @Mock private BailCase bailCase;
    @Mock private DateProvider dateProvider;
    @Mock private Direction existingDirection;
    @Mock private List allAppendedDirections;

    @Captor private ArgumentCaptor<List<IdValue<Direction>>> existingDirectionsCaptor;
    @Captor private ArgumentCaptor<Direction> newDirectionCaptor;

    private final List<Direction> existingDirections = singletonList(existingDirection);
    private final LocalDate now = LocalDate.now();
    private final String newDirectionDateOfCompliance = LocalDate.now().plusDays(1).toString();
    private final String newDirectionDescription = "some-description";
    private final String newDirectionRecipient = "some-recipient";
    private SendDirectionHandler sendDirectionHandler;

    private String callbackErrorMessage =
        "The date they must comply by must be a future date.";

    @BeforeEach
    public void setUp() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.SEND_BAIL_DIRECTION);
        when(caseDetails.getCaseData()).thenReturn(bailCase);

        when(dateProvider.now()).thenReturn(now);
        when(dateProvider.nowWithTime()).thenReturn(LocalDateTime.now());

        when(bailCase.read(DIRECTIONS)).thenReturn(Optional.of(existingDirections));
        when(bailCase.read(SEND_DIRECTION_DESCRIPTION, String.class)).thenReturn(Optional.of(newDirectionDescription));
        when(bailCase.read(SEND_DIRECTION_LIST, String.class)).thenReturn(Optional.of(newDirectionRecipient));
        when(bailCase.read(DATE_OF_COMPLIANCE, String.class)).thenReturn(Optional.of(newDirectionDateOfCompliance));


        when(directionAppender.append(any(Direction.class), anyList()))
            .thenReturn(allAppendedDirections);

        sendDirectionHandler =
            new SendDirectionHandler(
                directionAppender,
                dateProvider
            );
    }

    @Test
    void should_append_new_direction_to_existing_directions() {

        PreSubmitCallbackResponse<BailCase> callbackResponse =
            sendDirectionHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);


        verify(directionAppender, times(1)).append(
            newDirectionCaptor.capture(),
            existingDirectionsCaptor.capture());

        Direction capturedDirection = newDirectionCaptor.getValue();

        assertThat(capturedDirection.getSendDirectionDescription()).isEqualTo(newDirectionDescription);
        assertThat(capturedDirection.getSendDirectionList()).isEqualTo(newDirectionRecipient);
        assertThat(capturedDirection.getDateOfCompliance()).isEqualTo(newDirectionDateOfCompliance);
        assertThat(capturedDirection.getDateSent()).isEqualTo(now.toString());

        assertThat(existingDirectionsCaptor.getValue()).isEqualTo(existingDirections);

        verify(bailCase, times(1)).write(DIRECTIONS, allAppendedDirections);

        assertThat(callbackResponse.getData()).isEqualTo(callbackResponse.getData());
    }

    @Test
    void should_write_error_and_not_append_directions_if_date_not_future() {
        String yesterday = LocalDate.now().minusDays(1).toString();
        when(bailCase.read(DATE_OF_COMPLIANCE, String.class)).thenReturn(Optional.of(yesterday));

        PreSubmitCallbackResponse<BailCase> callbackResponse =
            sendDirectionHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);


        verify(bailCase, never()).write(DIRECTIONS, allAppendedDirections);

        final Set<String> errors = callbackResponse.getErrors();
        assertThat(errors).hasSize(1).containsOnly(callbackErrorMessage);
    }

    @Test
    void should_clear_fields_for_direction_being_sent() {

        PreSubmitCallbackResponse<BailCase> callbackResponse =
            sendDirectionHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        verify(bailCase, times(1)).clear(SEND_DIRECTION_DESCRIPTION);
        verify(bailCase, times(1)).clear(SEND_DIRECTION_LIST);
        verify(bailCase, times(1)).clear(DATE_OF_COMPLIANCE);
    }

    @Test
    void should_throw_when_direction_description_is_not_present() {

        when(bailCase.read(SEND_DIRECTION_DESCRIPTION, String.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sendDirectionHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("sendDirectionDescription is not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_throw_when_direction_recipient_is_not_present() {

        when(bailCase.read(SEND_DIRECTION_LIST, String.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sendDirectionHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("sendDirectionList is not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> sendDirectionHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPLICATION);
        assertThatThrownBy(() -> sendDirectionHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getCaseDetails()).thenReturn(caseDetails);
            when(caseDetails.getCaseData()).thenReturn(bailCase);
            when(callback.getEvent()).thenReturn(event);
            when(bailCase.read(LISTING_EVENT, ListingEvent.class)).thenReturn(Optional.of(INITIAL_LISTING));

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = sendDirectionHandler.canHandle(callbackStage, callback);

                assertThat(canHandle).isEqualTo(callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                                                    && (event.equals(Event.SEND_BAIL_DIRECTION)
                                                        || (event == Event.CASE_LISTING)));
            }

            reset(callback);
        }
    }

    @Test
    void should_not_handle_if_event_case_listing_and_not_initial_listing() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(bailCase);
        when(callback.getEvent()).thenReturn(Event.CASE_LISTING);
        when(bailCase.read(LISTING_EVENT, ListingEvent.class)).thenReturn(Optional.empty());

        boolean canHandle = sendDirectionHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);
        assertFalse(canHandle);
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> sendDirectionHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> sendDirectionHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> sendDirectionHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> sendDirectionHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
