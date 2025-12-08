package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class RemoveDetainedStatusHandlerTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    private RemoveDetainedStatusHandler removeDetainedStatusHandler;

    @BeforeEach
    public void setup() {
        removeDetainedStatusHandler = new RemoveDetainedStatusHandler();
    }

    @Test
    void should_remove_detained_status_for_a_non_ada_detained_case() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(callback.getEvent()).thenReturn(Event.REMOVE_DETAINED_STATUS);
        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            removeDetainedStatusHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase).write(APPELLANT_IN_DETENTION, YesOrNo.NO);
        verify(asylumCase).clear(DETENTION_FACILITY);
        verify(asylumCase).clear(IRC_NAME);
        verify(asylumCase).clear(PRISON_NAME);
        verify(asylumCase).clear(OTHER_DETENTION_FACILITY_NAME);
        verify(asylumCase).clear(PRISON_NOMS);
        verify(asylumCase).clear(CUSTODIAL_SENTENCE);
        verify(asylumCase).clear(DATE_CUSTODIAL_SENTENCE);
        verify(asylumCase).clear(HAS_PENDING_BAIL_APPLICATIONS);
        verify(asylumCase).clear(BAIL_APPLICATION_NUMBER);
        verify(asylumCase).clear(REMOVAL_ORDER_OPTIONS);
        verify(asylumCase).clear(REMOVAL_ORDER_DATE);
        verify(asylumCase).clear(DETENTION_STATUS);
    }

    @Test
    void should_not_clear_detention_fields_for_a_non_detained_case() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(callback.getEvent()).thenReturn(Event.REMOVE_DETAINED_STATUS);
        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            removeDetainedStatusHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, never()).write(APPELLANT_IN_DETENTION, YesOrNo.NO);
        verify(asylumCase, never()).clear(DETENTION_FACILITY);
        verify(asylumCase, never()).clear(IRC_NAME);
        verify(asylumCase, never()).clear(PRISON_NAME);
        verify(asylumCase, never()).clear(OTHER_DETENTION_FACILITY_NAME);
        verify(asylumCase, never()).clear(PRISON_NOMS);
        verify(asylumCase, never()).clear(CUSTODIAL_SENTENCE);
        verify(asylumCase, never()).clear(DATE_CUSTODIAL_SENTENCE);
        verify(asylumCase, never()).clear(HAS_PENDING_BAIL_APPLICATIONS);
        verify(asylumCase, never()).clear(BAIL_APPLICATION_NUMBER);
        verify(asylumCase, never()).clear(REMOVAL_ORDER_OPTIONS);
        verify(asylumCase, never()).clear(REMOVAL_ORDER_DATE);
        verify(asylumCase, never()).clear(DETENTION_STATUS);
        verify(asylumCase, never()).clear(DETENTION_REMOVAL_DATE);
        verify(asylumCase, never()).clear(DETENTION_REMOVAL_REASON);
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        assertThatThrownBy(
            () -> removeDetainedStatusHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {
        for (Event event : Event.values()) {
            when(callback.getEvent()).thenReturn(event);
            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {
                boolean canHandle = removeDetainedStatusHandler.canHandle(callbackStage, callback);
                if (callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && ((callback.getEvent() == Event.REMOVE_DETAINED_STATUS))) {
                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }
        }
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> removeDetainedStatusHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(
            () -> removeDetainedStatusHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> removeDetainedStatusHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> removeDetainedStatusHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }


}