package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.applyforcosts;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ApplyForCosts;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.CostsDecision;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.ApplyForCostsAppender;

@Component
public class ConsiderMakingCostsOrderHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final ApplyForCostsAppender applyForCostsAppender;

    public ConsiderMakingCostsOrderHandler(ApplyForCostsAppender applyForCostsAppender) {
        this.applyForCostsAppender = applyForCostsAppender;
    }

    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
            && callback.getEvent() == Event.CONSIDER_MAKING_COSTS_ORDER;
    }

    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        String consideredCostsType = asylumCase.read(JUDGE_APPLIED_COSTS_TYPES, DynamicList.class)
            .orElseThrow(() -> new IllegalStateException("judgeAppliedCostsTypes is not present"))
            .getValue()
            .getLabel();

        String respondentToCostsOrder = asylumCase.read(RESPONDENT_TO_COSTS_ORDER, String.class)
            .orElseThrow(() -> new IllegalStateException("respondentToCostsOrder is not present"));

        String tribunalConsideringReason = asylumCase.read(TRIBUNAL_CONSIDERING_REASON, String.class)
            .orElseThrow(() -> new IllegalStateException("tribunalConsideringReason is not present"));

        Optional<List<IdValue<Document>>> judgeEvidenceForCostsOrder = asylumCase.read(JUDGE_EVIDENCE_FOR_COSTS_ORDER);

        Optional<List<IdValue<ApplyForCosts>>> maybeExistingApplyForCosts = asylumCase.read(APPLIES_FOR_COSTS);

        final List<IdValue<ApplyForCosts>> existingAppliesForCosts = maybeExistingApplyForCosts.orElse(Collections.emptyList());

        List<IdValue<ApplyForCosts>> allApplyForCosts =
            applyForCostsAppender.append(
                existingAppliesForCosts,
                CostsDecision.PENDING.toString(),
                consideredCostsType,
                tribunalConsideringReason,
                judgeEvidenceForCostsOrder.orElse(Collections.emptyList()),
                respondentToCostsOrder
            );

        asylumCase.write(APPLIES_FOR_COSTS, allApplyForCosts);
        //Flag if the event has been completed
        asylumCase.write(IS_APPLIED_FOR_COSTS, YesOrNo.YES);

        asylumCase.clear(JUDGE_APPLIED_COSTS_TYPES);
        asylumCase.clear(RESPONDENT_TO_COSTS_ORDER);
        asylumCase.clear(TRIBUNAL_CONSIDERING_REASON);
        asylumCase.clear(JUDGE_EVIDENCE_FOR_COSTS_ORDER);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
