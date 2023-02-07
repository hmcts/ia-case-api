package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.AA_APPELLANT_DATE_OF_BIRTH;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.AGE_ASSESSMENT;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.boot.test.system.OutputCaptureExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

@ExtendWith({MockitoExtension.class, OutputCaptureExtension.class})
@MockitoSettings(strictness = Strictness.LENIENT)
public class DateOfBirthValidationHandlerTest {

    DateOfBirthValidationHandler dateOfBirthValidationHandler;
    @Mock Callback<AsylumCase> callback;
    @Mock CaseDetails<AsylumCase> caseDetails;
    @Mock AsylumCase asylumCase;

    @BeforeEach
    void setUp() {
        dateOfBirthValidationHandler = new DateOfBirthValidationHandler();
        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        when(callback.getPageId()).thenReturn(AA_APPELLANT_DATE_OF_BIRTH.value());
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(AGE_ASSESSMENT, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

    }

    @Test
    void should_log_error_for_invalid_date() {
        when(asylumCase.read(AA_APPELLANT_DATE_OF_BIRTH, String.class)).thenReturn(Optional.of("2045-11-11"));
        PreSubmitCallbackResponse<AsylumCase> response = dateOfBirthValidationHandler.handle(PreSubmitCallbackStage.MID_EVENT, callback);
        assertNotNull(response);
        assertEquals(1, response.getErrors().size());
        assertTrue(response.getErrors().contains("The date must not be a future date."));
    }

    @Test
    void should_not_log_error_for_valid_date() {
        when(asylumCase.read(AA_APPELLANT_DATE_OF_BIRTH, String.class)).thenReturn(Optional.of("2000-11-11"));
        PreSubmitCallbackResponse<AsylumCase> response = dateOfBirthValidationHandler.handle(PreSubmitCallbackStage.MID_EVENT, callback);
        assertNotNull(response);
        assertEquals(0, response.getErrors().size());
    }

    @Test
    void should_throw_error_when_dob_missing() {
        when(asylumCase.read(AA_APPELLANT_DATE_OF_BIRTH, String.class)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> dateOfBirthValidationHandler.handle(PreSubmitCallbackStage.MID_EVENT, callback))
            .isExactlyInstanceOf(RequiredFieldMissingException.class)
            .hasMessage("Appellant Date of Birth missing (Age Assessment)");
    }

    @Test
    void should_throw_error_when_age_assessment_field_missing() {
        when(asylumCase.read(AGE_ASSESSMENT, YesOrNo.class)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> dateOfBirthValidationHandler.handle(PreSubmitCallbackStage.MID_EVENT, callback))
            .isExactlyInstanceOf(RequiredFieldMissingException.class)
            .hasMessage("Age Assessment field missing");
    }

    @Test
    void should_handle_valid_event() {
        for (Event event : Event.values()) {
            when(callback.getEvent()).thenReturn(event);
            for (PreSubmitCallbackStage stage : PreSubmitCallbackStage.values()) {
                boolean canHandle = dateOfBirthValidationHandler.canHandle(stage, callback);
                if (event == Event.START_APPEAL && stage == PreSubmitCallbackStage.MID_EVENT) {
                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }
        }
    }

    @Test
    void should_handle_null_arguments() {
        assertThatThrownBy(() -> dateOfBirthValidationHandler.canHandle(null, callback))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("callbackStage must not be null");

        assertThatThrownBy(() -> dateOfBirthValidationHandler.canHandle(PreSubmitCallbackStage.MID_EVENT, null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("callback must not be null");
    }
}
