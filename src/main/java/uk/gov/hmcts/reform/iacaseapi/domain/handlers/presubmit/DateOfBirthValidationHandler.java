package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.AA_APPELLANT_DATE_OF_BIRTH;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.AGE_ASSESSMENT;

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

    public DateOfBirthValidationHandler() {
    }

    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        log.info(
            "DateOfBirthValidationHandler: stage `{}`, event `{}`, case ID `{}`, page ID `{}`",
            callbackStage,
            callback.getEvent(),
            callback.getCaseDetails().getId(),
            callback.getPageId()
        );

        return callback.getEvent() == Event.START_APPEAL
            && callbackStage == PreSubmitCallbackStage.MID_EVENT
            && callback.getPageId().equals(AA_APPELLANT_DATE_OF_BIRTH.value());
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {

        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        PreSubmitCallbackResponse<AsylumCase> response = new PreSubmitCallbackResponse<>(asylumCase);

        Optional<YesOrNo> isAgeAssessmentAppeal = asylumCase.read(AGE_ASSESSMENT, YesOrNo.class);
        if (isAgeAssessmentAppeal.equals(Optional.of(YesOrNo.YES))) {
            String appellantDobStr = asylumCase.read(AA_APPELLANT_DATE_OF_BIRTH, String.class)
                .orElseThrow(() -> new RequiredFieldMissingException("Appellant Date of Birth missing (Age Assessment)"));

            Optional<LocalDate> maybeAppellantDob = parseDate(appellantDobStr);
            maybeAppellantDob.ifPresent(dateToCheck -> {
                if (dateToCheck.isAfter(LocalDate.now())) {
                    response.addError("The date must not be a future date.");
                }
            });
        }
        return response;
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
