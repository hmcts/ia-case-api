package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumExtractor.*;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.CaseDataMap;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CheckValues;
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

        final CaseDataMap caseDataMap =
            callback
                .getCaseDetails()
                .getCaseData();

        Set<String> appealGrounds = new LinkedHashSet<>();

        Optional<CheckValues<String>> maybeAppealGroundsProtection = caseDataMap.get(APPEAL_GROUNDS_PROTECTION);

        maybeAppealGroundsProtection
            .ifPresent(appealGroundsProtection ->
                appealGrounds.addAll(appealGroundsProtection.getValues()));

        Optional<CheckValues<String>> maybeAppealGroundsHumanRights = caseDataMap.get(APPEAL_GROUNDS_HUMAN_RIGHTS);

        maybeAppealGroundsHumanRights.ifPresent(appealGroundsHumanRights ->
                appealGrounds.addAll(appealGroundsHumanRights.getValues()));

        Optional<CheckValues<String>> maybeAppealGroundsRevocation = caseDataMap.get(APPEAL_GROUNDS_REVOCATION);

        maybeAppealGroundsRevocation
            .ifPresent(appealGroundsRevocation -> appealGrounds.addAll(appealGroundsRevocation.getValues()));

        caseDataMap.write(
            APPEAL_GROUNDS_FOR_DISPLAY,
            new ArrayList<>(appealGrounds)
        );

        return new PreSubmitCallbackResponse<>(caseDataMap);
    }
}
