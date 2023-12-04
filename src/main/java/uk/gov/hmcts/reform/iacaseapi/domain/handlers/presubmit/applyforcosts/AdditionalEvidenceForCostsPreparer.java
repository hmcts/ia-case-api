package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.applyforcosts;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.ADD_EVIDENCE_FOR_COSTS_LIST;

import java.util.List;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Value;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.ApplyForCostsProvider;

@Component
public class AdditionalEvidenceForCostsPreparer implements PreSubmitCallbackHandler<AsylumCase> {

    private final ApplyForCostsProvider applyForCostsProvider;

    public AdditionalEvidenceForCostsPreparer(ApplyForCostsProvider applyForCostsProvider) {
        this.applyForCostsProvider = applyForCostsProvider;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_START
            && callback.getEvent() == Event.ADD_EVIDENCE_FOR_COSTS;
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase =
            callback
                .getCaseDetails()
                .getCaseData();

        PreSubmitCallbackResponse<AsylumCase> response = new PreSubmitCallbackResponse<>(asylumCase);

        List<Value> applyForCostsList = applyForCostsProvider.getApplyForCostsForApplicantOrRespondent(asylumCase);
        if (applyForCostsList.isEmpty()) {
            response.addError("You do not have any cost applications to add evidence to.");
        } else {
            DynamicList dynamicList = new DynamicList(applyForCostsList.get(0), applyForCostsList);
            asylumCase.write(ADD_EVIDENCE_FOR_COSTS_LIST, dynamicList);
        }

        return response;
    }
}
