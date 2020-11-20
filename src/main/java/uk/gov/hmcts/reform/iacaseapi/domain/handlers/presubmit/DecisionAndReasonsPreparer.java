package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;

@Component
public class DecisionAndReasonsPreparer implements PreSubmitCallbackHandler<AsylumCase> {

    private final FeatureToggler featureToggler;

    public DecisionAndReasonsPreparer(FeatureToggler featureToggler) {
        this.featureToggler = featureToggler;
    }

    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_START
               && callback.getEvent() == Event.DECISION_AND_REASONS_STARTED
               && featureToggler.getValue("reheard-feature", false);
    }

    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {

        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final AsylumCase asylumCase = callback
            .getCaseDetails()
            .getCaseData();

        if (asylumCase.read(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS, YesOrNo.class).map(flag -> flag.equals(YesOrNo.YES)).orElse(false)) {
            asylumCase.clear(CASE_INTRODUCTION_DESCRIPTION);
            asylumCase.clear(APPELLANT_CASE_SUMMARY_DESCRIPTION);
            asylumCase.clear(IMMIGRATION_HISTORY_AGREEMENT);
            asylumCase.clear(AGREED_IMMIGRATION_HISTORY_DESCRIPTION);
            asylumCase.clear(APPELLANTS_DISPUTED_SCHEDULE_OF_ISSUES_DESCRIPTION);
            asylumCase.clear(APPELLANTS_AGREED_SCHEDULE_OF_ISSUES_DESCRIPTION);
            asylumCase.clear(SCHEDULE_OF_ISSUES_AGREEMENT);
            asylumCase.clear(SCHEDULE_OF_ISSUES_DISAGREEMENT_DESCRIPTION);
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
