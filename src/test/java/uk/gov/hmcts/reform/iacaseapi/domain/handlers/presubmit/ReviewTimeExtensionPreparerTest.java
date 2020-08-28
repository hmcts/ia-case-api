package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.TimeExtensionStatus.IN_PROGRESS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.TimeExtensionStatus.SUBMITTED;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class ReviewTimeExtensionPreparerTest {

    @Mock private
    Callback<AsylumCase> callback;
    @Mock private
    CaseDetails<AsylumCase> caseDetails;
    @Mock private
    AsylumCase asylumCase;


    ReviewTimeExtensionPreparer reviewTimeExtensionPreparer;

    @BeforeEach
    void setUp() {

        reviewTimeExtensionPreparer = new ReviewTimeExtensionPreparer();
    }

    @Test
    void preparer_review_time_extension_fields() {
        IdValue<TimeExtension> extensionIdValue1 = new IdValue<>("1", new TimeExtension(null, "reasons1", State.APPEAL_SUBMITTED, IN_PROGRESS, emptyList()));
        IdValue<TimeExtension> extensionIdValue2 = new IdValue<>("2", new TimeExtension("date2", "reasons2", State.AWAITING_REASONS_FOR_APPEAL, SUBMITTED, emptyList()));
        List<IdValue<TimeExtension>> timeExtensions = asList(extensionIdValue1, extensionIdValue2);

        IdValue<PreviousDates> dateOne = new IdValue<>("1", new PreviousDates("2020-05-12", "2020-01-12"));
        IdValue<PreviousDates> dateTwo = new IdValue<>("2", new PreviousDates("2020-05-12", "2020-01-12"));
        List<IdValue<PreviousDates>> previousDates = asList(dateOne, dateTwo);

        IdValue<Direction> timeExtensionIdValue1 = new IdValue<>("1", new Direction("TestOne", Parties.APPELLANT, "2020-04-10", "2020-04-12", DirectionTag.REQUEST_REASONS_FOR_APPEAL, previousDates));
        IdValue<Direction> timeExtensionIdValue2 = new IdValue<>("2", new Direction("TestTwo", Parties.APPELLANT, "2020-04-16", "2020-04-14", DirectionTag.BUILD_CASE, previousDates));
        List<IdValue<Direction>> timeExtension = asList(timeExtensionIdValue1, timeExtensionIdValue2);

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.REVIEW_TIME_EXTENSION);
        when(caseDetails.getState()).thenReturn(State.AWAITING_REASONS_FOR_APPEAL);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(TIME_EXTENSIONS)).thenReturn(Optional.of(timeExtensions));
        when(asylumCase.read(DIRECTIONS)).thenReturn(Optional.of(timeExtension));

        reviewTimeExtensionPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        Mockito.verify(asylumCase).write(REVIEW_TIME_EXTENSION_DATE, "date2");
        Mockito.verify(asylumCase).write(REVIEW_TIME_EXTENSION_PARTY, Parties.APPELLANT);
        Mockito.verify(asylumCase).write(REVIEW_TIME_EXTENSION_REASON, "reasons2");
        Mockito.verify(asylumCase).write(REVIEW_TIME_EXTENSION_DECISION, null);
        Mockito.verify(asylumCase).write(REVIEW_TIME_EXTENSION_DECISION_REASON, "");
    }

    @Test
    void preparer_review_time_extension_fields_for_current_state() {
        IdValue<TimeExtension> extensionIdValue1 = new IdValue<>("1", new TimeExtension("date1", "reasons1", State.APPEAL_SUBMITTED, SUBMITTED, emptyList()));
        IdValue<TimeExtension> extensionIdValue2 = new IdValue<>("2", new TimeExtension("date2", "reasons2", State.AWAITING_REASONS_FOR_APPEAL, SUBMITTED, emptyList()));
        List<IdValue<TimeExtension>> timeExtensions = asList(extensionIdValue1, extensionIdValue2);

        IdValue<PreviousDates> dateOne = new IdValue<>("1", new PreviousDates("2020-05-12", "2020-01-12"));
        IdValue<PreviousDates> dateTwo = new IdValue<>("2", new PreviousDates("2020-05-12", "2020-01-12"));
        List<IdValue<PreviousDates>> previousDates = asList(dateOne, dateTwo);

        IdValue<Direction> timeExtensionIdValue1 = new IdValue<>("1", new Direction("TestOne", Parties.APPELLANT, "2020-04-10", "2020-04-12", DirectionTag.REQUEST_REASONS_FOR_APPEAL, previousDates));
        IdValue<Direction> timeExtensionIdValue2 = new IdValue<>("2", new Direction("TestTwo", Parties.APPELLANT, "2020-04-16", "2020-04-14", DirectionTag.BUILD_CASE, previousDates));
        List<IdValue<Direction>> timeExtension = asList(timeExtensionIdValue1, timeExtensionIdValue2);

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.REVIEW_TIME_EXTENSION);
        when(caseDetails.getState()).thenReturn(State.AWAITING_REASONS_FOR_APPEAL);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(TIME_EXTENSIONS)).thenReturn(Optional.of(timeExtensions));
        when(asylumCase.read(DIRECTIONS)).thenReturn(Optional.of(timeExtension));

        reviewTimeExtensionPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        Mockito.verify(asylumCase).write(REVIEW_TIME_EXTENSION_DATE, "date2");
        Mockito.verify(asylumCase).write(REVIEW_TIME_EXTENSION_PARTY, Parties.APPELLANT);
        Mockito.verify(asylumCase).write(REVIEW_TIME_EXTENSION_REASON, "reasons2");
    }

    @Test
    void gets_error_if_no_time_extension_request() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.REVIEW_TIME_EXTENSION);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(TIME_EXTENSIONS)).thenReturn(Optional.of(emptyList()));

        PreSubmitCallbackResponse<AsylumCase> handle = reviewTimeExtensionPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);
        Set<String> errors = handle.getErrors();
        assertEquals(errors, Stream.of("There is no time extension to review").collect(Collectors.toSet()));
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> reviewTimeExtensionPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.SEND_DIRECTION);
        assertThatThrownBy(() -> reviewTimeExtensionPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = reviewTimeExtensionPreparer.canHandle(callbackStage, callback);

                if (event == Event.REVIEW_TIME_EXTENSION
                    && callbackStage == PreSubmitCallbackStage.ABOUT_TO_START) {

                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }

            reset(callback);
        }
    }
}
