package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FTPA_APPELLANT_RJ_DECISION_OUTCOME_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FTPA_APPLICANT_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.UPLOAD_HOME_OFFICE_BUNDLE_ACTION_AVAILABLE;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.ResidentJudgeFtpaDecisionHandler.DLRM_SETASIDE_FEATURE_FLAG;

import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.RoleAssignmentService;


@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class ResidentJudgeFtpaDecisionConfirmationTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private FeatureToggler featureToggler;
    @Mock
    private RoleAssignmentService roleAssignmentService;
    @InjectMocks
    private ResidentJudgeFtpaDecisionConfirmation residentJudgeFtpaDecisionConfirmation;


    @BeforeEach
    void setup() {
        residentJudgeFtpaDecisionConfirmation =
            new ResidentJudgeFtpaDecisionConfirmation(featureToggler, roleAssignmentService);
    }

    @Test
    void should_return_grant_confirmation() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.RESIDENT_JUDGE_FTPA_DECISION);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(FTPA_APPLICANT_TYPE, String.class)).thenReturn(Optional.of("appellant"));
        when(asylumCase.read(FTPA_APPELLANT_RJ_DECISION_OUTCOME_TYPE, String.class)).thenReturn(Optional.of("granted"));

        PostSubmitCallbackResponse callbackResponse =
            residentJudgeFtpaDecisionConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(
            callbackResponse.getConfirmationHeader().get())
            .contains("You've recorded the First-tier permission to appeal decision");

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains(
                "Both parties have been notified of the decision. The Upper Tribunal has also been notified, and will now proceed with the case.<br>");

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains("#### What happens next");
    }

    @Test
    void should_return_partially_granted_confirmation() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.RESIDENT_JUDGE_FTPA_DECISION);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(FTPA_APPLICANT_TYPE, String.class)).thenReturn(Optional.of("appellant"));
        when(asylumCase.read(FTPA_APPELLANT_RJ_DECISION_OUTCOME_TYPE, String.class))
            .thenReturn(Optional.of("partiallyGranted"));

        PostSubmitCallbackResponse callbackResponse =
            residentJudgeFtpaDecisionConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(
            callbackResponse.getConfirmationHeader().get())
            .contains("You've recorded the First-tier permission to appeal decision");

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains(
                "Both parties have been notified of the decision. The Upper Tribunal has also been notified, and will now proceed with the case.<br>");

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains("#### What happens next");
    }

    @Test
    void should_return_refused_confirmation() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.RESIDENT_JUDGE_FTPA_DECISION);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(FTPA_APPLICANT_TYPE, String.class)).thenReturn(Optional.of("appellant"));
        when(asylumCase.read(FTPA_APPELLANT_RJ_DECISION_OUTCOME_TYPE, String.class)).thenReturn(Optional.of("refused"));

        PostSubmitCallbackResponse callbackResponse =
            residentJudgeFtpaDecisionConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(
            callbackResponse.getConfirmationHeader().get())
            .contains("You've recorded the First-tier permission to appeal decision");

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains(
                "Both parties have been notified that permission was refused. They'll also be able to access this information in the FTPA tab.<br>");

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains("#### What happens next");
    }

    @Test
    void should_return_not_admitted_confirmation() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.RESIDENT_JUDGE_FTPA_DECISION);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(FTPA_APPLICANT_TYPE, String.class)).thenReturn(Optional.of("appellant"));
        when(asylumCase.read(FTPA_APPELLANT_RJ_DECISION_OUTCOME_TYPE, String.class)).thenReturn(Optional.of(
            "notAdmitted"));

        PostSubmitCallbackResponse callbackResponse =
            residentJudgeFtpaDecisionConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(
            callbackResponse.getConfirmationHeader().get())
            .contains("You've recorded the First-tier permission to appeal decision");

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains(
                "Both parties have been notified that permission was refused. They'll also be able to access this information in the FTPA tab.<br>");

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains("#### What happens next");
    }

    @Test
    void should_return_reheardRule32_confirmation() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.RESIDENT_JUDGE_FTPA_DECISION);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(FTPA_APPLICANT_TYPE, String.class)).thenReturn(Optional.of("appellant"));
        when(asylumCase.read(FTPA_APPELLANT_RJ_DECISION_OUTCOME_TYPE, String.class))
            .thenReturn(Optional.of("reheardRule32"));

        PostSubmitCallbackResponse callbackResponse =
            residentJudgeFtpaDecisionConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(
            callbackResponse.getConfirmationHeader().get())
            .contains("You've recorded the First-tier permission to appeal decision");

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains(
                "Both parties will be notified of the decision. A Caseworker will review any Tribunal instructions and then relist the case.<br>");

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains("#### What happens next");
    }

    @ParameterizedTest
    @MethodSource("toggleDlrmSwitchMode")
    void should_return_reheardRule35_confirmation(boolean toggleDlrmFlag) {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.RESIDENT_JUDGE_FTPA_DECISION);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(FTPA_APPLICANT_TYPE, String.class)).thenReturn(Optional.of("appellant"));
        when(asylumCase.read(FTPA_APPELLANT_RJ_DECISION_OUTCOME_TYPE, String.class))
            .thenReturn(Optional.of("reheardRule35"));
        when(featureToggler.getValue(DLRM_SETASIDE_FEATURE_FLAG, false)).thenReturn(toggleDlrmFlag);

        PostSubmitCallbackResponse callbackResponse =
            residentJudgeFtpaDecisionConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(
            callbackResponse.getConfirmationHeader().get())
            .contains("You've recorded the First-tier permission to appeal decision");

        assertThat(
                callbackResponse.getConfirmationBody().get())
                .contains("#### What happens next");

        if (toggleDlrmFlag) {
            assertThat(
                    callbackResponse.getConfirmationBody().get())
                    .contains(
                            "Both parties will be notified of the decision. A Legal Officer will review any Tribunal instructions and then relist the case.<br>");

        } else {
            assertThat(
                    callbackResponse.getConfirmationBody().get())
                    .contains(
                            "Both parties will be notified of the decision. A Caseworker will review any Tribunal instructions and then relist the case.<br>");
        }
    }

    @Test
    void should_return_remadeRule31_confirmation() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.RESIDENT_JUDGE_FTPA_DECISION);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(FTPA_APPLICANT_TYPE, String.class)).thenReturn(Optional.of("appellant"));
        when(asylumCase.read(FTPA_APPELLANT_RJ_DECISION_OUTCOME_TYPE, String.class))
            .thenReturn(Optional.of("remadeRule31"));

        PostSubmitCallbackResponse callbackResponse =
            residentJudgeFtpaDecisionConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(
            callbackResponse.getConfirmationHeader().get())
            .contains("You've recorded the First-tier permission to appeal decision");

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains("Both parties have been notified of the decision.<br>");

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains("#### What happens next");
    }

    @Test
    void should_return_remadeRule32_confirmation() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.RESIDENT_JUDGE_FTPA_DECISION);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(FTPA_APPLICANT_TYPE, String.class)).thenReturn(Optional.of("appellant"));
        when(asylumCase.read(FTPA_APPELLANT_RJ_DECISION_OUTCOME_TYPE, String.class))
            .thenReturn(Optional.of("remadeRule32"));
        when(featureToggler.getValue(DLRM_SETASIDE_FEATURE_FLAG, false)).thenReturn(false);

        PostSubmitCallbackResponse callbackResponse =
            residentJudgeFtpaDecisionConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(
            callbackResponse.getConfirmationHeader().get())
            .contains("You've recorded the First-tier permission to appeal decision");

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains("Both parties have been notified of the decision.<br>");

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains("#### What happens next");
    }

    @Test
    void should_return_remadeRule32_confirmation_dlrm() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.RESIDENT_JUDGE_FTPA_DECISION);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(FTPA_APPLICANT_TYPE, String.class)).thenReturn(Optional.of("appellant"));
        when(asylumCase.read(FTPA_APPELLANT_RJ_DECISION_OUTCOME_TYPE, String.class))
            .thenReturn(Optional.of("remadeRule32"));
        when(featureToggler.getValue(DLRM_SETASIDE_FEATURE_FLAG, false)).thenReturn(true);

        PostSubmitCallbackResponse callbackResponse =
            residentJudgeFtpaDecisionConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(
            callbackResponse.getConfirmationHeader().get())
            .contains("# You've disposed of the application");

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains("A Judge will update the decision.<br>");

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains("#### What happens next");
    }

    @Test
    void should_throw_if_ftpa_applicant_type_missing() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.RESIDENT_JUDGE_FTPA_DECISION);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        assertThatThrownBy(() -> residentJudgeFtpaDecisionConfirmation.handle(callback))
            .hasMessage("FtpaApplicantType is not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> residentJudgeFtpaDecisionConfirmation.handle(callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.SEND_DIRECTION);
        assertThatThrownBy(() -> residentJudgeFtpaDecisionConfirmation.handle(callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
        verify(asylumCase, never()).write(UPLOAD_HOME_OFFICE_BUNDLE_ACTION_AVAILABLE, YesOrNo.NO);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = residentJudgeFtpaDecisionConfirmation.canHandle(callback);

                if (event == Event.RESIDENT_JUDGE_FTPA_DECISION || event == Event.DECIDE_FTPA_APPLICATION) {

                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }

            reset(callback);
        }
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> residentJudgeFtpaDecisionConfirmation.canHandle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> residentJudgeFtpaDecisionConfirmation.handle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    private static Stream<Arguments> toggleDlrmSwitchMode() {
        return Stream.of(
                Arguments.of(true),
                Arguments.of(false)
        );
    }


}
