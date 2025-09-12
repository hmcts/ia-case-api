package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.AA_APPELLANT_DATE_OF_BIRTH;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.AGE_ASSESSMENT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_DATE_OF_BIRTH;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

@Component
@Slf4j
public class DateOfBirthValidationHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private static final String APPELLANT_BASIC_DETAILS_PAGE_ID = "appellantBasicDetails";

    public DateOfBirthValidationHandler() {
    }

    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callback.getEvent() == Event.START_APPEAL
            && callbackStage == PreSubmitCallbackStage.MID_EVENT
            && (callback.getPageId().equals(AA_APPELLANT_DATE_OF_BIRTH.value()) || callback.getPageId().equals(APPELLANT_BASIC_DETAILS_PAGE_ID));
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {

        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        PreSubmitCallbackResponse<AsylumCase> response = new PreSubmitCallbackResponse<>(asylumCase);

        Boolean isAppellantDobInTheFuture = false;
        if (callback.getPageId().equals(AA_APPELLANT_DATE_OF_BIRTH.value())) {
            Optional<YesOrNo> isAgeAssessmentAppeal = asylumCase.read(AGE_ASSESSMENT, YesOrNo.class);
            if (isAgeAssessmentAppeal.equals(Optional.of(YesOrNo.YES))) {
                String appellantDobStr = asylumCase.read(AA_APPELLANT_DATE_OF_BIRTH, String.class)
                    .orElseThrow(() -> new RequiredFieldMissingException("Appellant Date of Birth missing (Age Assessment)"));
                isAppellantDobInTheFuture = isFutureAppellantDob(appellantDobStr);
            } 
        } else { // callback.getPageId() = "appellantBasicDetails"
            String appellantDobStr = asylumCase.read(APPELLANT_DATE_OF_BIRTH, String.class)
                .orElseThrow(() -> new RequiredFieldMissingException("Appellant Date of Birth missing"));
            isAppellantDobInTheFuture = isFutureAppellantDob(appellantDobStr);
        }
        
        if (isAppellantDobInTheFuture) {
            response.addError("The date must not be a future date.");
        }
        return response;
    }

    private Boolean isFutureAppellantDob(String appellantDobStr) {
        Boolean isAppellantDobInTheFuture = false;
        Optional<LocalDate> maybeAppellantDob = parseDate(appellantDobStr);
        if (maybeAppellantDob.isPresent()) {
            LocalDate dateToCheck = maybeAppellantDob.get();
            if (dateToCheck.isAfter(LocalDate.now())) {
                isAppellantDobInTheFuture = true;
            }
        }
        return isAppellantDobInTheFuture;
    }

    private Optional<LocalDate> parseDate(String dateStr) {
        try {
            return Optional.of(LocalDate.parse(dateStr));
        } catch (DateTimeParseException ex) {
            log.error("Date Str [{}] can't be parsed", dateStr);
        }
        return Optional.empty();
    }
}
