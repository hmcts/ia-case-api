package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealStatus.REINSTATED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.UserDetailsHelper;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackStateHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;

@Component
public class ReinstateAppealStateHandler implements PreSubmitCallbackStateHandler<AsylumCase> {

    private final FeatureToggler featureToggler;
    private final DateProvider dateProvider;
    private final UserDetails userDetails;
    private final UserDetailsHelper userDetailsHelper;


    private static final String REINSTATE_FEATURE = "reinstate-feature";

    public ReinstateAppealStateHandler(FeatureToggler featureToggler,
                                       DateProvider dateProvider,
                                       UserDetails userDetails,
                                       UserDetailsHelper userDetailsHelper
    ) {
        this.featureToggler = featureToggler;
        this.dateProvider = dateProvider;
        this.userDetails = userDetails;
        this.userDetailsHelper = userDetailsHelper;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && callback.getEvent() == Event.REINSTATE_APPEAL
               && featureToggler.getValue(REINSTATE_FEATURE, false);
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback,
        PreSubmitCallbackResponse<AsylumCase> callbackResponse
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase =
            callback
                .getCaseDetails()
                .getCaseData();

        final Optional<State> stateBeforeEndAppeal = asylumCase.read(STATE_BEFORE_END_APPEAL, State.class);

        if (stateBeforeEndAppeal.isEmpty()) {
            final PreSubmitCallbackResponse<AsylumCase> asylumCasePreSubmitCallbackResponse = new PreSubmitCallbackResponse<>(asylumCase);
            asylumCasePreSubmitCallbackResponse.addError("The appeal cannot be reinstated");
            return asylumCasePreSubmitCallbackResponse;
        }

        asylumCase.write(REINSTATED_DECISION_MAKER, userDetailsHelper.getLoggedInUserRoleLabel(userDetails).toString());
        asylumCase.write(APPEAL_STATUS, REINSTATED);
        asylumCase.write(REINSTATE_APPEAL_DATE, dateProvider.now().toString());
        asylumCase.write(RECORD_APPLICATION_ACTION_DISABLED, YesOrNo.NO);

        return new PreSubmitCallbackResponse<>(asylumCase, stateBeforeEndAppeal.get());
    }
}
