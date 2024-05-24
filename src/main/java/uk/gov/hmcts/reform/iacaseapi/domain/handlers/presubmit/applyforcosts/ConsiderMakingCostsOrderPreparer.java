package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.applyforcosts;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.JUDGE_APPLIED_COSTS_TYPES;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.TypesOfAppliedCosts.*;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.UserDetailsHelper;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Value;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

@Component
public class ConsiderMakingCostsOrderPreparer implements PreSubmitCallbackHandler<AsylumCase> {

    private static final String ROLE_JUDGE = "caseworker-ia-iacjudge";
    private final UserDetailsHelper userDetailsHelper;
    private final UserDetails userDetails;

    public ConsiderMakingCostsOrderPreparer(UserDetailsHelper userDetailsHelper, UserDetails userDetails) {
        this.userDetailsHelper = userDetailsHelper;
        this.userDetails = userDetails;
    }

    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_START
            && callback.getEvent() == Event.CONSIDER_MAKING_COSTS_ORDER;
    }

    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        boolean isJudgeRole = ROLE_JUDGE.equals(userDetailsHelper.getLoggedInUserRole(userDetails).toString());

        final List<Value> values = new ArrayList<>();
        if (isJudgeRole) {
            values.add(new Value(TRIBUNAL_COSTS.name(), TRIBUNAL_COSTS.toString()));
            values.add(new Value(UNREASONABLE_COSTS.name(), UNREASONABLE_COSTS.toString()));
            values.add(new Value(WASTED_COSTS.name(), WASTED_COSTS.toString()));
        }

        DynamicList typesOfCostsToConsider = new DynamicList(new Value("", ""), values);

        asylumCase.write(JUDGE_APPLIED_COSTS_TYPES, typesOfCostsToConsider);
        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
