package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.AUTO_REQUEST_HEARING;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CASE_FLAG_SET_ASIDE_REHEARD_EXISTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.isPanelRequired;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.AutoRequestHearingService;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReviewDraftHearingRequirementsHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final FeatureToggler featureToggler;
    private final AutoRequestHearingService autoRequestHearingService;

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && callback.getEvent() == Event.REVIEW_HEARING_REQUIREMENTS;
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {

        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase =
            callback
                .getCaseDetails()
                .getCaseData();

        asylumCase.write(AsylumCaseFieldDefinition.REVIEWED_HEARING_REQUIREMENTS, YesOrNo.YES);

        HandlerUtils.formatHearingAdjustmentResponses(asylumCase);

        if (featureToggler.getValue("reheard-feature", false)
            && asylumCase.read(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS, YesOrNo.class).map(flag -> flag.equals(YesOrNo.YES)).orElse(false)) {
            asylumCase.write(AsylumCaseFieldDefinition.LIST_CASE_HEARING_LENGTH_VISIBLE, YesOrNo.YES);
        }

        if (autoRequestHearingService.shouldAutoRequestHearing(asylumCase, canAutoRequest(asylumCase))) {
            return new PreSubmitCallbackResponse<>(
                autoRequestHearingService.autoCreateHearing(callback));
        }

        boolean isAcceleratedDetainedAppeal = HandlerUtils.isAcceleratedDetainedAppeal(asylumCase);

        if (isAcceleratedDetainedAppeal) {
            //For ADA case type - Set flag to no to remove event from being re-submittable
            asylumCase.write(ADA_HEARING_REQUIREMENTS_TO_REVIEW, YesOrNo.NO);
            //Set flag to Yes to enable updateHearingRequirementsEvent for ada cases
            asylumCase.write(ADA_HEARING_REQUIREMENTS_UPDATABLE, YesOrNo.YES);
        }

        boolean appealTransferredOutOfAda = asylumCase.read(HAS_TRANSFERRED_OUT_OF_ADA, YesOrNo.class)
            .map(yesOrNo -> yesOrNo.equals(YES))
            .orElse(false);

        if (appealTransferredOutOfAda) {
            asylumCase.write(ADA_EDIT_LISTING_AVAILABLE, YES);
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private boolean canAutoRequest(AsylumCase asylumCase) {

        boolean autoRequestHearing = asylumCase.read(AUTO_REQUEST_HEARING, YesOrNo.class)
            .map(autoRequest -> YES == autoRequest).orElse(true);

        return autoRequestHearing && !isPanelRequired(asylumCase);
    }
}
