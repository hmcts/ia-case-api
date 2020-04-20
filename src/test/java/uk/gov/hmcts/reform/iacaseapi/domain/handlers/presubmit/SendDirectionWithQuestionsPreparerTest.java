package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.SEND_DIRECTION_DATE_DUE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.SEND_DIRECTION_QUESTIONS;

import java.time.LocalDate;
import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class SendDirectionWithQuestionsPreparerTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private DateProvider dateProvider;

    private SendDirectionWithQuestionsPreparer sendDirectionWithQuestionsPreparer;

    @Before
    public void setup() {
        sendDirectionWithQuestionsPreparer = new SendDirectionWithQuestionsPreparer(dateProvider);
    }

    @Test
    public void preparer_review_time_extension_fields() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.SEND_DIRECTION_WITH_QUESTIONS);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(dateProvider.now()).thenReturn(LocalDate.parse("2020-01-01"));

        sendDirectionWithQuestionsPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        Mockito.verify(asylumCase).write(SEND_DIRECTION_DATE_DUE, "2020-01-29");
        Mockito.verify(asylumCase).write(SEND_DIRECTION_QUESTIONS, Collections.emptyList());
    }

    @Test
    public void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> sendDirectionWithQuestionsPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
                .hasMessage("Cannot handle callback")
                .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.SEND_DIRECTION);
        assertThatThrownBy(() -> sendDirectionWithQuestionsPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
                .hasMessage("Cannot handle callback")
                .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = sendDirectionWithQuestionsPreparer.canHandle(callbackStage, callback);

                if (event == Event.SEND_DIRECTION_WITH_QUESTIONS
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
