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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

@Component
@Slf4j
public class MinorTagHandler implements PreSubmitCallbackHandler<AsylumCase> {

    @Override
    public DispatchPriority getDispatchPriority() {
        return DispatchPriority.EARLIEST;
    }

    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");
        List<Event> validEvents = Arrays.asList(SUBMIT_APPEAL, EDIT_APPEAL_AFTER_SUBMIT, Event.PAY_AND_SUBMIT_APPEAL);
        return ABOUT_TO_SUBMIT.equals(callbackStage) && validEvents.contains(callback.getEvent());
    }

    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(PreSubmitCallbackStage callbackStage,
                                                        Callback<AsylumCase> callback) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        if (isAppellantMinor(asylumCase)) {
            asylumCase.write(AsylumCaseFieldDefinition.IS_APPELLANT_MINOR, YesOrNo.YES);
        } else {
            asylumCase.write(AsylumCaseFieldDefinition.IS_APPELLANT_MINOR, YesOrNo.NO);
        }
        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private boolean isAppellantMinor(AsylumCase asylumCase) {
        String appellantDobAsString = asylumCase.read(APPELLANT_DATE_OF_BIRTH, String.class).orElse(null);
        if (appellantDobAsString != null) {
            try {
                return Period.between(LocalDate.parse(appellantDobAsString), LocalDate.now()).getYears() < 18;
            } catch (Exception e) {
                log.warn("Error when parsing Appellant dob: ", e);
                return false;
            }
        }
        return false;
    }
}
