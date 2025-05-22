package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.*;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DetentionFacility;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

@MockitoSettings(strictness = Strictness.LENIENT)
public class DetentionFacilityEditAppealHandlerTest {

    DetentionFacilityEditAppealHandler detentionFacilityEditAppealHandler;

    @Mock Callback<AsylumCase> callback;
    @Mock CaseDetails<AsylumCase> caseDetails;
    @Mock AsylumCase asylumCase;

    @BeforeEach
    void setUp() {
        detentionFacilityEditAppealHandler = new DetentionFacilityEditAppealHandler();
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(AsylumCaseFieldDefinition.APPELLANT_IN_DETENTION, YesOrNo.class))
            .thenReturn(Optional.of(YesOrNo.YES));
    }

    @Test
    void should_remove_prison_details_if_now_irc_edit_appeal() {
        when(callback.getEvent()).thenReturn(EDIT_APPEAL);
        when(asylumCase.read(AsylumCaseFieldDefinition.DETENTION_FACILITY, String.class))
            .thenReturn(Optional.of(DetentionFacility.IRC.getValue()));
        PreSubmitCallbackResponse<AsylumCase> response = detentionFacilityEditAppealHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);
        assertNotNull(response);

        // case fields that should be cleared
        verify(asylumCase, times(1)).clear(AsylumCaseFieldDefinition.PRISON_NOMS);
        verify(asylumCase, times(1)).clear(AsylumCaseFieldDefinition.PRISON_NAME);
        verify(asylumCase, times(1)).clear(AsylumCaseFieldDefinition.OTHER_DETENTION_FACILITY_NAME);
        verify(asylumCase, times(1)).clear(AsylumCaseFieldDefinition.CUSTODIAL_SENTENCE);
        verify(asylumCase, times(1)).clear(AsylumCaseFieldDefinition.DATE_CUSTODIAL_SENTENCE);

        // case fields that should not be cleared
        verify(asylumCase, times(0)).clear(AsylumCaseFieldDefinition.IRC_NAME);
        verify(asylumCase, times(0)).clear(AsylumCaseFieldDefinition.HAS_PENDING_BAIL_APPLICATIONS);
        verify(asylumCase, times(0)).clear(AsylumCaseFieldDefinition.BAIL_APPLICATION_NUMBER);

        // detention facility not cleared
        verify(asylumCase, times(0)).clear(AsylumCaseFieldDefinition.DETENTION_FACILITY);
    }

    @Test
    void should_remove_irc_details_if_now_prison_edit_appeal() {
        when(callback.getEvent()).thenReturn(EDIT_APPEAL);
        when(asylumCase.read(AsylumCaseFieldDefinition.DETENTION_FACILITY, String.class))
            .thenReturn(Optional.of(DetentionFacility.PRISON.getValue()));
        PreSubmitCallbackResponse<AsylumCase> response = detentionFacilityEditAppealHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);
        assertNotNull(response);

        // case fields that should be cleared
        verify(asylumCase, times(1)).clear(AsylumCaseFieldDefinition.IRC_NAME);
        verify(asylumCase, times(1)).clear(AsylumCaseFieldDefinition.OTHER_DETENTION_FACILITY_NAME);
        verify(asylumCase, times(1)).clear(AsylumCaseFieldDefinition.DATE_CUSTODIAL_SENTENCE);

        // case fields that should not be cleared
        verify(asylumCase, times(0)).clear(AsylumCaseFieldDefinition.PRISON_NOMS);
        verify(asylumCase, times(0)).clear(AsylumCaseFieldDefinition.PRISON_NAME);
        verify(asylumCase, times(0)).clear(AsylumCaseFieldDefinition.CUSTODIAL_SENTENCE);
        verify(asylumCase, times(0)).clear(AsylumCaseFieldDefinition.HAS_PENDING_BAIL_APPLICATIONS);
        verify(asylumCase, times(0)).clear(AsylumCaseFieldDefinition.BAIL_APPLICATION_NUMBER);

        // detention facility not cleared
        verify(asylumCase, times(0)).clear(AsylumCaseFieldDefinition.DETENTION_FACILITY);
    }

    @Test
    void should_remove_irc_prison_details_if_now_other_facility_edit_appeal() {
        when(callback.getEvent()).thenReturn(EDIT_APPEAL);
        when(asylumCase.read(AsylumCaseFieldDefinition.DETENTION_FACILITY, String.class))
            .thenReturn(Optional.of(DetentionFacility.OTHER.getValue()));
        PreSubmitCallbackResponse<AsylumCase> response = detentionFacilityEditAppealHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);
        assertNotNull(response);

        // case fields that should be cleared
        verify(asylumCase, times(1)).clear(AsylumCaseFieldDefinition.PRISON_NOMS);
        verify(asylumCase, times(1)).clear(AsylumCaseFieldDefinition.PRISON_NAME);
        verify(asylumCase, times(1)).clear(AsylumCaseFieldDefinition.DATE_CUSTODIAL_SENTENCE);
        verify(asylumCase, times(1)).clear(AsylumCaseFieldDefinition.IRC_NAME);

        // case fields that should not be cleared
        verify(asylumCase, times(0)).clear(AsylumCaseFieldDefinition.OTHER_DETENTION_FACILITY_NAME);
        verify(asylumCase, times(0)).clear(AsylumCaseFieldDefinition.CUSTODIAL_SENTENCE);
        verify(asylumCase, times(0)).clear(AsylumCaseFieldDefinition.HAS_PENDING_BAIL_APPLICATIONS);
        verify(asylumCase, times(0)).clear(AsylumCaseFieldDefinition.BAIL_APPLICATION_NUMBER);

        // detention facility not cleared
        verify(asylumCase, times(0)).clear(AsylumCaseFieldDefinition.DETENTION_FACILITY);

    }

    @Test
    void should_remove_custodial_date_details_if_not_applicable() {
        when(callback.getEvent()).thenReturn(EDIT_APPEAL);
        when(asylumCase.read(AsylumCaseFieldDefinition.DETENTION_FACILITY, String.class))
            .thenReturn(Optional.of(DetentionFacility.IRC.getValue()));
        when(asylumCase.read(AsylumCaseFieldDefinition.CUSTODIAL_SENTENCE, YesOrNo.class))
            .thenReturn(Optional.of(YesOrNo.NO));
        PreSubmitCallbackResponse<AsylumCase> response = detentionFacilityEditAppealHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);
        assertNotNull(response);
        verify(asylumCase, times(1)).clear(AsylumCaseFieldDefinition.DATE_CUSTODIAL_SENTENCE);
    }

    @Test
    void should_remove_removal_order_date_details_if_not_applicable() {
        when(callback.getEvent()).thenReturn(EDIT_APPEAL_AFTER_SUBMIT);
        when(asylumCase.read(AsylumCaseFieldDefinition.DETENTION_FACILITY, String.class))
            .thenReturn(Optional.of(DetentionFacility.IRC.getValue()));
        when(asylumCase.read(AsylumCaseFieldDefinition.REMOVAL_ORDER_OPTIONS, YesOrNo.class))
            .thenReturn(Optional.of(YesOrNo.NO));
        PreSubmitCallbackResponse<AsylumCase> response = detentionFacilityEditAppealHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);
        assertNotNull(response);
        verify(asylumCase, times(1)).clear(AsylumCaseFieldDefinition.REMOVAL_ORDER_DATE);
    }

    @Test
    void handling_should_throw_if_detention_facility_not_available() {
        when(callback.getEvent()).thenReturn(EDIT_APPEAL);
        assertThatThrownBy(() -> detentionFacilityEditAppealHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Detention Facility missing")
            .isExactlyInstanceOf(RequiredFieldMissingException.class);
    }

    @Test
    void check_canHandle() {
        for (Event event : Event.values()) {
            when(callback.getEvent()).thenReturn(event);
            for (PreSubmitCallbackStage stage : PreSubmitCallbackStage.values()) {
                boolean canHandle = detentionFacilityEditAppealHandler.canHandle(stage, callback);
                if (stage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                        && List.of(EDIT_APPEAL, EDIT_APPEAL_AFTER_SUBMIT, UPDATE_DETENTION_LOCATION).contains(callback.getEvent())) {
                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }
        }
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {
        assertThatThrownBy(() -> detentionFacilityEditAppealHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        assertThatThrownBy(() -> detentionFacilityEditAppealHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void handling_should_throw_if_argument_null() {
        assertThatThrownBy(() -> detentionFacilityEditAppealHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> detentionFacilityEditAppealHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

}
