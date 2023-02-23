package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.util.Arrays;
import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;


@Component
public class InternalCaseCreationContactDetailsAppender implements PreSubmitCallbackHandler<AsylumCase> {

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && Arrays.asList(Event.START_APPEAL,
                Event.EDIT_APPEAL,
                Event.EDIT_APPEAL_AFTER_SUBMIT).contains(callback.getEvent());
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        YesOrNo isAdmin = asylumCase.read(IS_ADMIN, YesOrNo.class).orElse(YesOrNo.NO);


        if (isAdmin.equals(YesOrNo.YES)) {

            Optional<String> internalAppellantMobileNumber = asylumCase.read(INTERNAL_APPELLANT_MOBILE_NUMBER, String.class);
            Optional<String> internalAppellantEmail = asylumCase.read(INTERNAL_APPELLANT_EMAIL, String.class);
            Optional<String> appealSubmissionInternalDate = asylumCase.read(APPEAL_SUBMISSION_INTERNAL_DATE, String.class);
            Optional<String> decisionLetterReceivedDate = asylumCase.read(DECISION_LETTER_RECEIVED_DATE, String.class);
            Optional<String> homeOfficeDecisionDate = asylumCase.read(HOME_OFFICE_DECISION_DATE, String.class);
            Optional<String> tribunalReceivedDate = asylumCase.read(TRIBUNAL_RECEIVED_DATE, String.class);

            if (internalAppellantMobileNumber.isPresent()) {
                asylumCase.write(MOBILE_NUMBER, internalAppellantMobileNumber);
            }
            if (internalAppellantEmail.isPresent()) {
                asylumCase.write(EMAIL, internalAppellantEmail);
            }
            if (appealSubmissionInternalDate.isPresent()) {
                asylumCase.write(APPEAL_SUBMISSION_INTERNAL_DATE, appealSubmissionInternalDate);
            }
            if (decisionLetterReceivedDate.isPresent()) {
                asylumCase.write(APPEAL_SUBMISSION_INTERNAL_DATE, decisionLetterReceivedDate);
            }
            if (homeOfficeDecisionDate.isPresent()) {
                asylumCase.write(APPEAL_SUBMISSION_INTERNAL_DATE, homeOfficeDecisionDate);
            }
            if (tribunalReceivedDate.isPresent()) {
                asylumCase.write(TRIBUNAL_RECEIVED_INTERNAL_DATE, tribunalReceivedDate);
            }
        }
        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
