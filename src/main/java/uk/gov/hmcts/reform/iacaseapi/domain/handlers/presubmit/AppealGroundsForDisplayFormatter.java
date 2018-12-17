package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

@Component
public class AppealGroundsForDisplayFormatter implements PreSubmitCallbackHandler<AsylumCase> {

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT;
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final AsylumCase asylumCase =
            callback
                .getCaseDetails()
                .getCaseData();

        Set<String> appealGrounds = new LinkedHashSet<>();

        asylumCase
            .getAppealGroundsProtection()
            .ifPresent(appealGroundsProtection ->
                appealGrounds.addAll(appealGroundsProtection.getValues())
            );

        asylumCase
            .getAppealGroundsHumanRights()
            .ifPresent(appealGroundsHumanRights ->
                appealGrounds.addAll(appealGroundsHumanRights.getValues())
            );

        asylumCase
            .getAppealGroundsRevocation()
            .ifPresent(appealGroundsRevocation ->
                appealGrounds.addAll(appealGroundsRevocation.getValues())
            );

        asylumCase.setAppealGroundsForDisplay(
            new ArrayList<>(appealGrounds)
        );

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
