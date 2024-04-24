package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.applyforcosts;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.ApplyForCostsProvider;

import java.util.List;

@Component
public class RespondToCostsPreparer implements PreSubmitCallbackHandler<AsylumCase> {

    private final ApplyForCostsProvider applyForCostsProvider;

    public RespondToCostsPreparer(ApplyForCostsProvider applyForCostsProvider) {
        this.applyForCostsProvider = applyForCostsProvider;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_START
               && callback.getEvent() == Event.RESPOND_TO_COSTS;
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

        List<Value> applyForCostsList = applyForCostsProvider.getApplyForCostsForRespondent(asylumCase);
        if (applyForCostsList.isEmpty()) {
            response.addError("You do not have any cost applications to respond to.");
        } else {
            DynamicList dynamicList = new DynamicList(applyForCostsList.get(0), applyForCostsList);
            asylumCase.write(RESPOND_TO_COSTS_LIST, dynamicList);

        }

        return response;
    }
}