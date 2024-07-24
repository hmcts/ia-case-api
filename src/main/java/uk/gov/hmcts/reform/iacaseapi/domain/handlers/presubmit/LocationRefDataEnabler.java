package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_CASE_USING_LOCATION_REF_DATA;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.START_APPEAL;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;

@Component
@RequiredArgsConstructor
@Slf4j
public class LocationRefDataEnabler  implements PreSubmitCallbackHandler<AsylumCase> {

    /*
    Setting a flag to denote the case as an appeal that uses Location Ref Data or not.
    Once the feature is universally adopted across an environment, this can be defaulted to
    "true" or can be refactored so that the feature isn't hidden behind this filter.
     */

    private static final String APPEALS_LOCATION_REFERENCE_DATA = "appeals-location-reference-data";

    private final FeatureToggler featureToggler;

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && callback.getEvent() == START_APPEAL;
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final AsylumCase asylumCase =
            callback
                .getCaseDetails()
                .getCaseData();

        boolean isEnabledForCurrentUser = featureToggler
            .getValue(APPEALS_LOCATION_REFERENCE_DATA, false);

        if (isEnabledForCurrentUser) {
            asylumCase.write(IS_CASE_USING_LOCATION_REF_DATA, YES);
        }

        log.info("Current user is creating a case that {} Location Ref Data",
            isEnabledForCurrentUser ? "USES" : "DOES NOT USE");

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

}
