package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.CaseDataMap;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

@Component
public class HomeOfficeReferenceNumberTruncator implements PreSubmitCallbackHandler<CaseDataMap> {

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<CaseDataMap> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && (callback.getEvent() == Event.START_APPEAL || callback.getEvent() == Event.EDIT_APPEAL);
    }

    public PreSubmitCallbackResponse<CaseDataMap> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<CaseDataMap> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        CaseDataMap CaseDataMap =
            callback
                .getCaseDetails()
                .getCaseData();

        String homeOfficeReferenceNumber =
            CaseDataMap
                .getHomeOfficeReferenceNumber()
                .orElseThrow(() -> new RequiredFieldMissingException("homeOfficeReferenceNumber is not present"));

        if (homeOfficeReferenceNumber.contains("/") || homeOfficeReferenceNumber.length() > 8) {
            String truncatedReferenceNumber = homeOfficeReferenceNumber.split("/")[0];

            if (truncatedReferenceNumber.length() > 8) {
                truncatedReferenceNumber = truncatedReferenceNumber.substring(0, 8);
            }

            CaseDataMap.setHomeOfficeReferenceNumber(truncatedReferenceNumber);
        }

        return new PreSubmitCallbackResponse<>(CaseDataMap);
    }
}
