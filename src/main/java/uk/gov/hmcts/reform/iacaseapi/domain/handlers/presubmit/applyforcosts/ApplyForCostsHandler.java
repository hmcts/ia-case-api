package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.applyforcosts;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.isLegalRepJourney;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
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
public class ApplyForCostsHandler implements PreSubmitCallbackHandler<AsylumCase> {
    private final ApplyForCostsAppender applyForCostsAppender;

    public ApplyForCostsHandler(ApplyForCostsAppender applyForCostsAppender) {
        this.applyForCostsAppender = applyForCostsAppender;
    }

    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
            && callback.getEvent() == Event.APPLY_FOR_COSTS
            && isLegalRepJourney(callback.getCaseDetails().getCaseData());
    }

    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        String typeOfAppliedCosts = asylumCase.read(APPLIED_COSTS_TYPES, DynamicList.class)
            .orElseThrow(() -> new IllegalStateException("typesOfAppliedCosts is not present"))
            .getValue()
            .getLabel();

        Optional<String> argumentsAndEvidenceDetails = asylumCase.read(ARGUMENTS_AND_EVIDENCE_DETAILS, String.class);

        Optional<List<IdValue<Document>>> argumentsAndEvidenceDocuments = asylumCase.read(ARGUMENTS_AND_EVIDENCE_DOCUMENTS);

        Optional<List<IdValue<Document>>> scheduleOfCostsDocuments = asylumCase.read(SCHEDULE_OF_COSTS_DOCUMENTS);

        YesOrNo applyForCostsHearingType = asylumCase.read(APPLY_FOR_COSTS_HEARING_TYPE, YesOrNo.class)
            .orElseThrow(() -> new IllegalStateException("applyForCostsHearingType is not present"));

        String applyForCostsHearingTypeExplanation = "";

        if (applyForCostsHearingType.equals(YesOrNo.YES)) {
            applyForCostsHearingTypeExplanation = asylumCase.read(APPLY_FOR_COSTS_HEARING_TYPE_EXPLANATION, String.class)
                .orElseThrow(() -> new IllegalStateException("applyForCostsHearingTypeExplanation is not present"));
        }

        String legalRepName = asylumCase.read(LEGAL_REP_NAME, String.class)
            .orElseThrow(() -> new IllegalStateException("legalRepName is not present"));

        YesOrNo isApplyForCostsOot = asylumCase.read(IS_APPLY_FOR_COSTS_OOT, YesOrNo.class).orElse(YesOrNo.NO);

        Optional<List<IdValue<ApplyForCosts>>> maybeExistingApplyForCosts =
            asylumCase.read(APPLIES_FOR_COSTS);

        String applyForCostsOotExplanation = "";

        if (isApplyForCostsOot.equals(YesOrNo.YES)) {
            applyForCostsOotExplanation = asylumCase.read(APPLY_FOR_COSTS_OOT_EXPLANATION, String.class)
                .orElseThrow(() -> new IllegalStateException("applyForCostsOotExplanation is not present"));
        }

        Optional<List<IdValue<Document>>> ootUploadEvidenceDocuments = asylumCase.read(OOT_UPLOAD_EVIDENCE_DOCUMENTS);

        final List<IdValue<ApplyForCosts>> existingAppliesForCosts =
            maybeExistingApplyForCosts.orElse(Collections.emptyList());

        List<IdValue<ApplyForCosts>> allApplyForCosts =
            applyForCostsAppender.append(
                existingAppliesForCosts,
                typeOfAppliedCosts,
                argumentsAndEvidenceDetails.orElse(StringUtils.EMPTY),
                argumentsAndEvidenceDocuments.orElseThrow(() -> new IllegalStateException("argumentsAndEvidenceDocuments are not present")),
                scheduleOfCostsDocuments.orElse(Collections.emptyList()),
                applyForCostsHearingType,
                applyForCostsHearingTypeExplanation,
                CostsDecision.PENDING.toString(),
                legalRepName,
                applyForCostsOotExplanation,
                ootUploadEvidenceDocuments.orElse(Collections.emptyList()),
                isApplyForCostsOot
            );

        asylumCase.write(APPLIES_FOR_COSTS, allApplyForCosts);

        //Flag if the event has been completed
        asylumCase.write(IS_APPLIED_FOR_COSTS, YesOrNo.YES);

        asylumCase.clear(APPLIED_COSTS_TYPES);
        asylumCase.clear(ARGUMENTS_AND_EVIDENCE_DETAILS);
        asylumCase.clear(ARGUMENTS_AND_EVIDENCE_DOCUMENTS);
        asylumCase.clear(SCHEDULE_OF_COSTS_DOCUMENTS);
        asylumCase.clear(APPLY_FOR_COSTS_HEARING_TYPE);
        asylumCase.clear(APPLY_FOR_COSTS_HEARING_TYPE_EXPLANATION);
        asylumCase.clear(APPLY_FOR_COSTS_OOT_EXPLANATION);
        asylumCase.clear(OOT_UPLOAD_EVIDENCE_DOCUMENTS);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
