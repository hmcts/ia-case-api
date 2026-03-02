package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CCD_REFERENCE_NUMBER_FOR_DISPLAY;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.service.AppealReferenceNumberValidator;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@SuppressWarnings("unchecked")
class EditAppealRefNumberValidationMidEventTest {

    private static final String ARIA_APPEAL_REFERENCE_PAGE_ID = "appealReferenceNumber";
    private static final String VALID_APPEAL_REF = "HU/12345/2023";
    private static final String CCD_REF_NUMBER = "1234567890123456";

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private AppealReferenceNumberValidator appealReferenceNumberValidator;

    private EditAppealRefNumberValidationMidEvent editAppealRefNumberValidationMidEvent;

    @BeforeEach
    public void setUp() {
        editAppealRefNumberValidationMidEvent = new EditAppealRefNumberValidationMidEvent(appealReferenceNumberValidator);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
    }

    @Test
    void should_successfully_validate_aria_appeal_reference_number() {
        when(callback.getEvent()).thenReturn(Event.EDIT_APPEAL);
        when(callback.getPageId()).thenReturn(ARIA_APPEAL_REFERENCE_PAGE_ID);
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(VALID_APPEAL_REF));
        when(asylumCase.read(CCD_REFERENCE_NUMBER_FOR_DISPLAY, String.class)).thenReturn(Optional.of(CCD_REF_NUMBER));
        when(appealReferenceNumberValidator.validate(VALID_APPEAL_REF, CCD_REF_NUMBER)).thenReturn(Collections.emptyList());

