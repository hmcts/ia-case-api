package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.CcdSupplementaryUpdater;

import static java.util.Objects.requireNonNull;

@Component
public class AsylumSupplementaryDataFixingHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final CcdSupplementaryUpdater ccdSupplementaryUpdater;

    public AsylumSupplementaryDataFixingHandler(CcdSupplementaryUpdater ccdSupplementaryUpdater) {
        this.ccdSupplementaryUpdater = ccdSupplementaryUpdater;
    }

    @Override
    public DispatchPriority getDispatchPriority() {
        return DispatchPriority.EARLIEST;
    }

    @Override
    public boolean canHandle(
            PreSubmitCallbackStage callbackStage,
            Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                && callback.getEvent() != Event.START_APPEAL;
    }

    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(
            PreSubmitCallbackStage callbackStage,
            Callback<AsylumCase> callback
    ) {
        final CaseDetails<AsylumCase> caseDetails = callback.getCaseDetails();
        final AsylumCase asylumCase = caseDetails.getCaseData();

        Map<String, JsonNode> supplementaryData = caseDetails.getSupplementaryData();

        if (supplementaryData == null || !supplementaryData.containsKey(CcdSupplementaryUpdater.HMCTS_SERVICE_ID)) {
            ccdSupplementaryUpdater.setHmctsServiceIdSupplementary(callback);
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

}
