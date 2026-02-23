package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

@MockitoSettings(strictness = Strictness.LENIENT)
public class DetainedEditAppealHandlerTest {

    DetainedEditAppealHandler detainedEditAppealHandler;

    @Mock Callback<AsylumCase> callback;
    @Mock CaseDetails<AsylumCase> caseDetails;
    @Mock AsylumCase asylumCase;

    @BeforeEach
    void setUp() {
        detainedEditAppealHandler = new DetainedEditAppealHandler();
        when(callback.getEvent()).thenReturn(Event.EDIT_APPEAL);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
    }

    @Test
    void should_remove_appellant_address_and_contact_info() {
        when(asylumCase.read(AsylumCaseFieldDefinition.APPELLANT_IN_DETENTION, YesOrNo.class))
                .thenReturn(Optional.of(YES));
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of("prison"));
        PreSubmitCallbackResponse<AsylumCase> response = detainedEditAppealHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);
        assertNotNull(response);
        verify(asylumCase, times(1))
                .clear(AsylumCaseFieldDefinition.APPELLANT_HAS_FIXED_ADDRESS);
        verify(asylumCase, times(1)).clear(AsylumCaseFieldDefinition.APPELLANT_ADDRESS);
        verify(asylumCase, times(1)).clear(AsylumCaseFieldDefinition.SEARCH_POSTCODE);
        verify(asylumCase, times(1))
                .clear(AsylumCaseFieldDefinition.HAS_CORRESPONDENCE_ADDRESS);
        verify(asylumCase, times(1))
                .clear(AsylumCaseFieldDefinition.APPELLANT_OUT_OF_COUNTRY_ADDRESS);
        verify(asylumCase, times(1)).clear(AsylumCaseFieldDefinition.CONTACT_PREFERENCE);
        verify(asylumCase, times(1)).clear(AsylumCaseFieldDefinition.EMAIL);
        verify(asylumCase, times(1)).clear(AsylumCaseFieldDefinition.MOBILE_NUMBER);
    }

    @Test
    void should_remove_detained_fields_when_editing_to_non_detained() {
        when(asylumCase.read(AsylumCaseFieldDefinition.APPELLANT_IN_DETENTION, YesOrNo.class))
                .thenReturn(Optional.of(YesOrNo.NO));
        PreSubmitCallbackResponse<AsylumCase> response = detainedEditAppealHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);
        assertNotNull(response);
        verify(asylumCase, times(1))
                .clear(AsylumCaseFieldDefinition.DETENTION_STATUS);
        verify(asylumCase, times(1)).clear(AsylumCaseFieldDefinition.DETENTION_FACILITY);
        verify(asylumCase, times(1)).clear(AsylumCaseFieldDefinition.PRISON_NAME);
        verify(asylumCase, times(1))
                .clear(AsylumCaseFieldDefinition.PRISON_NOMS);
        verify(asylumCase, times(1))
                .clear(AsylumCaseFieldDefinition.CUSTODIAL_SENTENCE);
        verify(asylumCase, times(1)).clear(AsylumCaseFieldDefinition.DATE_CUSTODIAL_SENTENCE);
        verify(asylumCase, times(1)).clear(AsylumCaseFieldDefinition.OTHER_DETENTION_FACILITY_NAME);
        verify(asylumCase, times(1)).clear(AsylumCaseFieldDefinition.IRC_NAME);
        verify(asylumCase, times(1)).clear(AsylumCaseFieldDefinition.HAS_PENDING_BAIL_APPLICATIONS);
        verify(asylumCase, times(1)).clear(AsylumCaseFieldDefinition.BAIL_APPLICATION_NUMBER);
        verify(asylumCase, times(1)).clear(AsylumCaseFieldDefinition.IS_ACCELERATED_DETAINED_APPEAL);
        verify(asylumCase, times(1)).clear(AsylumCaseFieldDefinition.REMOVAL_ORDER_OPTIONS);
        verify(asylumCase, times(1)).clear(AsylumCaseFieldDefinition.REMOVAL_ORDER_DATE);
        verify(asylumCase, times(1)).clear(AsylumCaseFieldDefinition.SUITABILITY_HEARING_TYPE_YES_OR_NO);
        verify(asylumCase, times(1)).clear(AsylumCaseFieldDefinition.SUITABILITY_APPELLANT_ATTENDANCE_YES_OR_NO_1);
        verify(asylumCase, times(1)).clear(AsylumCaseFieldDefinition.SUITABILITY_APPELLANT_ATTENDANCE_YES_OR_NO_2);
        verify(asylumCase, times(1)).clear(AsylumCaseFieldDefinition.SUITABILITY_INTERPRETER_SERVICES_YES_OR_NO);
        verify(asylumCase, times(1)).clear(AsylumCaseFieldDefinition.SUITABILITY_INTERPRETER_SERVICES_LANGUAGE);
    }

    @Test
    void check_canHandle() {
        for (Event event : Event.values()) {
            when(callback.getEvent()).thenReturn(event);
            for (PreSubmitCallbackStage stage : PreSubmitCallbackStage.values()) {
                boolean canHandle = detainedEditAppealHandler.canHandle(stage, callback);
                if ((event == Event.EDIT_APPEAL) && (stage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT)) {
                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }
        }
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {
        assertThatThrownBy(() -> detainedEditAppealHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        assertThatThrownBy(() -> detainedEditAppealHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void handling_should_throw_if_argument_null() {
        assertThatThrownBy(() -> detainedEditAppealHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> detainedEditAppealHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void should_remove_ada_suitability_fields_when_editing_to_detained_non_ada() {
        when(asylumCase.read(AsylumCaseFieldDefinition.APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of("prison"));
        PreSubmitCallbackResponse<AsylumCase> response = detainedEditAppealHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(response);
        verify(asylumCase, times(1)).clear(AsylumCaseFieldDefinition.SUITABILITY_HEARING_TYPE_YES_OR_NO);
        verify(asylumCase, times(1)).clear(AsylumCaseFieldDefinition.SUITABILITY_APPELLANT_ATTENDANCE_YES_OR_NO_1);
        verify(asylumCase, times(1)).clear(AsylumCaseFieldDefinition.SUITABILITY_APPELLANT_ATTENDANCE_YES_OR_NO_2);
        verify(asylumCase, times(1)).clear(AsylumCaseFieldDefinition.SUITABILITY_INTERPRETER_SERVICES_YES_OR_NO);
        verify(asylumCase, times(1)).clear(AsylumCaseFieldDefinition.SUITABILITY_INTERPRETER_SERVICES_LANGUAGE);
    }

    @ParameterizedTest
    @EnumSource(YesOrNo.class)
    void should_remove_ada_suitability_appellant_attendance_fields_when_editing_hearing_type(YesOrNo yesOrNo) {
        when(asylumCase.read(AsylumCaseFieldDefinition.APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(SUITABILITY_HEARING_TYPE_YES_OR_NO, YesOrNo.class)).thenReturn(Optional.of(yesOrNo));
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of("prison"));
        PreSubmitCallbackResponse<AsylumCase> response = detainedEditAppealHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(response);
        assertEquals(asylumCase, response.getData());

        if (yesOrNo.equals(YES)) {
            verify(asylumCase, times(1)).clear(AsylumCaseFieldDefinition.SUITABILITY_APPELLANT_ATTENDANCE_YES_OR_NO_2);
        } else {
            verify(asylumCase, times(1)).clear(AsylumCaseFieldDefinition.SUITABILITY_APPELLANT_ATTENDANCE_YES_OR_NO_1);
        }
    }

    @Test
    void should_remove_ada_suitability_interpreter_services_fields_when_attendance_are_both_no() {
        when(asylumCase.read(AsylumCaseFieldDefinition.APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(SUITABILITY_HEARING_TYPE_YES_OR_NO, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(SUITABILITY_APPELLANT_ATTENDANCE_YES_OR_NO_1, YesOrNo.class)).thenReturn(Optional.of(NO));
        when(asylumCase.read(SUITABILITY_APPELLANT_ATTENDANCE_YES_OR_NO_2, YesOrNo.class)).thenReturn(Optional.of(NO));
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of("prison"));
        PreSubmitCallbackResponse<AsylumCase> response = detainedEditAppealHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(response);
        assertEquals(asylumCase, response.getData());

        verify(asylumCase, times(1)).clear(AsylumCaseFieldDefinition.SUITABILITY_INTERPRETER_SERVICES_YES_OR_NO);
        verify(asylumCase, times(1)).clear(AsylumCaseFieldDefinition.SUITABILITY_INTERPRETER_SERVICES_LANGUAGE);
    }

    @Test
    void should_not_clear_appellant_address_when_detention_facility_is_other() {
        when(asylumCase.read(AsylumCaseFieldDefinition.APPELLANT_IN_DETENTION, YesOrNo.class))
                .thenReturn(Optional.of(YES));
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of("other"));
        PreSubmitCallbackResponse<AsylumCase> response = detainedEditAppealHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);
        
        assertNotNull(response);
        verify(asylumCase, times(1))
                .clear(AsylumCaseFieldDefinition.APPELLANT_HAS_FIXED_ADDRESS);
        verify(asylumCase, never()).clear(AsylumCaseFieldDefinition.APPELLANT_ADDRESS);
        verify(asylumCase, times(1)).clear(AsylumCaseFieldDefinition.SEARCH_POSTCODE);
        verify(asylumCase, times(1))
                .clear(AsylumCaseFieldDefinition.HAS_CORRESPONDENCE_ADDRESS);
        verify(asylumCase, times(1))
                .clear(AsylumCaseFieldDefinition.APPELLANT_OUT_OF_COUNTRY_ADDRESS);
        verify(asylumCase, times(1)).clear(AsylumCaseFieldDefinition.CONTACT_PREFERENCE);
        verify(asylumCase, times(1)).clear(AsylumCaseFieldDefinition.EMAIL);
        verify(asylumCase, times(1)).clear(AsylumCaseFieldDefinition.MOBILE_NUMBER);
    }

    @Test
    void should_throw_exception_when_detention_facility_missing_for_detained_appellant() {
        when(asylumCase.read(AsylumCaseFieldDefinition.APPELLANT_IN_DETENTION, YesOrNo.class))
                .thenReturn(Optional.of(YES));
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> detainedEditAppealHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("detentionFacility missing for appellant in detention")
            .isExactlyInstanceOf(IllegalStateException.class);
    }
}
