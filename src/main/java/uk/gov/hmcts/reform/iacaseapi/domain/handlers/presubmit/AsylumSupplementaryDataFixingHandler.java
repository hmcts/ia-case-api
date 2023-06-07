package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.*;

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.UserDetailsProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.CcdSupplementaryUpdater;

@Component
public class AsylumSupplementaryDataFixingHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private static final String CITIZEN = "citizen";

    private final CcdSupplementaryUpdater ccdSupplementaryUpdater;
    private final UserDetailsProvider userDetailsProvider;

    public AsylumSupplementaryDataFixingHandler(CcdSupplementaryUpdater ccdSupplementaryUpdater,
                                                UserDetailsProvider userDetailsProvider) {
        this.ccdSupplementaryUpdater = ccdSupplementaryUpdater;
        this.userDetailsProvider = userDetailsProvider;
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

        Event event = callback.getEvent();
        boolean isCitizen = userDetailsProvider.getUserDetails().getRoles().contains(CITIZEN);

        return (callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT && event != START_APPEAL) || !isCitizen;
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
