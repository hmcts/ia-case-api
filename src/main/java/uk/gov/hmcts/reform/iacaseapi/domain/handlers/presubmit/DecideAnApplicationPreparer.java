package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;

@Component
public class DecideAnApplicationPreparer implements PreSubmitCallbackHandler<AsylumCase> {

    private final FeatureToggler featureToggler;

    public DecideAnApplicationPreparer(FeatureToggler featureToggler) {
        this.featureToggler = featureToggler;
    }

    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage,
                             Callback<AsylumCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_START
               && callback.getEvent() == Event.DECIDE_AN_APPLICATION
               && featureToggler.getValue("make-an-application-feature", false);
    }

    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(PreSubmitCallbackStage callbackStage,
                                                        Callback<AsylumCase> callback) {

        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        Optional<List<IdValue<MakeAnApplication>>> mayBeMakeAnApplications = asylumCase.read(MAKE_AN_APPLICATIONS);

        List<Value> makeAnApplicationsListElements = new ArrayList<>();
        AtomicInteger counter = new AtomicInteger(1);
        mayBeMakeAnApplications
            .orElse(Collections.emptyList())
            .stream()
            .forEach(idValue -> {
                if (idValue.getValue().getDecision().equals("Pending")) {
                    makeAnApplicationsListElements.add(
                        new Value(idValue.getId(), idValue.getValue().getApplicant() + " : Application " + counter));
                }
                counter.getAndIncrement();
            });

        if (makeAnApplicationsListElements.isEmpty()) {
            PreSubmitCallbackResponse<AsylumCase> callbackResponse = new PreSubmitCallbackResponse<>(asylumCase);
            callbackResponse.addError("There are no applications to decide.");
            return callbackResponse;
        }

        DynamicList dynamicList = new DynamicList(makeAnApplicationsListElements.get(0), makeAnApplicationsListElements);

        asylumCase.write(MAKE_AN_APPLICATIONS_LIST, dynamicList);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
