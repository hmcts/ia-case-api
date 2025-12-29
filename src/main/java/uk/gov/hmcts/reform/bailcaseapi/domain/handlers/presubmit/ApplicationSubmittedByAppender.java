package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.UserRoleLabel;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.bailcaseapi.domain.handlers.PreSubmitCallbackHandler;

@Component
public class ApplicationSubmittedByAppender implements PreSubmitCallbackHandler<BailCase> {

    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<BailCase> callback) {
        requireNonNull(callback, "callback must not be null");
        requireNonNull(callbackStage, "callbackStage must not be null");
        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && (callback.getEvent() == Event.START_APPLICATION
                   || callback.getEvent() == Event.EDIT_BAIL_APPLICATION
                   || callback.getEvent() == Event.EDIT_BAIL_APPLICATION_AFTER_SUBMIT
                   || callback.getEvent() == Event.MAKE_NEW_APPLICATION);
    }

    @Override
    public DispatchPriority getDispatchPriority() {
        return DispatchPriority.LATEST;
    }

    public PreSubmitCallbackResponse<BailCase> handle(PreSubmitCallbackStage callbackStage,
                                                      Callback<BailCase> callback) {

        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final BailCase bailCase = callback.getCaseDetails().getCaseData();

        boolean isLegalRep = bailCase.read(BailCaseFieldDefinition.IS_LEGAL_REP, YesOrNo.class).map(flag -> flag.equals(
            YesOrNo.YES)).orElse(false);

        boolean isAdmin = bailCase.read(
            BailCaseFieldDefinition.IS_ADMIN,
            YesOrNo.class
        ).map(flag -> flag.equals(YesOrNo.YES)).orElse(false);

        boolean isHomeOffice = bailCase.read(
            BailCaseFieldDefinition.IS_HOME_OFFICE,
            YesOrNo.class
        ).map(flag -> flag.equals(YesOrNo.YES)).orElse(false);

        String applicationSubmittedBy = null;

        if (isAdmin) {
            applicationSubmittedBy = bailCase.read(
                BailCaseFieldDefinition.APPLICATION_SENT_BY, String.class
            ).orElseThrow(() -> new IllegalStateException("Missing the field for Admin - APPLICATION_SENT_BY"));
        } else if (isLegalRep) {
            applicationSubmittedBy = UserRoleLabel.LEGAL_REPRESENTATIVE.toString();
        } else if (isHomeOffice) {
            applicationSubmittedBy = UserRoleLabel.HOME_OFFICE_BAIL.toString();
        } else {
            throw new IllegalStateException("Unknown user");
        }

        bailCase.write(BailCaseFieldDefinition.APPLICATION_SUBMITTED_BY, applicationSubmittedBy);
        return new PreSubmitCallbackResponse<>(bailCase);
    }
}
