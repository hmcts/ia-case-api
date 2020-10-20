package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.MakeAnApplicationTypesProvider;

@Component
public class MakeAnApplicationPreparer implements PreSubmitCallbackHandler<AsylumCase> {

    private final FeatureToggler featureToggler;
    private final MakeAnApplicationTypesProvider makeAnApplicationTypesProvider;

    public MakeAnApplicationPreparer(FeatureToggler featureToggler,
                                     MakeAnApplicationTypesProvider makeAnApplicationTypesProvider) {
        this.featureToggler = featureToggler;
        this.makeAnApplicationTypesProvider = makeAnApplicationTypesProvider;
    }

    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage,
                             Callback<AsylumCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_START
               && callback.getEvent() == Event.MAKE_AN_APPLICATION
               && featureToggler.getValue("make-an-application-feature", false);
    }

    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(PreSubmitCallbackStage callbackStage,
                                                        Callback<AsylumCase> callback) {

        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        DynamicList makeAnApplicationTypes = makeAnApplicationTypesProvider.getMakeAnApplicationTypes(callback);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            new PreSubmitCallbackResponse<>(asylumCase);

        asylumCase.write(MAKE_AN_APPLICATION_TYPES, makeAnApplicationTypes);

        return callbackResponse;
    }
}
