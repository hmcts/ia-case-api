package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.CaseDataMap;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

@Component
public class AppellantNameForDisplayFormatter implements PreSubmitCallbackHandler<CaseDataMap> {

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

        final String appellantGivenNames =
            CaseDataMap
                .getAppellantGivenNames()
                .orElseThrow(() -> new IllegalStateException("appellantGivenNames is not present"));

        final String appellantFamilyName =
            CaseDataMap
                .getAppellantFamilyName()
                .orElseThrow(() -> new IllegalStateException("appellantFamilyName is not present"));

        String appellantNameForDisplay = appellantGivenNames + " " + appellantFamilyName;

        CaseDataMap.setAppellantNameForDisplay(
            appellantNameForDisplay.replaceAll("\\s+", " ").trim()
        );

        return new PreSubmitCallbackResponse<>(CaseDataMap);
    }
}
