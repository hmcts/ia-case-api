package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.UserDetailsHelper;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserRoleLabel;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

@Component
public class ApplicationUserRoleAppender implements PreSubmitCallbackHandler<BailCase> {

    private final UserDetails userDetails;
    private final UserDetailsHelper userDetailsHelper;


    public ApplicationUserRoleAppender(UserDetails userDetails, UserDetailsHelper userDetailsHelper) {
        this.userDetails = userDetails;
        this.userDetailsHelper = userDetailsHelper;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<BailCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_START
               && (callback.getEvent() == Event.START_APPLICATION
                   || callback.getEvent() == Event.EDIT_BAIL_APPLICATION_AFTER_SUBMIT
                   || callback.getEvent() == Event.MAKE_NEW_APPLICATION)
               || callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                  && callback.getEvent() == Event.MAKE_NEW_APPLICATION;
    }

    public PreSubmitCallbackResponse<BailCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<BailCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final BailCase bailCase = callback.getCaseDetails().getCaseData();

        UserRoleLabel userRoleLabel = userDetailsHelper.getLoggedInUserRoleLabel(userDetails, true);
        if (userRoleLabel.equals(UserRoleLabel.ADMIN_OFFICER)) {
            bailCase.write(BailCaseFieldDefinition.IS_ADMIN, YesOrNo.YES);
        } else {
            bailCase.write(BailCaseFieldDefinition.IS_ADMIN, YesOrNo.NO);
        }
        if (userRoleLabel.equals(UserRoleLabel.LEGAL_REPRESENTATIVE)) {
            bailCase.write(BailCaseFieldDefinition.IS_LEGAL_REP, YesOrNo.YES);
        } else {
            bailCase.write(BailCaseFieldDefinition.IS_LEGAL_REP, YesOrNo.NO);
        }
        if (userRoleLabel.equals(UserRoleLabel.HOME_OFFICE_BAIL)) {
            bailCase.write(BailCaseFieldDefinition.IS_HOME_OFFICE, YesOrNo.YES);
        } else {
            bailCase.write(BailCaseFieldDefinition.IS_HOME_OFFICE, YesOrNo.NO);
        }

        return new PreSubmitCallbackResponse<>(bailCase);
    }
}
