package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_DATE_OF_BIRTH;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.EDIT_APPEAL_AFTER_SUBMIT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.SUBMIT_APPEAL;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;

import java.time.LocalDate;
import java.time.Period;
import java.util.Arrays;
import java.util.List;
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

    private LocalDate appellantDob;

    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");
        List<Event> validEvents = Arrays.asList(SUBMIT_APPEAL, EDIT_APPEAL_AFTER_SUBMIT);
        return ABOUT_TO_SUBMIT.equals(callbackStage) && validEvents.contains(callback.getEvent());
    }

    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(PreSubmitCallbackStage callbackStage,
                                                        Callback<AsylumCase> callback) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        setAppellantMinorFlag(asylumCase);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private void setAppellantMinorFlag(AsylumCase asylumCase) {
        if (isAppellantMinor(asylumCase)) {
            asylumCase.write(AsylumCaseFieldDefinition.IS_APPELLANT_MINOR, YesOrNo.YES);
        } else {
            asylumCase.write(AsylumCaseFieldDefinition.IS_APPELLANT_MINOR, YesOrNo.NO);
        }
    }

    private boolean isAppellantDobValid(AsylumCase asylumCase) {
        String appellantDobAsString = asylumCase.read(APPELLANT_DATE_OF_BIRTH, String.class).orElse(null);
        if (appellantDobAsString != null) {
            return isAppellantDobAValidDate(appellantDobAsString);
        } else {
            return false;
        }
    }

    private boolean isAppellantDobAValidDate(String appellantDobAsString) {
        try {
            appellantDob = LocalDate.parse(appellantDobAsString);
        } catch (Exception e) {
            log.warn("Error when parsing Appellant dob: ", e);
            return false;
        }
        return true;
    }

    private boolean isAppellantMinor(AsylumCase asylumCase) {
        YesOrNo result = YesOrNo.NO;
        if (isAppellantDobValid(asylumCase)) {
            result = Period.between(appellantDob, LocalDate.now()).getYears() < 18 ? YesOrNo.YES : YesOrNo.NO;
        }
        return result.equals(YesOrNo.YES);
    }
}
