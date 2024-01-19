package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.AUTO_REQUEST_HEARING;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CASE_FLAG_SET_ASIDE_REHEARD_EXISTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.MANUAL_CREATE_HEARING_REQUIRED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;

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
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.IaHearingsApiService;
import uk.gov.hmcts.reform.iacaseapi.domain.service.LocationBasedFeatureToggler;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.AsylumCaseServiceResponseException;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReviewDraftHearingRequirementsHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final FeatureToggler featureToggler;
    private final LocationBasedFeatureToggler locationBasedFeatureToggler;
    private final IaHearingsApiService iaHearingsApiService;

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

        if (shouldAutoRequestHearing(asylumCase)) {
            try {
                AsylumCase updatedAsylumCase = iaHearingsApiService.aboutToSubmit(callback);
                return new PreSubmitCallbackResponse<>(updatedAsylumCase);

            } catch (AsylumCaseServiceResponseException e) {
                log.error("Failure in call to IA-HEARINGS-API during event {} with error: {}",
                    callback.getEvent().toString(),
                    e.getMessage());

                asylumCase.write(MANUAL_CREATE_HEARING_REQUIRED, YesOrNo.YES);
            }
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private boolean shouldAutoRequestHearing(AsylumCase asylumCase) {
        boolean autoRequestHearing = asylumCase.read(AUTO_REQUEST_HEARING, YesOrNo.class)
            .map(autoRequest -> YES == autoRequest).orElse(false);
        boolean autoRequestHearingEnabled = locationBasedFeatureToggler.isAutoHearingRequestEnabled(asylumCase) == YES;

        return autoRequestHearingEnabled && autoRequestHearing;
    }
}
