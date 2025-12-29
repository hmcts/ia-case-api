package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.bailcaseapi.domain.UserDetailsHelper;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.UserRoleLabel;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.YesOrNo;

@ExtendWith(MockitoExtension.class)
class ApplicationUserRoleAppenderTest {

    @Mock
    private Callback<BailCase> callback;
    @Mock
    private CaseDetails<BailCase> caseDetails;
    @Mock
    private BailCase bailCase;
    @Mock
    private UserDetails userDetails;
    @Mock
    private UserDetailsHelper userDetailsHelper;

    private ApplicationUserRoleAppender applicationUserRoleAppender;

    @BeforeEach
    public void setUp() {
        applicationUserRoleAppender =
            new ApplicationUserRoleAppender(userDetails, userDetailsHelper);
    }

    @Test
    void handler_checks_is_admin_set_value_yes() {

        when(callback.getEvent()).thenReturn(Event.START_APPLICATION);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(bailCase);
        when(userDetailsHelper.getLoggedInUserRoleLabel(userDetails, true)).thenReturn(UserRoleLabel.ADMIN_OFFICER);


        PreSubmitCallbackResponse<BailCase> response =
            applicationUserRoleAppender.handle(ABOUT_TO_START, callback);

        assertThat(response).isNotNull();
        assertThat(response.getData()).isNotEmpty();
        assertThat(response.getData()).isEqualTo(bailCase);
        assertThat(response.getErrors()).isEmpty();
        verify(bailCase, times(1)).write(
            BailCaseFieldDefinition.IS_ADMIN, YesOrNo.YES);
        verify(bailCase, times(1)).write(
            BailCaseFieldDefinition.IS_LEGAL_REP, YesOrNo.NO);
        verify(bailCase, times(1)).write(
            BailCaseFieldDefinition.IS_HOME_OFFICE, YesOrNo.NO);
    }

    @Test
    void handler_checks_is_admin_set_value_no() {

        when(callback.getEvent()).thenReturn(Event.START_APPLICATION);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(bailCase);
        when(userDetailsHelper.getLoggedInUserRoleLabel(userDetails, true)).thenReturn(UserRoleLabel.LEGAL_REPRESENTATIVE);


        PreSubmitCallbackResponse<BailCase> response =
            applicationUserRoleAppender.handle(ABOUT_TO_START, callback);

        assertThat(response).isNotNull();
        assertThat(response.getData()).isNotEmpty();
        assertThat(response.getData()).isEqualTo(bailCase);
        assertThat(response.getErrors()).isEmpty();
        verify(bailCase, times(1)).write(
            BailCaseFieldDefinition.IS_ADMIN, YesOrNo.NO);
    }

    @Test
    void handler_checks_is_legal_rep_set_value_yes() {

        when(callback.getEvent()).thenReturn(Event.START_APPLICATION);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(bailCase);
        when(userDetailsHelper.getLoggedInUserRoleLabel(userDetails, true)).thenReturn(UserRoleLabel.LEGAL_REPRESENTATIVE);


        PreSubmitCallbackResponse<BailCase> response =
            applicationUserRoleAppender.handle(ABOUT_TO_START, callback);

        assertThat(response).isNotNull();
        assertThat(response.getData()).isNotEmpty();
        assertThat(response.getData()).isEqualTo(bailCase);
        assertThat(response.getErrors()).isEmpty();
        verify(bailCase, times(1)).write(
            BailCaseFieldDefinition.IS_LEGAL_REP, YesOrNo.YES);
        verify(bailCase, times(1)).write(
            BailCaseFieldDefinition.IS_ADMIN, YesOrNo.NO);
        verify(bailCase, times(1)).write(
            BailCaseFieldDefinition.IS_HOME_OFFICE, YesOrNo.NO);
    }

    @Test
    void handler_checks_is_legal_rep_set_value_no() {

        when(callback.getEvent()).thenReturn(Event.START_APPLICATION);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(bailCase);
        when(userDetailsHelper.getLoggedInUserRoleLabel(userDetails, true)).thenReturn(UserRoleLabel.ADMIN_OFFICER);


        PreSubmitCallbackResponse<BailCase> response =
            applicationUserRoleAppender.handle(ABOUT_TO_START, callback);

        assertThat(response).isNotNull();
        assertThat(response.getData()).isNotEmpty();
        assertThat(response.getData()).isEqualTo(bailCase);
        assertThat(response.getErrors()).isEmpty();
        verify(bailCase, times(1)).write(
            BailCaseFieldDefinition.IS_LEGAL_REP, YesOrNo.NO);
    }

    @Test
    void handler_checks_is_home_office_set_value_yes() {

        when(callback.getEvent()).thenReturn(Event.START_APPLICATION);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(bailCase);
        when(userDetailsHelper.getLoggedInUserRoleLabel(userDetails, true)).thenReturn(UserRoleLabel.HOME_OFFICE_BAIL);


        PreSubmitCallbackResponse<BailCase> response =
            applicationUserRoleAppender.handle(ABOUT_TO_START, callback);

        assertThat(response).isNotNull();
        assertThat(response.getData()).isNotEmpty();
        assertThat(response.getData()).isEqualTo(bailCase);
        assertThat(response.getErrors()).isEmpty();
        verify(bailCase, times(1)).write(
            BailCaseFieldDefinition.IS_HOME_OFFICE, YesOrNo.YES);
        verify(bailCase, times(1)).write(
            BailCaseFieldDefinition.IS_ADMIN, YesOrNo.NO);
        verify(bailCase, times(1)).write(
            BailCaseFieldDefinition.IS_LEGAL_REP, YesOrNo.NO);
    }

    @Test
    void handler_checks_is_home_office_set_value_no() {

        when(callback.getEvent()).thenReturn(Event.START_APPLICATION);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(bailCase);
        when(userDetailsHelper.getLoggedInUserRoleLabel(userDetails, true)).thenReturn(UserRoleLabel.LEGAL_REPRESENTATIVE);


        PreSubmitCallbackResponse<BailCase> response =
            applicationUserRoleAppender.handle(ABOUT_TO_START, callback);

        assertThat(response).isNotNull();
        assertThat(response.getData()).isNotEmpty();
        assertThat(response.getData()).isEqualTo(bailCase);
        assertThat(response.getErrors()).isEmpty();
        verify(bailCase, times(1)).write(
            BailCaseFieldDefinition.IS_HOME_OFFICE, YesOrNo.NO);
    }


    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {
                boolean canHandle = applicationUserRoleAppender.canHandle(callbackStage, callback);
                if (callbackStage == ABOUT_TO_START
                        && (callback.getEvent() == Event.START_APPLICATION
                            || callback.getEvent() == Event.EDIT_BAIL_APPLICATION_AFTER_SUBMIT
                            || callback.getEvent() == Event.MAKE_NEW_APPLICATION)
                    || callbackStage == ABOUT_TO_SUBMIT
                        && callback.getEvent() == Event.MAKE_NEW_APPLICATION) {
                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }
        }
    }


    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> applicationUserRoleAppender.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> applicationUserRoleAppender.canHandle(PreSubmitCallbackStage.ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> applicationUserRoleAppender.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> applicationUserRoleAppender.handle(PreSubmitCallbackStage.ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

    }

    @Test
    void handler_throws_error_if_cannot_actually_handle() {

        assertThatThrownBy(() -> applicationUserRoleAppender.handle(ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.START_APPLICATION);
        assertThatThrownBy(() -> applicationUserRoleAppender.handle(ABOUT_TO_START, callback))
            .isExactlyInstanceOf(NullPointerException.class);

    }

}
