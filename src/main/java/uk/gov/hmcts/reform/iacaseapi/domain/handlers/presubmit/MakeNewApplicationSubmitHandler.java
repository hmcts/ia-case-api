package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;

import static uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition.IS_IMA_ENABLED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackStateHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggleService;
import uk.gov.hmcts.reform.iacaseapi.domain.service.MakeNewApplicationService;

@Slf4j
@Component
public class MakeNewApplicationSubmitHandler implements PreSubmitCallbackStateHandler<BailCase> {

    private final MakeNewApplicationService makeNewApplicationService;
    private final FeatureToggleService featureToggleService;

    public MakeNewApplicationSubmitHandler(MakeNewApplicationService makeNewApplicationService,
                                           FeatureToggleService featureToggleService) {
        this.makeNewApplicationService = makeNewApplicationService;
        this.featureToggleService = featureToggleService;
    }

    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<BailCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
            && callback.getEvent() == Event.MAKE_NEW_APPLICATION;
    }

    @Override
    public PreSubmitCallbackResponse<BailCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<BailCase> callback,
        PreSubmitCallbackResponse<BailCase> callbackResponse
    ) {
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
        YesOrNo isImaEnabled = featureToggleService.imaEnabled() ? YES : NO;
        bailCase.write(IS_IMA_ENABLED, isImaEnabled);

        return new PreSubmitCallbackResponse<>(bailCase, State.APPLICATION_SUBMITTED);
    }
}
