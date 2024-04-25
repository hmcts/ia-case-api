package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.applyforcosts;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ApplyForCosts;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.ApplyForCostsProvider;

@Component
public class AdditionalEvidenceForCostsHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final ApplyForCostsProvider applyForCostsProvider;

    public AdditionalEvidenceForCostsHandler(ApplyForCostsProvider applyForCostsProvider) {
        this.applyForCostsProvider = applyForCostsProvider;
    }

    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
            && callback.getEvent() == Event.ADD_EVIDENCE_FOR_COSTS;
    }

    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        DynamicList applyForCostsDynamicList = asylumCase.read(ADD_EVIDENCE_FOR_COSTS_LIST, DynamicList.class)
            .orElseThrow(() -> new IllegalStateException("addEvidenceForCostsList is not present"));

        String applicationId = applyForCostsDynamicList.getValue().getCode();

        Optional<List<IdValue<Document>>> evidenceDocuments = asylumCase.read(ADDITIONAL_EVIDENCE_FOR_COSTS);

        Optional<List<IdValue<ApplyForCosts>>> mayBeApplyForCosts = asylumCase.read(APPLIES_FOR_COSTS);

        String loggedUser = applyForCostsProvider.getLoggedUserRole();

        mayBeApplyForCosts
            .orElse(Collections.emptyList())
            .stream()
            .filter(applyForCosts -> applyForCosts.getId().equals(applicationId))
            .forEach(applyForCostsIdValue -> {
                ApplyForCosts applyForCosts = applyForCostsIdValue.getValue();
                applyForCosts.setLoggedUserRole(loggedUser);
                if (loggedUser.equals(applyForCosts.getApplyForCostsApplicantType())) {
                    applyForCosts.setApplicantAdditionalEvidence(addAdditionalEvidenceToList(applyForCosts, evidenceDocuments, true));
                } else if (loggedUser.equals(applyForCosts.getApplyForCostsRespondentRole())) {
                    applyForCosts.setRespondentAdditionalEvidence(addAdditionalEvidenceToList(applyForCosts, evidenceDocuments, false));
                }
            });

        asylumCase.write(APPLIES_FOR_COSTS, mayBeApplyForCosts);
        asylumCase.clear(ADDITIONAL_EVIDENCE_FOR_COSTS);
        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private List<IdValue<Document>> addAdditionalEvidenceToList(ApplyForCosts applyForCosts, Optional<List<IdValue<Document>>> evidenceDocuments, boolean isApplicant) {
        List<IdValue<Document>> additionalEvidences;
        if (isApplicant) {
            additionalEvidences = applyForCosts.getApplicantAdditionalEvidence() != null ? applyForCosts.getApplicantAdditionalEvidence() : new ArrayList<>();
        } else {
            additionalEvidences = applyForCosts.getRespondentAdditionalEvidence() != null ? applyForCosts.getRespondentAdditionalEvidence() : new ArrayList<>();
        }
        additionalEvidences.addAll(evidenceDocuments.orElseThrow(() -> new IllegalStateException("evidenceDocuments are not present")));
        return additionalEvidences;
    }
}
