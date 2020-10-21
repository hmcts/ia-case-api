package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.UserDetailsProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.MakeAnApplication;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.MakeAnApplicationDecision;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;

@Component
public class DecideAnApplicationHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final FeatureToggler featureToggler;
    private final DateProvider dateProvider;
    private final UserDetailsProvider userDetailsProvider;

    public DecideAnApplicationHandler(
        DateProvider dateProvider,
        UserDetailsProvider userDetailsProvider,
        FeatureToggler featureToggler) {
        this.dateProvider = dateProvider;
        this.userDetailsProvider = userDetailsProvider;
        this.featureToggler = featureToggler;
    }

    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage,
                             Callback<AsylumCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
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

        DynamicList maybeMakeAnApplicationsList = asylumCase.read(MAKE_AN_APPLICATIONS_LIST, DynamicList.class)
            .orElseThrow(() -> new IllegalStateException("Make an applications list not present"));

        String applicationId = maybeMakeAnApplicationsList.getValue().getCode();

        MakeAnApplicationDecision decision = asylumCase.read(MAKE_AN_APPLICATION_DECISION, MakeAnApplicationDecision.class)
            .orElseThrow(() -> new IllegalStateException("No application decision is present"));
        String decisionReason = asylumCase.read(MAKE_AN_APPLICATION_DECISION_REASON, String.class)
            .orElseThrow(() -> new IllegalStateException("No application decision reason is present"));
        String decisionMakerRole = userDetailsProvider.getLoggedInUserRoleLabel().toString();

        Optional<List<IdValue<MakeAnApplication>>> mayBeMakeAnApplications = asylumCase.read(MAKE_AN_APPLICATIONS);

        mayBeMakeAnApplications
            .orElse(Collections.emptyList())
            .stream()
            .filter(a -> a.getId().equals(applicationId))
            .forEach(application -> {
                MakeAnApplication makeAnApplication = application.getValue();
                makeAnApplication.setDecision(decision.toString());
                makeAnApplication.setDecisionReason(decisionReason);
                makeAnApplication.setDecisionDate(dateProvider.now().toString());
                makeAnApplication.setDecisionMaker(decisionMakerRole);
                asylumCase.write(HAS_APPLICATIONS_TO_DECIDE, YesOrNo.NO);
            });

        mayBeMakeAnApplications
            .orElse(Collections.emptyList())
            .stream()
            .filter(a -> a.getValue().getDecision().equals("Pending"))
            .findAny().ifPresent(a -> asylumCase.write(HAS_APPLICATIONS_TO_DECIDE, YesOrNo.YES));

        asylumCase.write(DECIDE_AN_APPLICATION_ID, applicationId);
        asylumCase.write(MAKE_AN_APPLICATIONS, mayBeMakeAnApplications);

        asylumCase.clear(MAKE_AN_APPLICATIONS_LIST);
        asylumCase.clear(MAKE_AN_APPLICATION_FIELDS);
        asylumCase.clear(MAKE_AN_APPLICATION_DECISION);
        asylumCase.clear(MAKE_AN_APPLICATION_DECISION_REASON);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
