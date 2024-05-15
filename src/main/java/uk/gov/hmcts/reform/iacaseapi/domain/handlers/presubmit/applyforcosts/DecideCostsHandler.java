package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.applyforcosts;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.ApplyForCostsProvider;

@Component
public class DecideCostsHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final ApplyForCostsProvider applyForCostsProvider;

    public DecideCostsHandler(ApplyForCostsProvider applyForCostsProvider) {
        this.applyForCostsProvider = applyForCostsProvider;
    }

    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
            && callback.getEvent() == Event.DECIDE_COSTS_APPLICATION;
    }

    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        DynamicList applyForCostsDynamicList = asylumCase.read(DECIDE_COSTS_APPLICATION_LIST, DynamicList.class)
            .orElseThrow(() -> new IllegalStateException("decideCostsApplicationList is not present"));

        String applicationId = applyForCostsDynamicList.getValue().getCode();

        CostsDecision costsDecision = asylumCase.read(APPLY_FOR_COSTS_DECISION, CostsDecision.class)
            .orElseThrow(() -> new IllegalStateException("applyForCostsDecision is not present"));

        CostsDecisionType costsDecisionType = asylumCase.read(COSTS_DECISION_TYPE, CostsDecisionType.class)
            .orElseThrow(() -> new IllegalStateException("costsDecisionType is not present"));

        Optional<List<IdValue<Document>>> uploadCostsOrder = asylumCase.read(UPLOAD_COSTS_ORDER);

        Optional<List<IdValue<ApplyForCosts>>> mayBeApplyForCosts = asylumCase.read(APPLIES_FOR_COSTS);

        mayBeApplyForCosts
            .orElse(Collections.emptyList())
            .stream()
            .filter(applyForCosts -> applyForCosts.getId().equals(applicationId))
            .forEach(applyForCostsIdValue -> {
                ApplyForCosts applyForCosts = applyForCostsIdValue.getValue();
                applyForCosts.setApplyForCostsDecision(costsDecision.toString());
                applyForCosts.setCostsDecisionType(costsDecisionType.toString());
                if (costsDecisionType == CostsDecisionType.WITH_AN_ORAL_HEARING) {
                    String costsOralHearingDate = asylumCase.read(COSTS_ORAL_HEARING_DATE, String.class)
                        .orElseThrow(() -> new IllegalStateException("costsOralHearingDate is not present"));
                    applyForCosts.setCostsOralHearingDate(applyForCostsProvider.formatDate(costsOralHearingDate));
                }
                applyForCosts.setUploadCostsOrder(uploadCostsOrder.orElseThrow(() -> new IllegalStateException("uploadCostsOrder is not present")));
                applyForCosts.setDateOfDecision(applyForCostsProvider.formatDate(LocalDate.now()));
            });

        asylumCase.write(APPLIES_FOR_COSTS, mayBeApplyForCosts);
        asylumCase.clear(APPLY_FOR_COSTS_DECISION);
        asylumCase.clear(COSTS_DECISION_TYPE);
        asylumCase.clear(COSTS_ORAL_HEARING_DATE);
        asylumCase.clear(UPLOAD_COSTS_ORDER);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
