package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.applyforcosts;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import com.microsoft.applicationinsights.boot.dependencies.apachecommons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ApplyForCosts;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.TypesOfAppliedCosts;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.ApplyForCostsHandlerAppender;

@Component
public class ApplyForCostsHandler implements PreSubmitCallbackHandler<AsylumCase> {
    private ApplyForCostsHandlerAppender applyForCostsHandlerAppender;

    public ApplyForCostsHandler(ApplyForCostsHandlerAppender applyForCostsHandlerAppender) {
        this.applyForCostsHandlerAppender = applyForCostsHandlerAppender;
    }

    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                && callback.getEvent() == Event.APPLY_FOR_COSTS;
    }

    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        TypesOfAppliedCosts typeOfAppliedCosts = asylumCase.read(APPLIED_COSTS_TYPES, TypesOfAppliedCosts.class)
                .orElseThrow(() -> new IllegalStateException("typesOfAppliedCosts is not present"));

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

        Optional<List<IdValue<ApplyForCosts>>> maybeExistingMakeAnApplications =
                asylumCase.read(APPLIES_FOR_COSTS);

        final List<IdValue<ApplyForCosts>> existingAppliesForCosts =
                maybeExistingMakeAnApplications.orElse(Collections.emptyList());

        List<IdValue<ApplyForCosts>> allApplyForCosts =
                applyForCostsHandlerAppender.append(
                        existingAppliesForCosts,
                        typeOfAppliedCosts,
                        argumentsAndEvidenceDetails.orElse(StringUtils.EMPTY),
                        argumentsAndEvidenceDocuments.orElseThrow(() -> new IllegalStateException("argumentsAndEvidenceDocuments are not present")),
                        scheduleOfCostsDocuments.orElse(Collections.emptyList()),
                        applyForCostsHearingType,
                        applyForCostsHearingTypeExplanation,
                        "Pending"
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

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
