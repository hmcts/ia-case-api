package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.DECIDE_AN_APPLICATION_ID;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HAS_APPLICATIONS_TO_DECIDE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.MAKE_AN_APPLICATIONS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.MAKE_AN_APPLICATIONS_LIST;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.MAKE_AN_APPLICATION_DECISION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.MAKE_AN_APPLICATION_DECISION_REASON;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.MAKE_AN_APPLICATION_FIELDS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.MANUAL_CANCEL_HEARINGS_REQUIRED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.MakeAnApplicationDecision.GRANTED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.MakeAnApplicationTypes.CHANGE_DECISION_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State.DECISION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State.FINAL_BUNDLING;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State.LISTING;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State.PREPARE_FOR_HEARING;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State.PRE_HEARING;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.UserDetailsHelper;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.MakeAnApplication;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.MakeAnApplicationDecision;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.IaHearingsApiService;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.ServiceResponseException;

@Component
public class DecideAnApplicationHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private static final Set<State> STATES_FOR_HEARING_CANCELLATION = Set.of(
        LISTING,
        PREPARE_FOR_HEARING,
        FINAL_BUNDLING,
        PRE_HEARING,
        DECISION
    );
    private static final String HEARING_DELETION_CALLBACK_ERROR = "Could not delete some hearing request(s)";
    private final DateProvider dateProvider;
    private final UserDetails userDetails;
    private final UserDetailsHelper userDetailsHelper;
    private final FeatureToggler featureToggler;
    private final IaHearingsApiService iaHearingsApiService;
    private final String oldLegalOfficerDisplayName = "Tribunal Caseworker";
    private final String newLegalOfficerDisplayName = "Legal Officer";

    public DecideAnApplicationHandler(
        DateProvider dateProvider,
        UserDetails userDetails,
        UserDetailsHelper userDetailsHelper,
        FeatureToggler featureToggler,
        IaHearingsApiService iaHearingsApiService) {
        this.dateProvider = dateProvider;
        this.userDetails = userDetails;
        this.userDetailsHelper = userDetailsHelper;
        this.featureToggler = featureToggler;
        this.iaHearingsApiService = iaHearingsApiService;
    }

    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage,
                             Callback<AsylumCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && callback.getEvent() == Event.DECIDE_AN_APPLICATION;
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
        String decisionMakerRole = userDetailsHelper.getLoggedInUserRoleLabel(userDetails).toString();
        String updatedDecisionMakerRole = decisionMakerRole.equals(oldLegalOfficerDisplayName) ? newLegalOfficerDisplayName : decisionMakerRole;

        Optional<List<IdValue<MakeAnApplication>>> mayBeMakeAnApplications = asylumCase.read(MAKE_AN_APPLICATIONS);
        PreSubmitCallbackResponse<AsylumCase> response = new PreSubmitCallbackResponse<>(asylumCase);

        boolean isIntegrated = HandlerUtils.isIntegrated(asylumCase);

        State currentState = callback.getCaseDetails().getState();

        mayBeMakeAnApplications
            .orElse(Collections.emptyList())
            .stream()
            .filter(a -> a.getId().equals(applicationId))
            .forEach(application -> {
                MakeAnApplication makeAnApplication = application.getValue();
                setDecisionInfo(makeAnApplication, decision.toString(), decisionReason, dateProvider.now().toString(), updatedDecisionMakerRole);
                if (isIntegrated && isHearingDeletionNecessary(makeAnApplication, currentState)) {
                    delegateToIaHearingsApi(callback, response);
                }
                asylumCase.write(HAS_APPLICATIONS_TO_DECIDE, NO);
                if (featureToggler.getValue("wa-R2-feature", false)) {
                    asylumCase.write(AsylumCaseFieldDefinition.LAST_MODIFIED_APPLICATION, makeAnApplication);
                }
            });

        mayBeMakeAnApplications
            .orElse(Collections.emptyList())
            .stream()
            .filter(a -> a.getValue().getDecision().equals("Pending"))
            .findAny().ifPresent(a -> asylumCase.write(HAS_APPLICATIONS_TO_DECIDE, YES));

        asylumCase.write(DECIDE_AN_APPLICATION_ID, applicationId);
        asylumCase.write(MAKE_AN_APPLICATIONS, mayBeMakeAnApplications);

        asylumCase.clear(MAKE_AN_APPLICATIONS_LIST);
        asylumCase.clear(MAKE_AN_APPLICATION_FIELDS);
        asylumCase.clear(MAKE_AN_APPLICATION_DECISION);
        asylumCase.clear(MAKE_AN_APPLICATION_DECISION_REASON);

        return response;
    }

    public void setDecisionInfo(MakeAnApplication makeAnApplication, String decision, String decisionReason, String decisionDate, String decisionMakerRole) {
        makeAnApplication.setDecision(decision);
        makeAnApplication.setDecisionReason(decisionReason);
        makeAnApplication.setDecisionDate(decisionDate);
        makeAnApplication.setDecisionMaker(decisionMakerRole);
    }

    private void delegateToIaHearingsApi(Callback<AsylumCase> callback,
                                         PreSubmitCallbackResponse<AsylumCase> response) {
        try {
            AsylumCase asylumCase = iaHearingsApiService.aboutToSubmit(callback);
            if (!isDeletionRequestSuccessful(asylumCase)) {
                response.addError(HEARING_DELETION_CALLBACK_ERROR);
            }
        } catch (ServiceResponseException e) {
            response.addError(HEARING_DELETION_CALLBACK_ERROR);
        }
    }

    private boolean isHearingDeletionNecessary(MakeAnApplication makeAnApplication, State state) {

        return makeAnApplication.getType().equals(CHANGE_DECISION_TYPE.toString())
               && makeAnApplication.getDecision().equals(GRANTED.toString())
               && STATES_FOR_HEARING_CANCELLATION.contains(state);
    }

    private boolean isDeletionRequestSuccessful(AsylumCase asylumCase) {
        return asylumCase.read(MANUAL_CANCEL_HEARINGS_REQUIRED, YesOrNo.class)
            .map(yesOrNo -> !yesOrNo.equals(YES))
            .orElse(true);
    }
}

