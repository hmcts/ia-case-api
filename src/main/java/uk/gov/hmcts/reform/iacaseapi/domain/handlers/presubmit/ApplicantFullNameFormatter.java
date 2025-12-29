package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition.APPLICANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition.APPLICANT_FULL_NAME;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition.APPLICANT_GIVEN_NAMES;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

@Component
public class ApplicantFullNameFormatter implements PreSubmitCallbackHandler<BailCase> {

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<BailCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && (callback.getEvent() == Event.START_APPLICATION
                   || callback.getEvent() == Event.EDIT_BAIL_APPLICATION
                   || callback.getEvent() == Event.MAKE_NEW_APPLICATION
                   || callback.getEvent() == Event.EDIT_BAIL_APPLICATION_AFTER_SUBMIT);
    }

    public PreSubmitCallbackResponse<BailCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<BailCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final BailCase bailCase =
            callback
                .getCaseDetails()
                .getCaseData();

        final String applicantGivenNames =
            bailCase
                .read(APPLICANT_GIVEN_NAMES, String.class)
                .orElseThrow(() -> new IllegalStateException("applicantGivenNames is not present"));

        final String applicantFamilyName =
            bailCase
                .read(APPLICANT_FAMILY_NAME, String.class)
                .orElseThrow(() -> new IllegalStateException("applicantFamilyName is not present"));

        String applicantFullName = applicantGivenNames + " " + applicantFamilyName;

        bailCase.write(
            APPLICANT_FULL_NAME,
            applicantFullName.replaceAll("\\s+", " ").trim()
        );

        return new PreSubmitCallbackResponse<>(bailCase);
    }
}
