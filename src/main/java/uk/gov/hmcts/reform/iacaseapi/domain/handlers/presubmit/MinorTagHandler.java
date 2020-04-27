package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_DATE_OF_BIRTH;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;

import java.time.LocalDate;
import java.time.Period;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

@Component
@Slf4j
public class MinorTagHandler implements PreSubmitCallbackHandler<AsylumCase> {
    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");
        return ABOUT_TO_SUBMIT.equals(callbackStage) && Event.SUBMIT_APPEAL.equals(callback.getEvent());
    }

    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(PreSubmitCallbackStage callbackStage,
                                                        Callback<AsylumCase> callback) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        asylumCase.write(AsylumCaseFieldDefinition.IS_APPELLANT_MINOR, isAppellantMinor(getAppellantDob(asylumCase)));

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private LocalDate getAppellantDob(AsylumCase asylumCase) {
        String adultAppellantFallback = "1979-02-15";
        String appellantDobAsString = asylumCase.read(APPELLANT_DATE_OF_BIRTH, String.class)
            .orElse(adultAppellantFallback);
        try {
            return LocalDate.parse(appellantDobAsString);
        } catch (Exception e) {
            log.warn("Error when parsing Appellant dob: ", e);
            return LocalDate.parse(adultAppellantFallback);
        }
    }

    private YesOrNo isAppellantMinor(LocalDate appellantDob) {
        return Period.between(appellantDob, LocalDate.now()).getYears() < 18 ? YesOrNo.YES : YesOrNo.NO;
    }
}
