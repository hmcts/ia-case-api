package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.SEND_DECISIONS_AND_REASONS_DATE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.SEND_DECISION_AND_REASONS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.HearingDecisionProcessor;

@Component
public class SendDecisionAndReasonsHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final DateProvider dateProvider;
    private final HearingDecisionProcessor hearingDecisionProcessor;

    public SendDecisionAndReasonsHandler(
        DateProvider dateProvider,
        HearingDecisionProcessor hearingDecisionProcessor
    ) {
        this.dateProvider = dateProvider;
        this.hearingDecisionProcessor = hearingDecisionProcessor;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage.equals(ABOUT_TO_SUBMIT) && callback.getEvent().equals(SEND_DECISION_AND_REASONS);
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase =
            callback
                .getCaseDetails()
                .getCaseData();

        boolean isAcceleratedDetainedAppeal = HandlerUtils.isAcceleratedDetainedAppeal(asylumCase);

        if (isAcceleratedDetainedAppeal) {
            //Clear to remove access to event updateHearingRequirements for ada cases after submission
            asylumCase.clear(AsylumCaseFieldDefinition.ADA_HEARING_REQUIREMENTS_UPDATABLE);
            //Clear to remove access to event updateHearingAdjustments for ada cases after submission
            asylumCase.clear(AsylumCaseFieldDefinition.ADA_HEARING_ADJUSTMENTS_UPDATABLE);
            //Clear to remove access to event editCaseListing for ada cases after submission
            asylumCase.clear(AsylumCaseFieldDefinition.ADA_EDIT_LISTING_AVAILABLE);
        }

        asylumCase.write(SEND_DECISIONS_AND_REASONS_DATE,  dateProvider.now().toString());

        hearingDecisionProcessor.processHearingAppealDecision(callback.getCaseDetails().getCaseData());

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
