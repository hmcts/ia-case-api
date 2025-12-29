package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bailcaseapi.domain.UserDetailsHelper;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.UserRoleLabel;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.bailcaseapi.domain.handlers.PreSubmitCallbackHandler;

@Component
public class CurrentUserRoleAppender implements PreSubmitCallbackHandler<BailCase> {

    private final UserDetails userDetails;
    private final UserDetailsHelper userDetailsHelper;

    public CurrentUserRoleAppender(UserDetails userDetails, UserDetailsHelper userDetailsHelper) {
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
               && (callback.getEvent() == Event.UPLOAD_DOCUMENTS);
    }

    public PreSubmitCallbackResponse<BailCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<BailCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        BailCase bailCase = callback.getCaseDetails().getCaseData();

        UserRoleLabel userRoleLabel = userDetailsHelper.getLoggedInUserRoleLabel(userDetails, true);
        if (userRoleLabel.equals(UserRoleLabel.ADMIN_OFFICER)) {
            bailCase.write(BailCaseFieldDefinition.CURRENT_USER,
                           UserRoleLabel.ADMIN_OFFICER.toString());
        }
        if (userRoleLabel.equals(UserRoleLabel.LEGAL_REPRESENTATIVE)) {
            bailCase.write(BailCaseFieldDefinition.CURRENT_USER,
                           UserRoleLabel.LEGAL_REPRESENTATIVE.toString());
        }
        if (userRoleLabel.equals(UserRoleLabel.HOME_OFFICE_BAIL)) {
            bailCase.write(BailCaseFieldDefinition.CURRENT_USER,
                           UserRoleLabel.HOME_OFFICE_BAIL.toString());
        }
        if (userRoleLabel.equals(UserRoleLabel.JUDGE)) {
            bailCase.write(BailCaseFieldDefinition.CURRENT_USER,
                           UserRoleLabel.JUDGE.toString());
        }

        return new PreSubmitCallbackResponse<>(bailCase);
    }
}
