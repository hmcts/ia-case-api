package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Value;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggleService;
import uk.gov.hmcts.reform.iacaseapi.domain.service.LocationRefDataService;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition.IS_BAILS_LOCATION_REFERENCE_DATA_ENABLED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition.REF_DATA_LISTING_LOCATION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;

@Slf4j
@Component
public class CaseListingPreparer implements PreSubmitCallbackHandler<BailCase> {

    private final FeatureToggleService featureToggleService;
    private final LocationRefDataService locationRefDataService;

    public CaseListingPreparer(FeatureToggleService featureToggleService, LocationRefDataService locationRefDataService) {
        this.featureToggleService = featureToggleService;
        this.locationRefDataService = locationRefDataService;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<BailCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_START
               && callback.getEvent() == Event.CASE_LISTING;
    }

    public PreSubmitCallbackResponse<BailCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<BailCase> callback
    ) {

        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final BailCase bailCase = callback.getCaseDetails().getCaseData();

        YesOrNo isBailsLocationReferenceDataEnabled = featureToggleService.locationRefDataEnabled() ? YES : NO;
        bailCase.write(IS_BAILS_LOCATION_REFERENCE_DATA_ENABLED, isBailsLocationReferenceDataEnabled);

        if (isBailsLocationReferenceDataEnabled == YES) {

            DynamicList refDataLocationDynamicList = locationRefDataService.getHearingLocationsDynamicList();

            Value selectedRefDataLocation = bailCase.read(REF_DATA_LISTING_LOCATION, DynamicList.class)
                .map(dynamicList -> dynamicList.getValue()).orElse(null);

            if (selectedRefDataLocation != null) {
                bailCase.write(
                    REF_DATA_LISTING_LOCATION,
                    new DynamicList(selectedRefDataLocation, refDataLocationDynamicList.getListItems())
                );
            } else {
                bailCase.write(REF_DATA_LISTING_LOCATION, refDataLocationDynamicList);
            }

        }

        return new PreSubmitCallbackResponse<>(bailCase);
    }
}
