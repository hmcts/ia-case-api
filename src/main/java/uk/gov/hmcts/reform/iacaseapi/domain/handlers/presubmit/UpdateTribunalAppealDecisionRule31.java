package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.UpdateTribunalRules.UNDER_RULE_31;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.NO;

import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

@Component
public class UpdateTribunalAppealDecisionRule31 implements PreSubmitCallbackHandler<AsylumCase> {

    private final DateProvider dateProvider;

    public UpdateTribunalAppealDecisionRule31(
        DateProvider dateProvider) {
        this.dateProvider = dateProvider;
    }

    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                && callback.getEvent() == Event.UPDATE_TRIBUNAL_DECISION;
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }
        final AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        if (isDecisionRule31(asylumCase)) {
            DynamicList updateTribunalDecisionValue = asylumCase.read(TYPES_OF_UPDATE_TRIBUNAL_DECISION, DynamicList.class)
                .orElseThrow(() -> new IllegalStateException("typesOfUpdateTribunalDecision is not present"));

            asylumCase.write(UPDATED_APPEAL_DECISION, StringUtils.capitalize(updateTribunalDecisionValue.getValue().getCode()));
            final Optional<Document> decisionAndReasonsDoc = asylumCase.read(DECISION_AND_REASON_DOCS_UPLOAD, Document.class);

            asylumCase.write(UPDATE_TRIBUNAL_DECISION_DATE, dateProvider.now().toString());

            YesOrNo isDecisionAndReasonDocumentBeingUpdated = asylumCase.read(AsylumCaseFieldDefinition.UPDATE_TRIBUNAL_DECISION_AND_REASONS_FINAL_CHECK, YesOrNo.class)
                .orElse(NO);


            if (isDecisionAndReasonDocumentBeingUpdated.equals(NO)) {
                if (decisionAndReasonsDoc.isPresent()) {
                    asylumCase.clear(DECISION_AND_REASON_DOCS_UPLOAD);
                }
            }
        }
        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private boolean isDecisionRule31(AsylumCase asylumCase) {
        return asylumCase.read(UPDATE_TRIBUNAL_DECISION_LIST, UpdateTribunalRules.class)
                .map(type -> type.equals(UNDER_RULE_31)).orElse(false);
    }
}