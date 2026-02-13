package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.HomeOfficeApi;


@Component
@Slf4j
public class GetHomeOfficeAppellantDataHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final FeatureToggler featureToggler;
    private final HomeOfficeApi<AsylumCase> homeOfficeApi;

    public GetHomeOfficeAppellantDataHandler(
        FeatureToggler featureToggler,
        HomeOfficeApi<AsylumCase> homeOfficeApi
    ) {
        this.featureToggler = featureToggler;
        this.homeOfficeApi = homeOfficeApi;
    }

    @Override
    public DispatchPriority getDispatchPriority() {
        return DispatchPriority.LAST;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");
        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
            && callback.getEvent() == Event.GET_HOME_OFFICE_APPELLANT_DATA;
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {

        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        //AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        // Propagate event to ia-home-office-integration-api for the handlers to run there
        // AsylumCase asylumCaseWithHomeOfficeData =
        //     featureToggler.getValue("home-office-uan-feature", false)
        //         ? homeOfficeApi.aboutToSubmit(callback) : homeOfficeApi.call(callback);
        AsylumCase asylumCaseWithHomeOfficeData = homeOfficeApi.aboutToSubmit(callback);

        // >>> DO I HAVE TO WRITE THIS TO THE CASE as someone did below?  Or will returning the HO version suffice?
        // asylumCase.write(HOME_OFFICE_HEARING_BUNDLE_READY_INSTRUCT_STATUS,
        //     asylumCaseWithHomeOfficeData.read(HOME_OFFICE_HEARING_BUNDLE_READY_INSTRUCT_STATUS, String.class).orElse(""));

        return new PreSubmitCallbackResponse<>(asylumCaseWithHomeOfficeData);
    }

}
