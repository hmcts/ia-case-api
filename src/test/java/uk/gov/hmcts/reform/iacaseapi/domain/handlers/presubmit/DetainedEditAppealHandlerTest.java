package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
                .thenReturn(Optional.of(YesOrNo.YES));
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

}
