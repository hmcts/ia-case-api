package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Optional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacaseapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class HomeOfficeReferenceNumberTruncatorTest {

    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;

    private HomeOfficeReferenceNumberTruncator homeOfficeReferenceNumberTruncator =
        new HomeOfficeReferenceNumberTruncator();

    @Test
    public void should_truncate_home_office_reference_numbers() {

        Map<String, String> exampleInputOutputs =
            ImmutableMap
                .of("A1234567", "A123456",
                    "A123456/001", "A123456",
                    "A123456/1234567", "A123456");

        exampleInputOutputs
            .entrySet()
            .forEach(inputOutput -> {

                final String input = inputOutput.getKey();
                final String output = inputOutput.getValue();

                when(callback.getCaseDetails()).thenReturn(caseDetails);
                when(callback.getEvent()).thenReturn(Event.START_APPEAL);
                when(caseDetails.getCaseData()).thenReturn(asylumCase);
                when(asylumCase.getHomeOfficeReferenceNumber()).thenReturn(Optional.of(input));

                PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                    homeOfficeReferenceNumberTruncator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

                assertNotNull(callbackResponse);
                assertEquals(asylumCase, callbackResponse.getData());
                verify(asylumCase, times(1)).setHomeOfficeReferenceNumber(output);

                reset(asylumCase);
            });
    }

    @Test
    public void should_not_touch_home_office_reference_numbers_that_are_not_too_long() {

        Map<String, String> exampleInputOutputs =
            ImmutableMap
                .of("", "",
                    "A123", "A123",
                    "A123456", "A123456");

        exampleInputOutputs
            .entrySet()
            .forEach(inputOutput -> {

                final String input = inputOutput.getKey();
                final String output = inputOutput.getValue();

                when(callback.getCaseDetails()).thenReturn(caseDetails);
                when(callback.getEvent()).thenReturn(Event.EDIT_APPEAL);
                when(caseDetails.getCaseData()).thenReturn(asylumCase);
                when(asylumCase.getHomeOfficeReferenceNumber()).thenReturn(Optional.of(input));

                PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                    homeOfficeReferenceNumberTruncator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

                assertNotNull(callbackResponse);
                assertEquals(asylumCase, callbackResponse.getData());
                verify(asylumCase, never()).setHomeOfficeReferenceNumber(output);

                reset(asylumCase);
            });
    }

    @Test
    public void should_throw_when_home_office_reference_is_not_present() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.getHomeOfficeReferenceNumber()).thenReturn(Optional.empty());

        assertThatThrownBy(() -> homeOfficeReferenceNumberTruncator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("homeOfficeReferenceNumber is not present")
            .isExactlyInstanceOf(RequiredFieldMissingException.class);
    }

    @Test
    public void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> homeOfficeReferenceNumberTruncator.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.SEND_DIRECTION);
        assertThatThrownBy(() -> homeOfficeReferenceNumberTruncator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = homeOfficeReferenceNumberTruncator.canHandle(callbackStage, callback);

                if ((event == Event.START_APPEAL || event == Event.EDIT_APPEAL)
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
    public void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> homeOfficeReferenceNumberTruncator.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> homeOfficeReferenceNumberTruncator.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> homeOfficeReferenceNumberTruncator.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> homeOfficeReferenceNumberTruncator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
