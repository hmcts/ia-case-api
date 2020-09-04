package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HOME_OFFICE_REFERENCE_NUMBER;

import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
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
    public void should_truncate_home_office_reference_numbers_for_submit_appeal() {

        Map<String, String> exampleInputOutputs =
            ImmutableMap.<String, String>builder()
                .put("A1234567", "A1234567")
                .put("A1234567/", "A1234567/")
                .put("A123456/001", "A123456")
                .put("A123456/1234567", "A123456/1234567")
                .put("A1234567/001", "A1234567")
                .put("A1234567/1234567", "A1234567/1234567")
                .put("A123456789/1234567", "A123456789/1234567")
                .build();

        exampleInputOutputs
            .entrySet()
            .forEach(inputOutput -> {

                final String input = inputOutput.getKey();
                final String output = inputOutput.getValue();

                when(callback.getCaseDetails()).thenReturn(caseDetails);
                when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
                when(caseDetails.getCaseData()).thenReturn(asylumCase);
                when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER)).thenReturn(Optional.of(input));

                PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                    homeOfficeReferenceNumberTruncator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

                assertNotNull(callbackResponse);
                assertEquals(asylumCase, callbackResponse.getData());
                verify(asylumCase, times(1)).write(HOME_OFFICE_REFERENCE_NUMBER, output);

                reset(asylumCase);
            });
    }

    @Test
    public void should_truncate_home_office_reference_numbers_for_pay_and_submit_appeal() {

        Map<String, String> exampleInputOutputs =
            ImmutableMap.<String, String>builder()
                .put("A1234567", "A1234567")
                .put("A1234567/", "A1234567/")
                .put("A123456/001", "A123456")
                .put("A123456/1234567", "A123456/1234567")
                .put("A1234567/001", "A1234567")
                .put("A1234567/1234567", "A1234567/1234567")
                .put("A123456789/1234567", "A123456789/1234567")
                .build();

        exampleInputOutputs
            .entrySet()
            .forEach(inputOutput -> {

                final String input = inputOutput.getKey();
                final String output = inputOutput.getValue();

                when(callback.getCaseDetails()).thenReturn(caseDetails);
                when(callback.getEvent()).thenReturn(Event.PAY_AND_SUBMIT_APPEAL);
                when(caseDetails.getCaseData()).thenReturn(asylumCase);
                when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER)).thenReturn(Optional.of(input));

                PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                    homeOfficeReferenceNumberTruncator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

                assertNotNull(callbackResponse);
                assertEquals(asylumCase, callbackResponse.getData());
                verify(asylumCase, times(1)).write(HOME_OFFICE_REFERENCE_NUMBER, output);

                reset(asylumCase);
            });
    }

    @Test
    public void should_not_touch_home_office_reference_numbers_that_are_not_too_long_for_submit_appeal() {

        Map<String, String> exampleInputOutputs =
            ImmutableMap
                .of("", "");

        exampleInputOutputs
            .entrySet()
            .forEach(inputOutput -> {

                final String input = inputOutput.getKey();
                final String output = inputOutput.getValue();

                when(callback.getCaseDetails()).thenReturn(caseDetails);
                when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
                when(caseDetails.getCaseData()).thenReturn(asylumCase);
                when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER)).thenReturn(Optional.of(input));

                PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                    homeOfficeReferenceNumberTruncator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

                assertNotNull(callbackResponse);
                assertEquals(asylumCase, callbackResponse.getData());
                verify(asylumCase, never()).write(HOME_OFFICE_REFERENCE_NUMBER, output);

                reset(asylumCase);
            });
    }

    @Test
    public void should_not_touch_home_office_reference_numbers_that_are_not_too_long_for_pay_and_submit_appeal() {

        Map<String, String> exampleInputOutputs =
            ImmutableMap
                .of("", "");

        exampleInputOutputs
            .entrySet()
            .forEach(inputOutput -> {

                final String input = inputOutput.getKey();
                final String output = inputOutput.getValue();

                when(callback.getCaseDetails()).thenReturn(caseDetails);
                when(callback.getEvent()).thenReturn(Event.PAY_AND_SUBMIT_APPEAL);
                when(caseDetails.getCaseData()).thenReturn(asylumCase);
                when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER)).thenReturn(Optional.of(input));

                PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                    homeOfficeReferenceNumberTruncator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

                assertNotNull(callbackResponse);
                assertEquals(asylumCase, callbackResponse.getData());
                verify(asylumCase, never()).write(HOME_OFFICE_REFERENCE_NUMBER, output);

                reset(asylumCase);
            });
    }

    @Test
    public void should_throw_when_home_office_reference_is_not_present_for_submit_appeal() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> homeOfficeReferenceNumberTruncator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("homeOfficeReferenceNumber is not present")
            .isExactlyInstanceOf(RequiredFieldMissingException.class);
    }

    @Test
    public void should_throw_when_home_office_reference_is_not_present_for_pay_and_submit_appeal() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.PAY_AND_SUBMIT_APPEAL);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER)).thenReturn(Optional.empty());

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
                when(callback.getCaseDetails()).thenReturn(caseDetails);
                when(caseDetails.getCaseData()).thenReturn(asylumCase);

                boolean canHandle = homeOfficeReferenceNumberTruncator.canHandle(callbackStage, callback);

                if (Arrays.asList(
                    Event.SUBMIT_APPEAL,
                    Event.PAY_AND_SUBMIT_APPEAL)
                        .contains(callback.getEvent())
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
