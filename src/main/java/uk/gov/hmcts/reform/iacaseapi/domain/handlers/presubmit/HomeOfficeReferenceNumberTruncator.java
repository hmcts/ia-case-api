package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HOME_OFFICE_REFERENCE_NUMBER;

import java.util.Arrays;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

@Component
public class HomeOfficeReferenceNumberTruncator implements PreSubmitCallbackHandler<AsylumCase> {

    private static final Pattern HOME_OFFICE_REF_PATTERN = Pattern.compile("^[A-Za-z][0-9]{6}[0-9]?(|\\/[0-9][0-9]?[0-9]?)$");

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && Arrays.asList(
                    Event.SUBMIT_APPEAL,
                    Event.PAY_AND_SUBMIT_APPEAL)
                   .contains(callback.getEvent());
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase =
            callback
                .getCaseDetails()
                .getCaseData();

        Optional<String> maybeHomeOfficeReferenceNumber =
                asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER);

        String homeOfficeReferenceNumber =
                maybeHomeOfficeReferenceNumber.orElseThrow(() -> new RequiredFieldMissingException("homeOfficeReferenceNumber is not present"));

        Matcher homeOfficeReferenceMatcher = HOME_OFFICE_REF_PATTERN.matcher(homeOfficeReferenceNumber);

        if (homeOfficeReferenceMatcher.find()
                && homeOfficeReferenceMatcher.groupCount() == 1) {

            if (homeOfficeReferenceNumber.contains("/")) {
                String truncatedReferenceNumber = homeOfficeReferenceNumber.split("/")[0];
                asylumCase.write(HOME_OFFICE_REFERENCE_NUMBER, truncatedReferenceNumber);
            } else {
                asylumCase.write(HOME_OFFICE_REFERENCE_NUMBER, homeOfficeReferenceNumber);
            }
        } else {
            if (homeOfficeReferenceNumber.length() > 0) {
                asylumCase.write(HOME_OFFICE_REFERENCE_NUMBER, homeOfficeReferenceNumber);
            }
        }
        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