        PreSubmitCallbackResponse<AsylumCase> response =
            editAppealRefNumberValidationMidEvent.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertThat(response).isNotNull();
        assertThat(response.getErrors()).isEmpty();
        verify(appealReferenceNumberValidator, times(1)).validate(VALID_APPEAL_REF, CCD_REF_NUMBER);
    }

    @Test
    void should_error_when_aria_appeal_reference_number_format_is_invalid() {
        String invalidAppealRef = "INVALID123";
        when(callback.getEvent()).thenReturn(Event.EDIT_APPEAL);
        when(callback.getPageId()).thenReturn(ARIA_APPEAL_REFERENCE_PAGE_ID);
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(invalidAppealRef));
        when(asylumCase.read(CCD_REFERENCE_NUMBER_FOR_DISPLAY, String.class)).thenReturn(Optional.of(CCD_REF_NUMBER));
        when(appealReferenceNumberValidator.validate(invalidAppealRef, CCD_REF_NUMBER))
            .thenReturn(Collections.singletonList("The reference number is in an incorrect format."));

        PreSubmitCallbackResponse<AsylumCase> response =
            editAppealRefNumberValidationMidEvent.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertThat(response).isNotNull();
        assertThat(response.getErrors()).hasSize(1);
        assertThat(response.getErrors()).contains("The reference number is in an incorrect format.");
        verify(appealReferenceNumberValidator, times(1)).validate(invalidAppealRef, CCD_REF_NUMBER);
    }

    @Test
    void should_error_when_aria_appeal_reference_number_already_exists() {
        when(callback.getEvent()).thenReturn(Event.EDIT_APPEAL);
        when(callback.getPageId()).thenReturn(ARIA_APPEAL_REFERENCE_PAGE_ID);
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(VALID_APPEAL_REF));
        when(asylumCase.read(CCD_REFERENCE_NUMBER_FOR_DISPLAY, String.class)).thenReturn(Optional.of(CCD_REF_NUMBER));
        when(appealReferenceNumberValidator.validate(VALID_APPEAL_REF, CCD_REF_NUMBER))
            .thenReturn(Collections.singletonList("The reference number already exists. Please enter a different reference number."));

        PreSubmitCallbackResponse<AsylumCase> response =
            editAppealRefNumberValidationMidEvent.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertThat(response).isNotNull();
        assertThat(response.getErrors()).hasSize(1);
        assertThat(response.getErrors()).contains("The reference number already exists. Please enter a different reference number.");
        verify(appealReferenceNumberValidator, times(1)).validate(VALID_APPEAL_REF, CCD_REF_NUMBER);
    }

    @Test
    void should_error_when_aria_appeal_reference_number_has_multiple_validation_errors() {
        String invalidAppealRef = "INVALID";
        when(callback.getEvent()).thenReturn(Event.EDIT_APPEAL);
        when(callback.getPageId()).thenReturn(ARIA_APPEAL_REFERENCE_PAGE_ID);
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(invalidAppealRef));
        when(asylumCase.read(CCD_REFERENCE_NUMBER_FOR_DISPLAY, String.class)).thenReturn(Optional.of(CCD_REF_NUMBER));
        when(appealReferenceNumberValidator.validate(invalidAppealRef, CCD_REF_NUMBER))
            .thenReturn(Arrays.asList(
                "The reference number is in an incorrect format.",
                "The reference number already exists. Please enter a different reference number."
            ));

        PreSubmitCallbackResponse<AsylumCase> response =
            editAppealRefNumberValidationMidEvent.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertThat(response).isNotNull();
        assertThat(response.getErrors()).hasSize(2);
        assertThat(response.getErrors()).contains("The reference number is in an incorrect format.");
        assertThat(response.getErrors()).contains("The reference number already exists. Please enter a different reference number.");
        verify(appealReferenceNumberValidator, times(1)).validate(invalidAppealRef, CCD_REF_NUMBER);
    }

    @Test
    void should_throw_exception_when_aria_appeal_reference_number_is_missing() {
        when(callback.getEvent()).thenReturn(Event.EDIT_APPEAL);
        when(callback.getPageId()).thenReturn(ARIA_APPEAL_REFERENCE_PAGE_ID);
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> editAppealRefNumberValidationMidEvent.handle(PreSubmitCallbackStage.MID_EVENT, callback))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("appealReferenceNumber is missing");
    }

    @Test
    void should_throw_exception_when_ccd_reference_number_is_missing() {
        when(callback.getEvent()).thenReturn(Event.EDIT_APPEAL);
        when(callback.getPageId()).thenReturn(ARIA_APPEAL_REFERENCE_PAGE_ID);
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(VALID_APPEAL_REF));
        when(asylumCase.read(CCD_REFERENCE_NUMBER_FOR_DISPLAY, String.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> editAppealRefNumberValidationMidEvent.handle(PreSubmitCallbackStage.MID_EVENT, callback))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("ccdReferenceNumber is missing");
    }

    @Test
    void should_only_handle_edit_appeal_event() {
        when(callback.getEvent()).thenReturn(Event.EDIT_APPEAL);
        when(callback.getPageId()).thenReturn(ARIA_APPEAL_REFERENCE_PAGE_ID);

        boolean canHandle = editAppealRefNumberValidationMidEvent.canHandle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertTrue(canHandle);
    }

    @Test
    void should_not_handle_start_appeal_event() {
        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        when(callback.getPageId()).thenReturn(ARIA_APPEAL_REFERENCE_PAGE_ID);

        boolean canHandle = editAppealRefNumberValidationMidEvent.canHandle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertFalse(canHandle);
    }

    @Test
    void should_not_handle_wrong_page_id() {
        when(callback.getEvent()).thenReturn(Event.EDIT_APPEAL);
        when(callback.getPageId()).thenReturn("wrongPageId");

        boolean canHandle = editAppealRefNumberValidationMidEvent.canHandle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertFalse(canHandle);
    }

    @Test
    void should_not_handle_wrong_callback_stage() {
        when(callback.getEvent()).thenReturn(Event.EDIT_APPEAL);
        when(callback.getPageId()).thenReturn(ARIA_APPEAL_REFERENCE_PAGE_ID);

        boolean canHandle = editAppealRefNumberValidationMidEvent.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertFalse(canHandle);
    }

    @Test
    void should_throw_exception_when_cannot_handle() {
        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        when(callback.getPageId()).thenReturn(ARIA_APPEAL_REFERENCE_PAGE_ID);

        assertThatThrownBy(() -> editAppealRefNumberValidationMidEvent.handle(PreSubmitCallbackStage.MID_EVENT, callback))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("Cannot handle callback");
    }

    @Test
    void handling_should_throw_if_callback_stage_is_null() {
        assertThatThrownBy(() -> editAppealRefNumberValidationMidEvent.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void handling_should_throw_if_callback_is_null() {
        assertThatThrownBy(() -> editAppealRefNumberValidationMidEvent.canHandle(PreSubmitCallbackStage.MID_EVENT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
