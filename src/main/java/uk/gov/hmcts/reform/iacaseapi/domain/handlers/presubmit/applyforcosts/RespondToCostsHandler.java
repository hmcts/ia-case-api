package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.applyforcosts;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

@Component
public class RespondToCostsHandler implements PreSubmitCallbackHandler<AsylumCase> {

    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                && callback.getEvent() == Event.RESPOND_TO_COSTS;
    }

    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        DynamicList applyForCostsDynamicList = asylumCase.read(RESPOND_TO_COSTS_LIST, DynamicList.class)
                .orElseThrow(() -> new IllegalStateException("respondToCostsDynamicList is not present"));
        String applicationId = applyForCostsDynamicList.getValue().getCode();

        String response = asylumCase.read(RESPONSE_TO_APPLICATION_TEXT_AREA, String.class)
                .orElseThrow(() -> new IllegalStateException("No response to application is present"));
        Optional<List<IdValue<Document>>> evidenceDocuments = asylumCase.read(RESPONSE_TO_APPLICATION_EVIDENCE);
        Optional<String> typeOfHearingExplanation = asylumCase.read(TYPE_OF_HEARING_EXPLANATION, String.class);
        YesOrNo typeOfHearing = asylumCase.read(TYPE_OF_HEARING_OPTION, YesOrNo.class)
                .orElseThrow(() -> new IllegalStateException("typeOfHearing is not present"));

        Optional<List<IdValue<ApplyForCosts>>> mayBeApplyForCosts = asylumCase.read(APPLIES_FOR_COSTS);

        mayBeApplyForCosts
                .orElse(Collections.emptyList())
                .stream()
                .filter(applyForCosts -> applyForCosts.getId().equals(applicationId))
                .forEach(applyForCostsIdValue -> {
                    ApplyForCosts applyForCosts = applyForCostsIdValue.getValue();
                    applyForCosts.setResponseToApplication(response);
                    applyForCosts.setEvidence(evidenceDocuments.orElse(Collections.emptyList()));
                    applyForCosts.setResponseHearingType(typeOfHearing);
                    if (typeOfHearing == YesOrNo.YES) {
                        applyForCosts.setResponseHearingTypeExplanation(typeOfHearingExplanation.orElseThrow(() -> new IllegalStateException("typeOfHearingExplanation is not present")));
                    }
                });

        asylumCase.write(APPLIES_FOR_COSTS, mayBeApplyForCosts);
        asylumCase.clear(RESPONSE_TO_APPLICATION_TEXT_AREA);
        asylumCase.clear(RESPONSE_TO_APPLICATION_EVIDENCE);
        asylumCase.clear(TYPE_OF_HEARING_EXPLANATION);
        asylumCase.clear(TYPE_OF_HEARING_OPTION);
        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
