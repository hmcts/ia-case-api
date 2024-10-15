package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.migratedcase;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_ARIA_MIGRATED_FILTER;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_ARIA_MIGRATED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.NO;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

@Component
public class StartAppealCreateCaseHandler implements PreSubmitCallbackHandler<AsylumCase> {

    public StartAppealCreateCaseHandler() {
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return (callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
            && Event.START_APPEAL == callback.getEvent());
    }

    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(PreSubmitCallbackStage callbackStage,
                                                        Callback<AsylumCase> callback) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        asylumCase.write(IS_ARIA_MIGRATED, NO);
        //isAriaMigratedFilter is used separately for case list filtering on ExUI
        asylumCase.write(IS_ARIA_MIGRATED_FILTER, NO);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}