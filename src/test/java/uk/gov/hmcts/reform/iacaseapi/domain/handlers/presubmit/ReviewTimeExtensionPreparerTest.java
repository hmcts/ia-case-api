package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hibernate.validator.internal.util.CollectionHelper.asSet;
import static org.junit.Assert.*;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.TimeExtensionStatus.IN_PROGRESS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.TimeExtensionStatus.SUBMITTED;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Parties;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.TimeExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class ReviewTimeExtensionPreparerTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;


    private ReviewTimeExtensionPreparer reviewTimeExtensionPreparer;

    @Before
    public void setup() {
        reviewTimeExtensionPreparer = new ReviewTimeExtensionPreparer();
    }

    @Test
    public void preparer_review_time_extension_fields() {
        IdValue<TimeExtension> extensionIdValue1 = new IdValue<>("1", new TimeExtension(null, "reasons1", State.APPEAL_SUBMITTED, IN_PROGRESS, emptyList()));
        IdValue<TimeExtension> extensionIdValue2 = new IdValue<>("2", new TimeExtension("date2", "reasons2", State.AWAITING_REASONS_FOR_APPEAL, SUBMITTED, emptyList()));
        List<IdValue<TimeExtension>> timeExtensions = asList(extensionIdValue1, extensionIdValue2);

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.REVIEW_TIME_EXTENSION);
        when(caseDetails.getState()).thenReturn(State.AWAITING_REASONS_FOR_APPEAL);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(TIME_EXTENSIONS)).thenReturn(Optional.of(timeExtensions));

        reviewTimeExtensionPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        Mockito.verify(asylumCase).write(REVIEW_TIME_EXTENSION_DATE, "date2");
        Mockito.verify(asylumCase).write(REVIEW_TIME_EXTENSION_PARTY, Parties.APPELLANT);
        Mockito.verify(asylumCase).write(REVIEW_TIME_EXTENSION_REASON, "reasons2");
    }

    @Test
    public void preparer_review_time_extension_fields_for_current_state() {
        IdValue<TimeExtension> extensionIdValue1 = new IdValue<>("1", new TimeExtension("date1", "reasons1", State.APPEAL_SUBMITTED, SUBMITTED, emptyList()));
        IdValue<TimeExtension> extensionIdValue2 = new IdValue<>("2", new TimeExtension("date2", "reasons2", State.AWAITING_REASONS_FOR_APPEAL, SUBMITTED, emptyList()));
        List<IdValue<TimeExtension>> timeExtensions = asList(extensionIdValue1, extensionIdValue2);

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.REVIEW_TIME_EXTENSION);
        when(caseDetails.getState()).thenReturn(State.AWAITING_REASONS_FOR_APPEAL);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(TIME_EXTENSIONS)).thenReturn(Optional.of(timeExtensions));

        reviewTimeExtensionPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        Mockito.verify(asylumCase).write(REVIEW_TIME_EXTENSION_DATE, "date2");
        Mockito.verify(asylumCase).write(REVIEW_TIME_EXTENSION_PARTY, Parties.APPELLANT);
        Mockito.verify(asylumCase).write(REVIEW_TIME_EXTENSION_REASON, "reasons2");
    }

    @Test
    public void gets_error_if_no_time_extension_request() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.REVIEW_TIME_EXTENSION);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(TIME_EXTENSIONS)).thenReturn(Optional.of(emptyList()));

        PreSubmitCallbackResponse<AsylumCase> handle = reviewTimeExtensionPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);
        Set<String> errors = handle.getErrors();
        assertEquals(errors, asSet("There is no time extension to review"));
    }

    @Test
    public void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> reviewTimeExtensionPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
                .hasMessage("Cannot handle callback")
                .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.SEND_DIRECTION);
        assertThatThrownBy(() -> reviewTimeExtensionPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
                .hasMessage("Cannot handle callback")
                .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void it_can_handle_callback() {

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
