package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.CaseDataMap;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

@Component
public class AppealGroundsForDisplayFormatter implements PreSubmitCallbackHandler<CaseDataMap> {

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<CaseDataMap> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT;
    }

    public PreSubmitCallbackResponse<CaseDataMap> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<CaseDataMap> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final CaseDataMap CaseDataMap =
            callback
                .getCaseDetails()
                .getCaseData();

        Set<String> appealGrounds = new LinkedHashSet<>();

        CaseDataMap
            .getAppealGroundsProtection()
            .ifPresent(appealGroundsProtection ->
                appealGrounds.addAll(appealGroundsProtection.getValues())
            );

        CaseDataMap
            .getAppealGroundsHumanRights()
            .ifPresent(appealGroundsHumanRights ->
                appealGrounds.addAll(appealGroundsHumanRights.getValues())
            );

        CaseDataMap
            .getAppealGroundsRevocation()
            .ifPresent(appealGroundsRevocation ->
                appealGrounds.addAll(appealGroundsRevocation.getValues())
            );

        CaseDataMap.setAppealGroundsForDisplay(
            new ArrayList<>(appealGrounds)
        );

        return new PreSubmitCallbackResponse<>(CaseDataMap);
    }
}
