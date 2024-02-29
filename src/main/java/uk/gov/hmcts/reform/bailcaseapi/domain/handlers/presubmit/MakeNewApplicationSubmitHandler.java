package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.IS_IMA_ENABLED;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.bailcaseapi.domain.handlers.PreSubmitCallbackStateHandler;
import uk.gov.hmcts.reform.bailcaseapi.domain.service.FeatureToggler;
import uk.gov.hmcts.reform.bailcaseapi.domain.service.MakeNewApplicationService;

@Slf4j
@Component
public class MakeNewApplicationSubmitHandler implements PreSubmitCallbackStateHandler<BailCase> {

    private final MakeNewApplicationService makeNewApplicationService;
    private final FeatureToggler featureToggler;

    public MakeNewApplicationSubmitHandler(MakeNewApplicationService makeNewApplicationService, FeatureToggler featureToggler) {
        this.makeNewApplicationService = makeNewApplicationService;
        this.featureToggler = featureToggler;
    }

    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<BailCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
            && callback.getEvent() == Event.MAKE_NEW_APPLICATION;
    }

    @Override
    public PreSubmitCallbackResponse<BailCase> handle(PreSubmitCallbackStage callbackStage, Callback<BailCase> callback,
                                                      PreSubmitCallbackResponse<BailCase> callbackResponse) {

        requireNonNull(callbackResponse, "callback must not be null");

        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final BailCase bailCase = callback.getCaseDetails().getCaseData();

        makeNewApplicationService.clearFieldsAboutToSubmit(bailCase);

        CaseDetails<BailCase> caseDetailsBefore = callback.getCaseDetailsBefore().orElse(null);

        if (caseDetailsBefore == null) {
            throw new IllegalStateException("Case details before missing");
        }

        BailCase detailsBefore = caseDetailsBefore.getCaseData();
        makeNewApplicationService.appendPriorApplication(bailCase, detailsBefore);

        //Because the ABOUT_TO_START handler doesn't persist the IMA_ENABLED field, we need to set it here
        YesOrNo isImaEnabled = featureToggler.getValue("ima-feature-flag", false) == true ? YesOrNo.YES : YesOrNo.NO;
        bailCase.write(IS_IMA_ENABLED, isImaEnabled);

        return new PreSubmitCallbackResponse<>(bailCase, State.APPLICATION_SUBMITTED);
    }
}
