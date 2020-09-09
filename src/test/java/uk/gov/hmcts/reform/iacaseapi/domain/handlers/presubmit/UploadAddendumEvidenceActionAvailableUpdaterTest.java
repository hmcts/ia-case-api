package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.util.Arrays;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;


@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class UploadAddendumEvidenceActionAvailableUpdaterTest {

    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;

    private UploadAddendumEvidenceActionAvailableUpdater uploadAddendumEvidenceActionAvailableUpdater =
        new UploadAddendumEvidenceActionAvailableUpdater();

    @Test
    public void should_set_action_available_flag_to_yes_when_state_applies() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        for (State state : State.values()) {

            when(caseDetails.getState()).thenReturn(state);

            PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                uploadAddendumEvidenceActionAvailableUpdater
                    .handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

            assertNotNull(callbackResponse);
            assertEquals(asylumCase, callbackResponse.getData());

            if (Arrays.asList(
                State.PRE_HEARING,
                State.DECISION,
                State.DECIDED
            ).contains(state)) {

                verify(asylumCase).write(UPLOAD_ADDENDUM_EVIDENCE_ACTION_AVAILABLE, YesOrNo.YES);
                verify(asylumCase).write(UPLOAD_ADDENDUM_EVIDENCE_LEGAL_REP_ACTION_AVAILABLE, YesOrNo.YES);
                verify(asylumCase).write(UPLOAD_ADDENDUM_EVIDENCE_HOME_OFFICE_ACTION_AVAILABLE, YesOrNo.YES);
                verify(asylumCase).write(UPLOAD_ADDENDUM_EVIDENCE_ADMIN_OFFICER_ACTION_AVAILABLE, YesOrNo.YES);
            } else {
                verify(asylumCase).write(UPLOAD_ADDENDUM_EVIDENCE_ACTION_AVAILABLE, YesOrNo.NO);
                verify(asylumCase).write(UPLOAD_ADDENDUM_EVIDENCE_LEGAL_REP_ACTION_AVAILABLE, YesOrNo.NO);
                verify(asylumCase).write(UPLOAD_ADDENDUM_EVIDENCE_HOME_OFFICE_ACTION_AVAILABLE, YesOrNo.NO);
                verify(asylumCase).write(UPLOAD_ADDENDUM_EVIDENCE_ADMIN_OFFICER_ACTION_AVAILABLE, YesOrNo.NO);

            }

            reset(asylumCase);
        }
    }

    @Test
    public void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> uploadAddendumEvidenceActionAvailableUpdater.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void it_can_handle_callback_for_all_events() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = uploadAddendumEvidenceActionAvailableUpdater.canHandle(callbackStage, callback);

                if (callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT) {

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

        assertThatThrownBy(() -> uploadAddendumEvidenceActionAvailableUpdater.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> uploadAddendumEvidenceActionAvailableUpdater.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> uploadAddendumEvidenceActionAvailableUpdater.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> uploadAddendumEvidenceActionAvailableUpdater.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
