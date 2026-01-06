package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_REHYDRATED_APPEAL;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.MID_EVENT;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.service.AppealReferenceNumberValidator;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class RehydratedAppealReferenceNumberHandlerTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private AppealReferenceNumberValidator validator;

    private RehydratedAppealReferenceNumberHandler handler;

    @BeforeEach
    public void setUp() {
        handler = new RehydratedAppealReferenceNumberHandler(validator);

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
    }

    @Test
    void should_return_earliest_dispatch_priority() {
        assertEquals(DispatchPriority.EARLIEST, handler.getDispatchPriority());
    }

    @Test
    void it_can_handle_callback() {
        for (Event event : Event.values()) {
            when(callback.getEvent()).thenReturn(event);
            when(callback.getCaseDetails()).thenReturn(caseDetails);
            when(caseDetails.getCaseData()).thenReturn(asylumCase);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {
                when(asylumCase.read(IS_REHYDRATED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

                boolean canHandle = handler.canHandle(callbackStage, callback);

                if (callbackStage == ABOUT_TO_SUBMIT
                    && (event == Event.START_APPEAL || event == Event.EDIT_APPEAL)) {
                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }
            reset(callback);
        }
    }

    @Test
    void cannot_handle_callback_if_not_rehydrated_appeal() {
        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        when(asylumCase.read(IS_REHYDRATED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        boolean canHandle = handler.canHandle(ABOUT_TO_SUBMIT, callback);

        assertFalse(canHandle);
    }

    @Test
    void cannot_handle_callback_if_rehydrated_appeal_field_missing() {
        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        when(asylumCase.read(IS_REHYDRATED_APPEAL, YesOrNo.class)).thenReturn(Optional.empty());

        boolean canHandle = handler.canHandle(ABOUT_TO_SUBMIT, callback);

        assertFalse(canHandle);
    }

    @Test
    void should_successfully_validate_appeal_reference_number() {
        String validAppealReferenceNumber = "HU/12345/2024";
        when(asylumCase.read(IS_REHYDRATED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.of(validAppealReferenceNumber));
        when(validator.validate(validAppealReferenceNumber)).thenReturn(Collections.emptyList());

        PreSubmitCallbackResponse<AsylumCase> response = handler.handle(ABOUT_TO_SUBMIT, callback);

        assertNotNull(response);
        assertEquals(asylumCase, response.getData());
        assertThat(response.getErrors()).isEmpty();
        verify(validator, times(1)).validate(validAppealReferenceNumber);
    }

    @Test
    void should_add_error_when_appeal_reference_number_format_is_invalid() {
        String invalidAppealReferenceNumber = "INVALID/12345/2024";
        String formatError = "The reference number is in an incorrect format.";
        when(asylumCase.read(IS_REHYDRATED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.of(invalidAppealReferenceNumber));
        when(validator.validate(invalidAppealReferenceNumber))
            .thenReturn(Collections.singletonList(formatError));

        PreSubmitCallbackResponse<AsylumCase> response = handler.handle(ABOUT_TO_SUBMIT, callback);

        assertNotNull(response);
        assertEquals(asylumCase, response.getData());
        assertThat(response.getErrors()).hasSize(1).containsOnly(formatError);
        verify(validator, times(1)).validate(invalidAppealReferenceNumber);
    }

    @Test
    void should_add_error_when_appeal_reference_number_already_exists() {
        String existingAppealReferenceNumber = "HU/12345/2024";
        String alreadyExistsError = "The reference number already exists. Please enter a different reference number.";
        when(asylumCase.read(IS_REHYDRATED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.of(existingAppealReferenceNumber));
        when(validator.validate(existingAppealReferenceNumber))
            .thenReturn(Collections.singletonList(alreadyExistsError));

        PreSubmitCallbackResponse<AsylumCase> response = handler.handle(ABOUT_TO_SUBMIT, callback);

        assertNotNull(response);
        assertEquals(asylumCase, response.getData());
        assertThat(response.getErrors()).hasSize(1).containsOnly(alreadyExistsError);
        verify(validator, times(1)).validate(existingAppealReferenceNumber);
    }

    @Test
    void should_add_multiple_errors_when_multiple_validation_errors_exist() {
        String invalidAppealReferenceNumber = "INVALID/12345/2024";
        String formatError = "The reference number is in an incorrect format.";
        String alreadyExistsError = "The reference number already exists. Please enter a different reference number.";
        List<String> validationErrors = Arrays.asList(formatError, alreadyExistsError);
        when(asylumCase.read(IS_REHYDRATED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.of(invalidAppealReferenceNumber));
        when(validator.validate(invalidAppealReferenceNumber)).thenReturn(validationErrors);

        PreSubmitCallbackResponse<AsylumCase> response = handler.handle(ABOUT_TO_SUBMIT, callback);

        assertNotNull(response);
        assertEquals(asylumCase, response.getData());
        assertThat(response.getErrors()).hasSize(2).containsAll(validationErrors);
        verify(validator, times(1)).validate(invalidAppealReferenceNumber);
    }

    @Test
    void should_throw_exception_when_appeal_reference_number_is_missing() {
        when(asylumCase.read(IS_REHYDRATED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.handle(ABOUT_TO_SUBMIT, callback))
            .hasMessage("appealReferenceNumber is missing")
            .isExactlyInstanceOf(IllegalStateException.class);

        verify(validator, never()).validate(null);
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {
        when(asylumCase.read(IS_REHYDRATED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        assertThatThrownBy(() -> handler.handle(ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        assertThatThrownBy(() -> handler.handle(MID_EVENT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_not_allow_null_arguments() {
        assertThatThrownBy(() -> handler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> handler.canHandle(ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {"START_APPEAL", "EDIT_APPEAL"})
    void should_validate_appeal_reference_number_for_applicable_events(Event event) {
        String validAppealReferenceNumber = "PA/10234/2025";
        when(callback.getEvent()).thenReturn(event);
        when(asylumCase.read(IS_REHYDRATED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.of(validAppealReferenceNumber));
        when(validator.validate(validAppealReferenceNumber)).thenReturn(Collections.emptyList());

        PreSubmitCallbackResponse<AsylumCase> response = handler.handle(ABOUT_TO_SUBMIT, callback);

        assertNotNull(response);
        assertEquals(asylumCase, response.getData());
        assertThat(response.getErrors()).isEmpty();
        verify(validator, times(1)).validate(validAppealReferenceNumber);
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {"START_APPEAL", "EDIT_APPEAL"})
    void should_handle_validation_errors_for_applicable_events(Event event) {
        String invalidAppealReferenceNumber = "XX/12345/2024";
        String formatError = "The reference number is in an incorrect format.";
        when(callback.getEvent()).thenReturn(event);
        when(asylumCase.read(IS_REHYDRATED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.of(invalidAppealReferenceNumber));
        when(validator.validate(invalidAppealReferenceNumber))
            .thenReturn(Collections.singletonList(formatError));

        PreSubmitCallbackResponse<AsylumCase> response = handler.handle(ABOUT_TO_SUBMIT, callback);

        assertNotNull(response);
        assertEquals(asylumCase, response.getData());
        assertThat(response.getErrors()).hasSize(1).containsOnly(formatError);
        verify(validator, times(1)).validate(invalidAppealReferenceNumber);
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {"START_APPEAL", "EDIT_APPEAL"})
    void should_throw_exception_when_appeal_reference_missing_for_applicable_events(Event event) {
        when(callback.getEvent()).thenReturn(event);
        when(asylumCase.read(IS_REHYDRATED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.handle(ABOUT_TO_SUBMIT, callback))
            .hasMessage("appealReferenceNumber is missing")
            .isExactlyInstanceOf(IllegalStateException.class);

        verify(validator, never()).validate(null);
    }
}
